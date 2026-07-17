# Funciones que debe tener la interfaz de usuario
A continuación se describe el conjunto de funciones que debe proveer la interfaz gráfica de usuario.
Estas listas de funciones que debe proveer el frontend son mencionadas en base a los endpoints disponibles en el backend.

## Usuarios
Gestión de entidades usuarios.
El sistema debe permitir los siguientes tipo de búsquedas:
- Buscar usuarios por nombre
- Buscar usuario por DNI exacto
- Buscar usuarios por DNI (parcial)
### Huespedes
El sistema debe permitir registrar un huesped, actualizar la información de un huesped, y eliminar un usuario huesped.
Además se debe poder administrar las tarjetas de los huespedes con las siguientes funciones:
- Agregar tarjeta de crédito
- Eliminar tarjeta de crédito
- Cambiar tarjeta principal
- Listar tarjetas de crédito
### Propietarios
El sistema debe permitir registrar un propietario, actualizar la información de un propietario, y eliminar un usuario propietario.

## Bancos
Gestión de entidades banco. Son requeridos por las entidades usuarios.
El sistema debe permitir registrar un banco, actualizar la información de un banco, eliminar un banco, buscar bancos por id, listar todos los bancos.

## Habitaciones
Gestión de entidades habitaciones.
El sistema debe proveer las siguientes funcionalidades:
- Crear habitación nueva
- Buscar habitación por ID
- Buscar habitaciones: Busca habitaciones con filtros opcionales (cantidad de huéspedes, tipo, rango de precio).
- Actualizar habitación
- Eliminar habitación
- Obtener tarifa vigente de habitación: Retorna la tarifa vigente (hoy) para una habitación específica

## Hoteles
Gestión de entidades hoteles.
El sistema debe proveer las siguientes funcionalidades:
- Crea un nuevo hotel con todos sus datos
- Buscar hotel por ID: Retorna los datos de un hotel específico incluyendo sus amenities
- Busca hoteles con filtros opcionales de nombre, categoría, domicilio y amenity. Devuelve resultados paginados.
- Actualizar hotel: Actualiza los datos editables de un hotel existente: categoría, teléfono y correo de contacto. El nombre, CUIT, domicilio y coordenadas no se pueden modificar.
- Cerrar hotel
- Agregar amenities: Agrega una o varias amenities a un hotel existente
- Elimina una amenity específica de un hotel

## Tarifas
Gestión de tarifas.
El sistema debe proveer las siguientes funcionalidades:
- Crea una tarifa normal vigente o una tarifa promocional según las fechas enviadas
- Buscar tarifa por ID
- Buscar tarifas: Retorna tarifas paginadas
- Eliminar tarifa

## Reservas
Gestión de reservas.
El sistema debe proveer las siguientes funcionalidades:
- Crea una nueva reserva para una habitación en fechas específicas.
- Buscar reserva por ID: Retorna los datos completos de una reserva específica incluyendo habitación, huésped, pagos y reviews.
- Buscar reservas por huésped: Retorna todas las reservas de un huésped específico con paginación. Incluye reservas en todos los estados.
- Actualizar estado de reserva: Cambia el estado de una reserva. Estados válidos: REALIZADA, CONFIRMADA (requiere pago >= 50%), EFECTUADA (cliente ya ingresó al hotel), FINALIZADA (requiere review y pago completo), ADEUDADA (finalizada sin pago completo), CANCELADA (solo si no tiene pagos), BLOQUEADA, CERRADA. Las transiciones de estado se validan según las reglas de negocio.
- Realizar check-in: Registra el ingreso del cliente al hotel. La reserva debe estar en estado CONFIRMADA y se actualiza a EFECTUADA. 
- Agregar pago a reserva: Registra un pago para la reserva. Si el total pagado alcanza el 50% o más del precio total, la reserva pasa automáticamente a estado CONFIRMADA. Si el pago completa el 100%, queda lista para finalizar.
- Agregar review del cliente: Permite al cliente (huésped) dejar una calificación y comentario sobre la habitación y el hotel. Solo se puede hacer después de la fecha de check-out. El review es obligatorio para finalizar la reserva.
- Agregar review del host: Permite al dueño del hotel dejar una calificación y comentario sobre el huésped. Solo se puede hacer después de la fecha de check-out.
- Cancela una reserva existente. Solo se puede cancelar si la reserva no tiene pagos registrados. Al cancelarse, la reserva se elimina de la lista de reservas de la habitación, liberándola para otras reservas.
- Buscar habitaciones disponibles: Busca habitaciones disponibles para un rango de fechas con filtros avanzados. Filtros disponibles: capacidad, rango de precio, categoría del hotel (1-5 estrellas), amenities (wifi, tv, piscina, etc. ), y búsqueda geoespacial por proximidad. Una habitación está disponible si no tiene reservas que se solapen con las fechas solicitadas. Todos los filtros son opcionales excepto las fechas de entrada y salida.

# Estilos y diseños
Seguir los estilos y diseños minimalistas definidos en docs-frontend.md
El diseño debe verse profesional siguiendo los estándares de la industria para SaaS.
El diseño debe usar los componentes shadcn siguiendo el diseño "Vega" (Lucide - Inter).
Las etiquetas deben estar en español.