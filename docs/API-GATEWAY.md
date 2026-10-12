# API Gateway — `dan-spring-gateway`

Este documento explica **cómo funciona** el componente API Gateway del sistema: por qué se implementó con Spring Cloud Gateway, cómo se integra con Eureka, por qué se eligió el modelo WebMVC en lugar del Reactive, y qué funciones cumple realmente (ruteo, reescritura de paths y balanceo de carga).

> Para el detalle de cada ruta (predicados `Path`, filtro `StripPrefix`) ver [ETAPA03.md](./ETAPA03.md). Para el detalle de Eureka como servidor de descubrimiento ver [ETAPA04.md](./ETAPA04.md). Este documento se centra en el gateway en sí.

---

## 1. Por qué Spring Cloud Gateway

El proyecto necesitaba un **punto de entrada único** (`:8080`) que reciba todo el tráfico del frontend y lo reenvíe al microservicio correspondiente (`user-svc`, `gestion-svc`, `reservas-svc`), sin que el cliente tenga que conocer las direcciones internas de cada uno.

Para esto se creó el módulo `common/dan-spring-gateway` como una aplicación Spring Boot independiente, con las dependencias clave declaradas en su `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

La clase principal (`DanSpringGateway.java`) es una aplicación Spring Boot estándar, sin ninguna configuración Java adicional — **todo el comportamiento del gateway (rutas, filtros, cliente Eureka) se define declarativamente por propiedades**, no por código (`RouteLocatorBuilder` beans, etc.). Esto es válido en Spring Cloud Gateway: las rutas pueden definirse en YAML/properties en vez de Java.

En `docker-compose.yml` el servicio se declara como `dan-gateway`, construido desde `common/dan-spring-gateway/Dockerfile`, sin puerto host publicado en `docker-compose.yml` (se mapea por capas en `docker-compose.override.yml` para desarrollo, ver `AGENTS.md`). Depende (`depends_on`) de que `user-svc`, `reservas-svc` y `gestion-svc` estén `healthy` y de que `dan-eureka-server` haya arrancado, lo cual tiene sentido porque el gateway necesita que haya servicios registrados para poder balancear tráfico hacia ellos.

---

## 2. Los dos archivos de properties: ¿cuál se usa?

Hay dos archivos:

- `application.properties` — configuración **base**, siempre se carga.
- `application-eureka.properties` — configuración del **profile `eureka`**, solo se carga si ese profile está activo.

En `docker-compose.yml`, el gateway arranca con:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: ${SPRING_PROFILE:-dev,eureka}
```

Y `SPRING_PROFILE` por defecto vale `dev,eureka` (definido en `.env` / default del compose). Es decir: **en el entorno normal del proyecto, el profile `eureka` siempre está activo**, por lo que `application-eureka.properties` también se carga.

Importante: Spring **no usa "solo uno" de los dos archivos** — `application.properties` se carga siempre como base, y `application-eureka.properties` se carga *encima*, sobrescribiendo las claves que coincidan. Por eso tu intuición es parcialmente correcta ("se usa el que dice eureka") pero el mecanismo real es **merge con prioridad del profile**, no una elección excluyente. Esto importa porque:

| Clave | `application.properties` (base) | `application-eureka.properties` (profile) | Resultado con profile `eureka` activo |
|---|---|---|---|
| `routes[0]` (user-svc) | `uri=http://user-svc:8080` | `uri=lb://user-svc` | **gana el profile** → `lb://user-svc` |
| `routes[1]` (reservas-svc) | `uri=http://reservas-svc:8080` | `uri=lb://reservas-svc` | **gana el profile** → `lb://reservas-svc` |
| `routes[2]` (gestion-svc) | `uri=http://gestion-svc:8080` | `uri=lb://gestion-svc` | **gana el profile** → `lb://gestion-svc` |
| `routes[3]` (bancos-svc) | `uri=http://user-svc:8080` | `uri=lb://user-svc` | **gana el profile** → `lb://user-svc` |
| `routes[4]` (habitaciones-svc) | `uri=http://reservas-svc:8080` | `uri=lb://reservas-svc` | **gana el profile** → `lb://reservas-svc` |

**Nota (ya corregido):** inicialmente las rutas `bancos-svc` y `habitaciones-svc` (índices 3 y 4) solo existían en el archivo base y no habían sido migradas al archivo `-eureka`, por lo que con el profile `eureka` activo quedaban apuntando al hostname Docker fijo en vez de usar `lb://` + Eureka (sin participar del balanceo de carga). Se agregaron `routes[3]`/`routes[4]` con `uri=lb://user-svc` / `uri=lb://reservas-svc` en `application-eureka.properties`, de modo que las 5 rutas sean consistentes y todas pasen por Eureka + Spring Cloud LoadBalancer cuando ese profile está activo.

---

## 3. Cómo se configuró el gateway como cliente Eureka

La integración con Eureka tiene tres partes:

