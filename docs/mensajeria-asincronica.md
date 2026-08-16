# Mensajería asíncronica en el TP DAN 2025

Documento de aprendizaje. Explica, desde cero, cómo funciona la comunicación asincrónica entre
`gestion-svc` y `reservas-svc` usando RabbitMQ, con ejemplos reales de este repositorio.

> Término para ir guardando: **asíncrono** = el emisor no espera la respuesta. Emite "y sigue con su vida".
> A diferencia de un `GET /api/...` (síncrono), aquí nadie se queda bloqueado esperando un `200 OK`.

---

## 1. ¿Este proyecto usa el estilo Publish-Subscribe?

**Sí**, pero con matices. Es una arquitectura de microservicios **orientada a eventos** ([event-driven]).
Cuando `gestion-svc` crea/actualiza/elimina una habitación, **publica** un evento; `reservas-svc` está
**suscrito** a un subconjunto de "temas" y **consume** esos eventos.

El patrón Publish-Subscribe dice:
- El **publisher** (editor) no conoce a los suscriptores. Solo emite un mensaje a un lugar neutro (el broker).
- Los **subscribers** (suscriptores) expresan interés en ciertos mensajes y escuchan.
- Existe un intermediario (**message broker**) que desacopla a ambos extremos.

```
   ┌────────────┐  publica   ┌──────────────┐   enruta    ┌──────────────┐  consume   ┌────────────┐
   │ gestion-svc│ ─────────▶ │ RabbitMQ     │ ─────────▶ │ cola/topic   │ ─────────▶ │ reservas-svc│
   │ (publisher)│            │ (broker)     │            │ habitacion.topic│           │ (subscriber)│
   └────────────┘            └──────────────┘            └──────────────┘            └────────────┘
```

El matiz importante: RabbitMQ tiene **varios tipos de exchange** y aquí se usa el tipo **`topic`**.
Con un exchange `topic`, el enrutamiento no es "broadcast a todos" (eso sería `fanout`), sino que va
**según un patrón de routing key**. Ver sección 4.

Categorización correcta para la memoria del TP: el proyecto combina:
- Arquitectura de **microservicios** (comunicación REST síncrona vía gateway/Eureka **y** eventos asincrónicos).
- Patrón **Publish-Subscribe / event-driven** para sincronizar el catálogo de habitaciones entre gestión y reservas.

### ¿Publish-Subscribe y event-driven es lo mismo?

**No exactamente.** Son conceptos relacionados pero a niveles distintos:

**Event-driven** (arquitectura orientada a eventos) es un estilo arquitectónico **amplio**: el sistema
reacciona a eventos (hechos que ocurren) en vez de a llamadas directas. Lo importante es *qué dispara la
acción* (un evento), no *cómo se transporta*. Puede haber event-driven **sin** broker ni pub/sub: por
ejemplo un trigger de base de datos, CDC (Change Data Capture) o un `WebSocket` que avisa "algo pasó".

**Publish-Subscribe** es un **patrón de integración específico** (un mecanismo) para implementar la
comunicación: un emisor publica a un intermediario y N suscriptores que expresan interés reciben el
mensaje. Es una de las **formas de llevar a cabo** el estilo event-driven.

```
event-driven (estilo arquitectónico — el "por qué" reaccionás)
   └── pub/sub (mecanismo — el "cómo" entregás el evento)
   └── punto-a-punto / colas (mecanismo alternativo)
```

**En este TP** se usan las dos cosas, encadenadas: se decidió que el sistema es **event-driven** (cuando
se crea/modifica/borra una habitación "algo pasó" y `reservas-svc` debe enterarse sin que nadie la llame),
y eso se implementa con el patrón **pub/sub** (exchange `dan.exchange` con varios listeners suscritos).

La prueba de que no son lo mismo = un sistema puede ser uno sin ser el otro:
- **Pub/sub sin event-driven:** un job que publica "estado X" periódicamente (no es un evento real del negocio).
- **Event-driven sin pub/sub:** un solo consumidor leyendo una cola punto-a-punto; sigue siendo event-driven.

---

## 2. ¿Qué es JMS? ¿Qué es AMQP?

Son **dos cosas distintas** que la gente suele confundir. (De hecho, este código tiene comentarios que
dicen "Publicar evento JMS"… pero el proyecto **no usa JMS**, usa AMQP. Es un nombre genérico mal puesto.)

