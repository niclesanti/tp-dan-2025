# Eureka Server — `dan-eureka-server`

Este documento explica **cómo está implementado y cómo funciona** el servidor de service discovery (Netflix Eureka) del sistema, y luego cómo funcionan los **clientes de Eureka** (`user-svc`, `gestion-svc`, `reservas-svc`, `dan-spring-gateway`) que se registran contra él.

> Para el historial de cambios que llevaron a esta configuración (correcciones de hostname, instance-id, escalado) ver [ETAPA04.md](./ETAPA04.md). Para cómo el gateway usa el registro de Eureka para balancear carga, ver [API-GATEWAY.md](./API-GATEWAY.md).

---

## 1. Qué es y por qué se usa

Eureka es un **servicio de registro y descubrimiento** (service discovery): un directorio en memoria donde cada microservicio se anota a sí mismo ("estoy vivo, me llamo `user-svc`, estoy en esta IP:puerto") y desde donde otros componentes (en este proyecto, principalmente el gateway) pueden preguntar "¿dónde están las instancias vivas de `user-svc` en este momento?" en vez de tener hardcodeada una IP fija.

Esto es necesario porque los microservicios pueden **escalar horizontalmente** (`docker compose up --scale gestion-svc=3`) y las instancias pueden aparecer/desaparecer dinámicamente — sin un directorio centralizado, el gateway no tendría forma de saber cuántas réplicas hay ni a cuáles reenviar tráfico.

---

## 2. Implementación del servidor

El módulo `common/dan-eureka-server` es una aplicación Spring Boot mínima dedicada exclusivamente a correr el servidor Eureka.

### a) Dependencia