**a) Dependencia:** `spring-cloud-starter-netflix-eureka-client` en el `pom.xml`. Solo con tenerla en el classpath, Spring Cloud auto-configura el cliente Eureka (no hace falta anotar la clase principal con `@EnableDiscoveryClient` o `@EnableEurekaClient` — eso era necesario en versiones viejas de Spring Cloud Netflix; desde hace varias versiones la autoconfiguración se activa automáticamente al detectar la dependencia, salvo que se desactive explícitamente con `eureka.client.enabled=false`).

**b) Propiedades de cliente**, en `application-eureka.properties`:

```properties
eureka.client.serviceUrl.defaultZone=http://dan-eureka-server:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}:${server.port}:${HOSTNAME:${spring.application.name}}
```

- `serviceUrl.defaultZone`: le dice al cliente dónde está el servidor Eureka (`dan-eureka-server`, resuelto por DNS de Docker).
- `register-with-eureka=true`: el gateway **se registra a sí mismo** como una instancia más en Eureka (con nombre `dan-spring-gateway`, tomado de `spring.application.name`).
- `fetch-registry=true`: además de registrarse, **descarga y cachea** el registro completo de instancias de otros servicios, para poder resolver `lb://user-svc` localmente sin tener que consultar a Eureka en cada request.
- `prefer-ip-address=true`: se registra usando la IP del contenedor en vez del hostname, lo cual es la práctica recomendada en entornos Docker/containers donde el hostname no siempre es resoluble desde otros servicios.
- `instance-id`: fuerza un ID de instancia único combinando nombre + puerto + hostname, para distinguir réplicas cuando el servicio se escala (`docker compose --scale`).

**c) El "glue" de ruteo:** `spring.cloud.gateway.discovery.locator.enabled=true` habilita el *discovery locator*, que crea rutas automáticas para cada servicio que aparece en Eureka (útil como fallback), aunque este proyecto igual define rutas manuales explícitas (`routes[0..2]`) con `uri=lb://<service-id>` — el esquema `lb://` es lo que le indica al gateway "no vayas directo a este host, resolvé el destino a través del cliente de balanceo de carga usando el `service-id` registrado en Eureka".

---

## 4. WebMVC vs. Reactive: qué significa y por qué se eligió WebMVC

Spring Cloud Gateway tiene **dos implementaciones** posibles:

| | Gateway "clásico" (Reactive) | Gateway **Server MVC** (usado en este proyecto) |
|---|---|---|
| Starter | `spring-cloud-starter-gateway` | `spring-cloud-starter-gateway-server-webmvc` |
| Modelo de servidor | **WebFlux** — reactivo, no bloqueante, basado en Project Reactor | **Spring MVC** — servlet, basado en hilos (thread-per-request) |
| Servidor embebido | Netty | Tomcat (el mismo que usan `user-svc`, `gestion-svc`, `reservas-svc`) |
| Tipo de aplicación | `reactive` | `servlet` |

En este proyecto, `application.properties` fuerza explícitamente el modo MVC:

```properties
spring.cloud.gateway.server.webmvc.enabled=true
spring.main.web-application-type=servlet
```

**Qué significa en la práctica:**

- El gateway corre sobre **Tomcat**, con el modelo clásico de **un hilo por petición entrante**, igual que cualquier controlador `@RestController` tradicional de Spring MVC. No usa programación reactiva (`Mono`/`Flux`, Project Reactor, Netty).
- Esto es coherente con el resto del stack: los tres microservicios de negocio (`user-svc`, `gestion-svc`, `reservas-svc`) también son aplicaciones Spring MVC servlet tradicionales. Al usar el gateway en modo MVC, **todo el sistema usa el mismo paradigma de programación** (bloqueante/servlet), evitando mezclar código reactivo (gateway) con código imperativo (microservicios) y el salto mental/operativo que eso implica (debugging, trazas, manejo de excepciones, hilos vs. event loop).
- **Trade-off:** el modelo reactivo (Netty/WebFlux) escala mejor en escenarios de muy alta concurrencia con I/O-bound workloads, porque no bloquea un hilo por conexión abierta. El modelo servlet consume un hilo del pool por cada request en vuelo, lo cual es menos eficiente bajo cargas masivas, pero es más simple de razonar, depurar y es suficiente para el volumen de tráfico de este trabajo práctico.
- Conceptualmente, `spring-cloud-gateway-server-webmvc` fue introducido por Spring Cloud justamente para dar una opción no-reactiva a quienes no quieren (o no necesitan) adoptar WebFlux solo para tener un gateway — manteniendo las mismas abstracciones de rutas/predicados/filtros.

---

## 5. ¿Qué funciones cumple este gateway? ¿Es solo un reverse proxy?

No es *solo* un reverse proxy. Cumple tres funciones:

### a) Reverse proxy / ruteo por path
Cada ruta define un **predicado** (`Path=/users/**`, `/reservas/**`, `/gestion/**`, etc.) que decide a qué microservicio reenviar la petición entrante, y un **filtro** `StripPrefix=1` que reescribe la URL quitando el primer segmento del path antes de reenviarla (detalle completo y ejemplos en [ETAPA03.md](./ETAPA03.md)).