### JMS — Java Message Service
- Es una **API estándar de Java** (interfaz, no implementación), parte de Jakarta EE.
- Define cómo una app Java produce/consume mensajes, con modelos **P2P** (cola) y **pub/sub** (topic).
- Necesita un proveedor concreto (\*implemements*): ActiveMQ, Artemis, IBM MQ, etc.
- **Solo sirve para Java.** Es un contrato de lenguaje.

### AMQP — Advanced Message Queuing Protocol
- Es un **protocolo de red** (como HTTP o TCP). Define el formato en que viajan los bytes por el cable.
- Nace en finanzas y está pensado para **interoperar entre lenguajes** (Java lee lo que publicó Go, etc.).
- Define entidades de nivel alto: **exchange**, **queue**, **binding**, **message**.
- RabbitMQ es el broker AMQP más famoso.

| | JMS | AMQP |
|---|---|---|
| Qué es | Librería/estándar Java | Protocolo de red |
| Ámbito | Solo Java | Multilenguaje |
| Medios | Colas y topics (conceptos propios) | Exchanges, queues, bindings, routing keys |
| Broker popular | ActiveMQ/Artemis | RabbitMQ |

> En este proyecto, **Spring AMQP** (módulo `spring-boot-starter-amqp`) es la capa de Spring que habla
> el protocolo AMQP con RabbitMQ. Es Spring AMQP quien convierte nuestro objeto Java
> `HabitacionEvent` en un mensaje AMQP (JSON) y viceversa.

---

## 3. ¿Qué es un broker? ¿Este proyecto usa uno?

Un **message broker** es un servicio intermedio (un servidor más, con su propia red y almacenamiento)
que:
- **Recibe** mensajes de los productores.
- Los **guarda temporalmente** en colas internas.
- Los **enruta** hacia las colas que correspondan según reglas.
- Los **entrega** a los consumidores suscritos.

Ventajas que da:
- **Desacople temporal y espacial:** el publisher y el subscriber no necesitan estar vivos al mismo tiempo
  ni conocerse.
- **Garantías de entrega:** si `reservas-svc` está caído, el mensaje queda en la cola y se entrega cuando vuelva.
- **Balanceo** o **escalado** de consumidores.

**Este proyecto sí usa un broker: RabbitMQ**, levantado como contenedor Docker en `docker-compose.yml:131`:

```yaml
rabbitmq:
  image: rabbitmq:3-management   # imagen oficial de Docker Hub (incluye panel web en :15672)
  volumes:
    - ./infra/rabbitmq/definitions.json:/etc/rabbitmq/definitions.json:ro
```

---

## 4. Publisher, subscriber, evento, topic y exchange

### ¿Quién publica y quién se suscribe en este proyecto?

| Rol | Servicio | Clase | Qué hace |
|---|---|---|---|
| **Publisher** | `gestion-svc` | `gestion/messaging/GestionMessagePublisher.java` | Toma un `HabitacionEvent`/`HotelCierreEvent` y lo envía al exchange `dan.exchange`. |
| **Subscriber** | `reservas-svc` | `messaging/GestionMessageListener.java` | Escucha la cola `habitacion.topic` (patrón `dan.habitacion.#`) y replica la habitación en MongoDB. |
| **Subscriber** (2) | `reservas-svc` | `messaging/HotelCierreListener.java` | Escucha `dan.hotel.cerrar` y crea reservas `BLOQUEADAS` para las habitaciones del hotel cerrado. |

### Conceptos clave (jerga del proyecto y de RabbitMQ/AQMP)

**Evento**
Un **mensaje que describe que algo pasó**. No pide nada, solo informa. En este repo los eventos son las
clases de `dan-common-lib`:

- `HabitacionEvent { habitacion: HabitacionDTO, tarifa: TarifaDTO, tipoEvento: TipoEvento }`
- `HotelCierreEvent { hotel: HotelDTO, habitaciones: List<HabitacionDTO> }`
- `TipoEvento` = enum `{ CREAR, ACTUALIZAR_DATOS, ACTUALIZAR_PRECIO, ELIMINAR, CERRAR_HOTEL }`

**Exchange (intercambiador)**
"Recepcionista" del broker. El publisher **nunca publica directo a una cola**: publica al **exchange**,
y del exchange los mensajes salen hacia las colas según reglas de binding. En este repo, el exchange se
llama `dan.exchange` y es de tipo **topic** (`infra/rabbitmq/definitions.json:25`).

**Routing key**
Etiqueta que el publisher pone al mensaje. El exchange la usa para decidir a qué colas repartirlo.
El publisher de este proyecto usa estas routing keys (`GestionMessagePublisher.java:19-23`):