En `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

Este starter trae toda la implementación del servidor Eureka de Netflix OSS, envuelta por Spring Cloud: el registro en memoria, la UI web de administración, y la API REST (`/eureka/apps/**`) que usan los clientes para registrarse, mandar heartbeats y consultar el registro.

### b) La anotación `@EnableEurekaServer`

```java
@SpringBootApplication
@EnableEurekaServer
public class DanEurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DanEurekaServerApplication.class, args);
    }
}
```

A diferencia del *cliente* Eureka (que se autoconfigura solo con tener la dependencia en el classpath, sin anotaciones — ver [API-GATEWAY.md](./API-GATEWAY.md) sección 3), **el servidor sí requiere una anotación explícita**: `@EnableEurekaServer`. Esta anotación es la que activa los `@Configuration` internos de Spring Cloud Netflix que:

- Levantan los controllers REST del protocolo Eureka (`/eureka/apps`, `/eureka/apps/{appId}`, etc.) que los clientes usan para registrarse y consultar.
- Exponen el **dashboard HTML** de administración en la raíz (`http://localhost:8761/`), donde se puede ver visualmente qué instancias están registradas, su estado (`UP`/`DOWN`), IP, etc.
- Inicializan el **registro en memoria** (`PeerAwareInstanceRegistry`) donde se almacenan las instancias.

Sin esta anotación, la dependencia en el classpath no alcanza — la app levantaría como un Spring Boot normal sin ningún endpoint de Eureka.

### c) Configuración (`application.properties`)

```properties
server.port=8761

eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.serviceUrl.defaultZone=http://dan-eureka-server:8761/eureka/

eureka.server.enable-self-preservation=false
eureka.server.eviction-interval-timer-in-ms=5000
```

Punto importante y no obvio: **el servidor Eureka internamente también incluye un cliente Eureka** (porque en un despliegue en clúster con múltiples nodos de Eureka, cada nodo se registra como peer de los demás para replicar el registro entre sí — alta disponibilidad del propio directorio). En este proyecto hay un solo nodo Eureka, así que:

- `eureka.client.register-with-eureka=false`: le dice a este servidor que **no se registre a sí mismo como cliente** (evita que aparezca como una instancia más en su propio dashboard y evita ruido/errores al intentar auto-registrarse).
- `eureka.client.fetch-registry=false`: tampoco necesita **descargar** el registro de otro servidor peer, porque no hay otros nodos — es el único.
- `eureka.client.serviceUrl.defaultZone`: queda configurada (apuntando a sí mismo) pero es efectivamente **inerte** mientras las dos propiedades anteriores estén en `false`. Es la configuración que se usaría si este Eureka corriera en modo clúster con más nodos peer; se deja declarada por si se necesitara escalar a HA en el futuro, pero hoy no tiene efecto práctico.

Esto configura al servidor en modo **standalone** (un solo nodo), que es lo apropiado para este proyecto académico — no hay necesidad de alta disponibilidad del propio Eureka.

#### Self-preservation y eviction

```properties
eureka.server.enable-self-preservation=false
eureka.server.eviction-interval-timer-in-ms=5000
```

- **Self-preservation** es un mecanismo de Eureka pensado para producción: si el servidor detecta que está recibiendo *muchos menos* heartbeats de los esperados (por defecto, si la tasa de renovaciones cae por debajo del 85% de lo esperado), asume que **probablemente hay un problema de red** (partición) y no una caída masiva real de instancias — y entonces **deja de desregistrar instancias**, incluso si no mandan heartbeat, para evitar eliminar instancias sanas por error. Es una protección contra falsos positivos.
- En este proyecto se **desactiva** (`enable-self-preservation=false`). Esto es apropiado para desarrollo/testing: en un entorno Docker Compose local, los contenedores se detienen y reinician constantemente (rebuilds, restarts manuales, escalado hacia abajo), y se prefiere que las instancias caídas se **remuevan del registro rápido y de forma confiable** en vez de quedar "fantasma" en el dashboard por la protección de self-preservation. El trade-off (aceptable en dev, riesgoso en producción) es que ante una partición de red real, Eureka podría desregistrar instancias que en realidad siguen vivas.
- `eviction-interval-timer-in-ms=5000`: cada cuánto corre la tarea en background que revisa y expulsa (*evict*) del registro a las instancias cuyo lease (concesión) venció. El valor por defecto de Eureka es 60000 ms (60 s); acá se bajó a 5000 ms (5 s) para que, en desarrollo, una instancia caída desaparezca del registro casi de inmediato en vez de tardar hasta un minuto — feedback más rápido al probar escalado o caídas.

#### Logging y observabilidad
El resto del archivo (`logging.level.com.netflix.eureka=DEBUG`, `logging.level.com.netflix.discovery=DEBUG`, patrón de logs con `traceId`/`spanId`, y las propiedades `management.*`/`management.otlp.*`) sigue el mismo patrón de observabilidad que el resto de los módulos del sistema (exportación a OpenTelemetry Collector → Tempo/Prometheus/Loki, ver `AGENTS.md`), más el logging a nivel `DEBUG` de los paquetes internos de Netflix Eureka/Discovery para poder ver en los logs el detalle de registros, heartbeats y evicciones mientras se desarrolla.

### d) Despliegue en Docker Compose

```yaml
dan-eureka-server:
  build:
    context: .
    dockerfile: common/dan-eureka-server/Dockerfile
  container_name: dan-eureka-server
  restart: unless-stopped
  ports:
    - "8761:8761"
  networks:
    - tp-dan-network
```

Nótese que este servicio **sí tiene `container_name` fijo** (a diferencia de los servicios de negocio, que lo quitaron para poder escalar horizontalmente — ver ETAPA04.md). Esto es intencional: el Eureka server no se escala (es un único punto de registro), así que mantener un nombre de contenedor fijo y predecible (`dan-eureka-server`) es lo que permite que todos los clientes lo resuelvan de forma estable vía `eureka.client.serviceUrl.defaultZone=http://dan-eureka-server:8761/eureka/`.

En `docker-compose.yml`, todos los demás servicios (`user-svc`, `gestion-svc`, `reservas-svc`, `dan-gateway`) declaran `depends_on: dan-eureka-server: condition: service_started` — es decir, **arrancan después de que Eureka arrancó** (aunque no esperan a que esté "healthy", porque Eureka server no tiene healthcheck definido en el compose; `service_started` solo garantiza orden de arranque del contenedor, no que el endpoint ya esté aceptando registros). En la práctica los clientes reintentan el registro automáticamente si el primer intento falla porque Eureka todavía no levantó del todo.

---

## 3. Cómo funcionan los clientes de Eureka

Los cuatro servicios cliente (`user-svc`, `gestion-svc`, `reservas-svc`, `dan-spring-gateway`) comparten el mismo patrón de configuración en su `application-eureka.properties`, por ejemplo (`user-svc`):

```properties
eureka.client.serviceUrl.defaultZone=http://dan-eureka-server:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.prefer-ip-address=true
spring.application.name=user-svc
server.port=8080
eureka.instance.instance-id=${spring.application.name}:${server.port}:${HOSTNAME:${spring.application.name}}
```

### a) Autoconfiguración, sin anotaciones

Al tener `spring-cloud-starter-netflix-eureka-client` en el classpath (ver `pom.xml` de cada servicio), Spring Cloud detecta la dependencia y **activa automáticamente** el cliente Eureka — no hace falta `@EnableEurekaClient` (deprecada y removida de las clases principales, ver ETAPA04.md) ni ninguna otra anotación. Esto aplica tanto a los tres microservicios de negocio como al gateway.

### b) Ciclo de vida de un cliente Eureka

1. **Registro (`register-with-eureka=true`):** al arrancar, el cliente envía un POST a `http://dan-eureka-server:8761/eureka/apps/{APP-NAME}` con sus metadatos: nombre de la app (`spring.application.name`, p. ej. `USER-SVC` — Eureka normaliza a mayúsculas), IP/host, puerto, `instance-id`, estado inicial, etc. A partir de ese momento la instancia existe en el registro con estado `STARTING` → `UP`.

2. **Heartbeat / renovación de lease:** cada instancia registrada le manda periódicamente al servidor un heartbeat ("sigo viva") para renovar su *lease* (concesión). El intervalo por defecto de Eureka es cada **30 segundos**, y si el servidor no recibe un heartbeat durante (por defecto) **90 segundos**, considera que la instancia murió y la expulsa del registro (en este proyecto esa expulsión se revisa cada 5 segundos en vez de cada 60, por la propiedad `eviction-interval-timer-in-ms` del servidor). Ninguna de estas dos propiedades (`lease-renewal-interval-in-seconds`, `lease-expiration-duration-in-seconds`) fue sobreescrita en los clientes de este proyecto, así que usan los valores default (30 s / 90 s).

3. **Descarga del registro (`fetch-registry=true`):** además de registrarse, cada cliente **descarga y cachea localmente** (en memoria) la lista completa de instancias de todos los servicios registrados, y la refresca periódicamente (cada 30 s por defecto). Esto es clave para el rendimiento: cuando el gateway necesita resolver `lb://user-svc`, **no hace una llamada de red a Eureka en cada request** — consulta su copia local cacheada, que se mantiene actualizada en background. Esto hace que Eureka sea tolerante a caídas temporales de sí mismo: si el servidor Eureka se cae un momento, los clientes siguen funcionando con la última copia del registro que tenían cacheada (a costa de no ver altas/bajas nuevas hasta que Eureka vuelva).

4. **`prefer-ip-address=true`:** cuando el cliente se registra, en vez de anunciar su hostname anuncia directamente su **dirección IP** de contenedor. Esto es la práctica recomendada en Docker, donde el hostname del contenedor no siempre es resoluble por DNS desde otros contenedores de la misma red, mientras que la IP asignada por Docker en la red `tp-dan-network` sí lo es.

5. **`instance-id` único:** la expresión `${spring.application.name}:${server.port}:${HOSTNAME:${spring.application.name}}` genera un identificador único por instancia combinando nombre de la app, puerto, y el hostname del contenedor (que Docker asigna automáticamente y es distinto para cada réplica, p. ej. `gestion-svc-1`, `gestion-svc-2` al escalar). Si `HOSTNAME` no estuviera disponible, cae al nombre de la app como fallback. Esto es lo que permite que, al escalar un servicio (`docker compose up -d --scale gestion-svc=3`), Eureka registre **3 entradas distintas y bien identificadas** bajo la misma app `GESTION-SVC`, en lugar de que una sobreescriba a la otra por tener el mismo ID.

### c) Qué hace cada tipo de cliente con esa información

- **`user-svc`, `gestion-svc`, `reservas-svc`**: se registran para ser *descubribles*, pero no consultan activamente el registro para resolver a otros servicios (no llaman entre sí vía `lb://`) — su `fetch-registry=true` les sirve principalmente para que Eureka no les devuelva advertencias/metadatos incompletos y para consistencia de configuración, pero el caso de uso real de "resolver y balancear contra otro servicio" en este proyecto lo hace el gateway.
- **`dan-spring-gateway`**: es el principal **consumidor activo** del registro. Usa su copia cacheada (vía `fetch-registry=true`) para resolver los `uri=lb://<service-id>` de sus rutas a una lista de instancias concretas, y Spring Cloud LoadBalancer elige una de ellas por Round Robin en cada request (ver [API-GATEWAY.md](./API-GATEWAY.md) sección 5c).

### d) Qué pasa si Eureka no está disponible al arrancar un cliente

Si un servicio cliente arranca antes de que Eureka esté completamente levantado (posible porque `depends_on: dan-eureka-server: condition: service_started` solo espera que el contenedor haya *iniciado*, no que el endpoint HTTP esté listo), el cliente Eureka simplemente **reintenta el registro en background** en los intervalos configurados, sin que esto haga fallar el arranque del microservicio — Eureka es un componente *best-effort*: su ausencia temporal no bloquea el funcionamiento normal (no-Eureka) de cada microservicio, solo retrasa su aparición en el registro/discovery hasta que logre conectar.

---

## Resumen rápido

| Pregunta | Respuesta |
|---|---|
| ¿Qué hace `@EnableEurekaServer`? | Activa los endpoints REST del protocolo Eureka, el dashboard web y el registro en memoria. A diferencia del cliente, el servidor sí necesita esta anotación explícita. |
| ¿Por qué `register-with-eureka=false` / `fetch-registry=false` en el servidor? | Porque el servidor Eureka incluye internamente un cliente (pensado para replicación entre nodos peer en clúster). Con un solo nodo, no hace falta que se auto-registre ni que descargue registro de otro peer. |
| ¿Qué es self-preservation y por qué está deshabilitado? | Protección contra desregistrar instancias en falsos positivos (ej. partición de red). Se deshabilita en este proyecto para que, en desarrollo con Docker, las instancias caídas se remuevan rápido y de forma confiable del registro. |
| ¿Cómo se activa el cliente en cada microservicio? | Solo con tener `spring-cloud-starter-netflix-eureka-client` en el classpath — autoconfiguración, sin anotaciones. |
| ¿Cómo se identifica cada instancia escalada? | Con `eureka.instance.instance-id` combinando nombre de app + puerto + `HOSTNAME` del contenedor (único por réplica). |
| ¿Los clientes consultan Eureka en cada request? | No — descargan y cachean el registro localmente (`fetch-registry=true`) y lo refrescan en background, evitando una llamada de red por cada resolución. |