### b) Punto único de observabilidad
El gateway centraliza trazas (OpenTelemetry/Tempo), métricas (Prometheus vía Micrometer) y logs correlacionados por `traceId`/`spanId`, exponiendo `management.endpoints.web.exposure.include=*` y exportando a `otel-collector:4318`. Esto le da visibilidad end-to-end de cada request que entra al sistema, independientemente de a qué servicio se dirija.

### c) Load balancer (sí, está configurado)

Cuando el profile `eureka` está activo, las rutas `user-svc`, `reservas-svc` y `gestion-svc` usan `uri=lb://<service-id>` en lugar de una URL fija. El esquema `lb://` es reconocido por **Spring Cloud LoadBalancer**, que:

1. Resuelve `<service-id>` contra el registro de Eureka (cacheado localmente por el cliente, gracias a `fetch-registry=true`) y obtiene la lista de instancias vivas de ese servicio (por IP, gracias a `prefer-ip-address=true`).
2. Elige una instancia entre las disponibles aplicando un **algoritmo de balanceo**.
3. Reenvía la petición a la instancia elegida.

**¿Qué algoritmo usa?** Por defecto, Spring Cloud LoadBalancer usa **Round Robin** (`RoundRobinLoadBalancer`), que reparte las peticiones de forma secuencial y equitativa entre todas las instancias registradas y saludables de un servicio. El proyecto **no sobreescribe este comportamiento** — no hay ningún `@Bean` ni propiedad que configure una estrategia distinta (p. ej. `ZonePreferenceServiceInstanceListSupplier` o balanceo ponderado) — por lo tanto se usa el algoritmo por defecto. Esto es lo que permite que `docker compose --scale user-svc=3` reparta el tráfico entre las 3 réplicas automáticamente (ver ETAPA04.md para el detalle del escalado).

Esto se confirma además al inspeccionar el árbol de dependencias de Maven (`./mvnw dependency:tree`): `spring-cloud-starter-netflix-eureka-client` trae transitivamente `spring-cloud-starter-loadbalancer` → `spring-cloud-loadbalancer`. Es decir, **el balanceador de carga no se agregó como dependencia explícita** — llega automáticamente junto con el cliente Eureka, y el gateway lo activa simplemente usando el esquema `lb://` en sus rutas.


---

## 6. ¿Usa Zuul y Ribbon?

**No.** Ninguno de los dos:

- **Zuul** (el gateway original de Netflix OSS, integrado antes como `spring-cloud-starter-netflix-zuul`) está **deprecado/descontinuado** dentro del ecosistema Spring Cloud desde hace varias generaciones. Spring Cloud Gateway (el proyecto propio de Spring Cloud, no de Netflix) es su reemplazo oficial, y es justamente lo que usa este proyecto (`spring-cloud-starter-gateway-server-webmvc`). Zuul ni siquiera aparece como dependencia, directa o transitiva, en el `pom.xml` del módulo.
- **Ribbon** (`spring-cloud-starter-netflix-ribbon`, el balanceador de carga lado-cliente original de Netflix) también está **deprecado y fue reemplazado por Spring Cloud LoadBalancer** desde Spring Cloud 2020.0 (Ilford) en adelante. Este proyecto usa Spring Cloud 2025.0.0 (`spring-cloud-dependencies` en el `pom.xml` raíz), muy posterior a ese cambio, y como se confirmó en la sección anterior, la dependencia transitiva que llega con el cliente Eureka es `spring-cloud-starter-loadbalancer`, no Ribbon.

En resumen: este proyecto usa exclusivamente componentes **actuales** del ecosistema Spring Cloud (Gateway Server MVC + LoadBalancer + Eureka Client), sin ninguna dependencia de los componentes Netflix OSS que fueron descontinuados (Zuul, Ribbon, Hystrix).

---

## Resumen rápido

| Pregunta | Respuesta |
|---|---|
| ¿Se usa solo `application-eureka.properties`? | No — se usan **ambos**, el profile sobreescribe la base solo en las claves que redefine (las 5 rutas, 0–4, ya están cubiertas con `lb://` en el profile `eureka`). |
| ¿Cómo se configuró como cliente Eureka? | Dependencia `spring-cloud-starter-netflix-eureka-client` + propiedades `eureka.client.*` / `eureka.instance.*` en el profile `eureka`. Sin anotaciones Java — autoconfigurado. |
| ¿Qué es WebMVC vs Reactive? | Spring Cloud Gateway tiene dos implementaciones: WebFlux/Netty (reactiva, no bloqueante) y **Server MVC/Tomcat** (servlet, bloqueante, la usada acá). Se eligió MVC para ser consistente con el resto de los microservicios, que también son Spring MVC. |
| ¿Es solo reverse proxy? | No. También centraliza observabilidad y hace **load balancing** vía Eureka + Spring Cloud LoadBalancer para las rutas `lb://`. |
| ¿Qué algoritmo de balanceo usa? | **Round Robin** (default de Spring Cloud LoadBalancer, sin overrides en el proyecto). |
| ¿Usa Zuul o Ribbon? | No — ambos están deprecados en el ecosistema Spring Cloud. Se usa Spring Cloud Gateway (no Zuul) y Spring Cloud LoadBalancer (no Ribbon). |