```java
dan.habitacion.crear      // CREAR
dan.habitacion.actualizar // ACTUALIZAR_DATOS
dan.habitacion.precio     // ACTUALIZAR_PRECIO
dan.habitacion.eliminar   // ELIMINAR
dan.hotel.cerrar          // CERRAR_HOTEL
```

**Queue (cola)**
Búfer donde quedan los mensajes esperando a que un consumidor los tome. En las colas los mensajes se
**encolan en orden** (FIFO) y se **confirman** uno a uno cuando el consumidor los procesó.

**Binding**
Regla que "une" un exchange con una cola, dándole un patrón de routing key. La cola `habitacion.topic`
tiene el binding `dan.habitacion.#` (`definitions.json:49`).

### Topic ≠ cola. La diferencia importante

En AMQP:
- **Cola** = destino final de los mensajes. El consumidor lee de la cola.
- **Topic/exchange `topic`** = es un **tipo de exchange** que enruta usando el patrón `*` (una palabra) y
  `#` (cero o más palabras).

Confusión frecuente: en este repo hay una **cola que se llama `habitacion.topic`** y un exchange tipo
**topic**. El nombre de la cola es solo un nombre; lo que la hace "topic" es el exchange al que está unida
y el patrón del binding.

Con `dan.habitacion.#` el patron matchea:
- `dan.habitacion.crear` ✅
- `dan.habitacion.actualizar` ✅
- `dan.habitacion.precio` ✅
- `dan.hotel.cerrar` ❌ (no empieza con `dan.habitacion.`)

Por eso hace **falta una segunda cola** `hotel.cierre.topic` con binding `dan.hotel.cerrar` para el evento
de cierre de hotel.

### Un detalle de diseño a observar

En `infra/rabbitmq/definitions.json` la cola `hotel.cierre.topic` **no está definida**. ¿Cómo funciona
entonces? Porque el `@RabbitListener` de `HotelCierreListener.java:33-39` declara su propia cola:

```java
@QueueBinding(
    value = @Queue(value = "hotel.cierre.topic", durable = "true"),
    exchange = @Exchange(value = "dan.exchange", type = "topic"),
    key = "dan.hotel.cerrar"
)
```

Spring AMQP tiene dos formas de crear las entidades del broker:
1. **Declarativas vía anotación** (`@RabbitListener`): al arrancar `reservas-svc`, Spring crea la cola, el
   exchange y el binding si no existen.
2. **Estáticas vía archivo de definiciones** (`definitions.json`): se cargan al levantar el contenedor.

Esto es normal; cada equipo elige qué entidades deja "fijas" en la infraestructura y cuáles declara cada
microservicio al arrancar.

---

## 5. ¿Cómo se combina `dan-common-lib`? ¿Siempre se hace así?

`dan-common-lib` contiene los **contratos compartidos** entre servicios: los DTOs (`HotelDTO`,
`HabitacionDTO`, `TarifaDTO`) y los **eventos** (`HabitacionEvent`, `HotelCierreEvent`, `TipoEvento`).

El porqué es pragmático: `gestion-svc` crea el `HabitacionEvent` y lo serializa a JSON; `reservas-svc`
deserializa ese JSON. Para que la deserialización produzca el objeto correcto, **ambos deben conocer el
mismo esquema** del mensaje. Si en `gestion-svc` el campo se llama `tipoEvento` y en `reservas-svc` se lee
`eventType`, se rompe todo.

```
dan-common-lib
 ├── HabitacionEvent      ┐
 ├── HotelCierreEvent     │  "contrato" del mensaje
 ├── TipoEvento           ┘
 ├── HabitacionDTO / HotelDTO / TarifaDTO  (payload dentro del evento)
```

Ambos `pom.xml` dependen de `dan-common-lib` y de `spring-boot-starter-amqp`
(`services/gestion-svc/pom.xml:48`, `services/reservas-svc/pom.xml:40`).

### ¿Es la única manera / cómo se hace en el mundo real?

Compartir una librería de DTOs/eventos es la forma **más simple y típica en proyectos chicos/monorepo**,
pero tiene costos: acopla los servicios a "una misma librería" y hace frágil la evolución (si cambio un
campo, tengo que recompilar los dos lados).

Alternativas usadas en la industria:
- **Schema Registry** (Confluent/Kafka, Azure Schema Registry): los esquemas se versionan centralmente; cada
  lado valida contra el esquema **sin compartir código**.
- **JSON Schema / AsyncAPI**: se define el contrato como documento y cada servicio genera su propio código.
- **Versionado de eventos**: se agrega un campo `version` al mensaje para tolerar cambios.

Moraleja: compartir la librería es correcto aquí (y muy común), pero no es "la" forma universal; es una
**decisión de diseño** con tradeoffs.

---

## 6. ¿Cómo se implementa la comunicación de punta a punta?

Recorre los pasos reales del proyecto:

### Paso 1 — Levantar el broker con Docker (`docker-compose.yml:131`)

```yaml
rabbitmq:
  image: rabbitmq:3-management   # sí, es una imagen oficial de Docker Hub
  ...
  volumes:
    - ./infra/rabbitmq/definitions.json:/etc/rabbitmq/definitions.json:ro
```

Al hacer `docker compose up -d --build`, RabbitMQ arranca **desde la imagen oficial**, y automáticamente
carga `definitions.json` (exchange `dan.exchange`, cola `habitacion.topic`, binding, usuario/vhost).
También expone el panel de gestión web y el puerto AMQP (ver `docker-compose.override.yml`).

El archivo `definitions.json` responde tu pregunta: **sí, hay una "configuración estructural" del broker**
(qué exchange, qué colas, qué bindings) que se define antes de levantar nada.

### Paso 2 — Dependencias en cada servicio (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>edu.utn.frsf.isi.dan</groupId>
    <artifactId>dan-common-lib</artifactId>
</dependency>
```

### Paso 3 — Configuración de conexión (`application.properties`)

`gestion-svc/src/main/resources/application.properties:24-27` y `reservas-svc/...`:

```properties
spring.rabbitmq.host=${RABBITMQ_HOST:rabbitmq}
spring.rabbitmq.port=5672
spring.rabbitmq.username=${RABBITMQ_USER:admin}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:admin}
```

En Docker, `RABBITMQ_HOST=rabbitmq` → el nombre del contenedor en la red `tp-dan-network`.
En `reservas-svc` hay además una línea clave para la deserialización:

```properties
spring.amqp.deserialization.trust.all=true
```

> Seguridad: Spring Boot (desde 3.x) no deserializa clases arbitrarias por defecto. Sin `trust.all=true`
> (o un lista de paquetes confiables), el cast de JSON → `HabitacionEvent` **falla**. `trust.all=true`
> es cómodo en desarrollo pero un riesgo de seguridad en producción; lo correcto sería listar solo
> `edu.utn.frsf.isi.dan.shared.*`.

### Paso 4 — Serializar/deserializar con JSON (`RabbitMQConfig.java` de cada servicio)

Ambos servicios definen un `MessageConverter` basado en Jackson:

```java
@Bean
public MessageConverter jackson2MessageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

Con esto, el objeto Java `HabitacionEvent` viaja como JSON por el cable y se reconstruye del otro lado.

### Paso 5 — El publicador (lado `gestion-svc`)

`GestionMessagePublisher.java` inyecta `RabbitTemplate` y usa `convertAndSend(exchange, routingKey, objeto)`:

```java
rabbitTemplate.convertAndSend("dan.exchange", "dan.habitacion.crear", event);
```

¿Cuándo se llama? En `HabitacionServiceImpl.crearHabitacion()`, después de guardar en PostgreSQL
(`services/gestion-svc/.../HabitacionServiceImpl.java:67`):

```java
var habitacionGuardada = habitacionRepository.save(habitacion);
publicarEventoHabitacion(habitacionGuardada, TipoEvento.CREAR);  // -> messagePublisher.publishHabitacionEvent(event)
```

Igual en `actualizarHabitacion`, `eliminarHabitacion` (líneas 88, 104) y en el cierre de hotel en
`HotelServiceImpl.cerrarHotel` (línea 101).

### Paso 6 — El suscriptor (lado `reservas-svc`)

`GestionMessageListener.java` usa `@RabbitListener`, que **declara** la cola/binding y queda escuchando:

```java
@RabbitListener(
    bindings = @QueueBinding(
        value = @Queue(value = "habitacion.topic", durable = "true"),
        exchange = @Exchange(value = "dan.exchange", type = "topic"),
        key = "dan.habitacion.#"
    ),
    ackMode = "MANUAL"   // ← nosotros confirmamos el mensaje a mano
)
public void receiveMessage(HabitacionEvent event, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
    try {
        habitacionService.handleEvent(event);   // replica en MongoDB
        channel.basicAck(deliveryTag, false);   // ➜ aviso al broker "procesado, sacalo de la cola"
    } catch (Exception e) {
        channel.basicReject(deliveryTag, false); // ➜ "no lo procesé, y NO lo reentegues" (descartado)
    }
}
```

El `handleEvent` (`reservas-svc/.../HabitacionServiceImpl.java:61`) aplica la lógica según el tipo:

```java
switch (event.getTipoEvento()) {
    case CREAR        -> save(mapFromHabitacion(event.getHabitacion()));   // inserta en Mongo
    case ACTUALIZAR_DATOS -> updateByHabitacionId(...);                   // actualiza
    case ACTUALIZAR_PRECIO -> actualizarPrecioPorTipo(event.getTarifa()); // re-precio por tipo
    case ELIMINAR     -> deleteByHabitacionId(...);
    ...
}
```

El `HotelCierreListener.java` hace lo mismo con `dan.hotel.cerrar`, creando reservas `BLOQUEADAS`.

### Paso 7 — Flujo completo con un ejemplo real

Imaginá que el frontend crea una "Habitación 101" en el módulo de gestión:

```
1. POST /gestion/habitaciones  (vía gateway) → gestion-svc
2. gestion-svc guarda "101" en PostgreSQL
3. gestion-svc arma HabitacionEvent{CREAR} y rabbitTemplate.convertAndSend
   ("dan.exchange", "dan.habitacion.crear", event)
4. RabbitMQ recibe el mensaje, lo enruta por el exchange topic:
   "dan.habitacion.crear" matchea binding "dan.habitacion.#"  ➜  a la cola habitacion.topic
5. reservas-svc (con su listener activo) toma el mensaje desde la cola (FIFO)
6. Jackson deserializa el JSON al objeto HabitacionEvent
7. handleEvent(CREAR)  ➜  mapFromHabitacion(...)  ➜  inserta el documento en MongoDB
8. channel.basicAck(deliveryTag, false)  ➜  el broker descarta el mensaje de la cola
```

Resultado: las habitaciones disponibles para buscar/reservar (MongoDB) quedan **sincronizadas con el
catálogo** (PostgreSQL) **sin que ningún servicio haga llamadas REST al otro** y **sin bloquearse** entre sí.

---

## Resumen final (para recordar en 1 minuto)

| Pregunta | Respuesta corta |
|---|---|
| ¿Publish-Subscribe? | Sí, estilo **event-driven**, con exchange tipo `topic` de RabbitMQ. |
| ¿JMS o AMQP? | **AMQP** (protocolo). JMS es otra cosa (API Java) y acá **no se usa**; los comentarios "JMS" son un nombre genérico. |
| ¿Usa broker? | Sí: **RabbitMQ**, en Docker con la imagen `rabbitmq:3-management`. |
| ¿Quién publica/consume? | `gestion-svc` publica `HabitacionEvent`/`HotelCierreEvent`; `reservas-svc` los consume y replica en MongoDB. |
| ¿Evento vs topic? | Evento = mensaje que informa que algo pasó. Topic = tipo de exchange que enruta por routing key con patrones `*`/`#`. |
| ¿`dan-common-lib`? | Provee los **contratos compartidos** (eventos + DTOs) para que prop/cons deserialicen igual. Es la forma más simple; hay alternativas (Schema Registry, versionado). |
| ¿Cómo se implementa? | 1) Broker en Docker con `definitions.json`, 2) dependencia `spring-boot-starter-amqp`, 3) `spring.rabbitmq.*` en properties, 4) `Jackson2JsonMessageConverter`, 5) `RabbitTemplate.convertAndSend` en el publisher, 6) `@RabbitListener` + ACK manual en el consumidor. |

### Glosario rápido

- **Broker**: servidor intermedio de mensajería (aquí, RabbitMQ).
- **Producir/Publicar**: enviar un mensaje al broker.
- **Consumir**: leer un mensaje de una cola y procesarlo.
- **Exchange**: entrada del broker; recibe mensajes y los enruta.
- **Cola (queue)**: búfer FIFO donde esperan los mensajes para ser consumidos.
- **Binding**: regla exchange ↔ cola con un patrón de routing key.
- **Routing key**: "dirección" que el mensaje lleva puesta.
- **Evento**: mensaje que describe un hecho ocurrido.
- **ACK / NACK**: confirmación de "procesé el mensaje" / "no pude, descartalo" (`chapter: basicAck / basicReject`).