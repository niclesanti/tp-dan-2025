// --- Enums ---

export type EstadoReserva =
  | "RESERVADA"
  | "CONFIRMADA"
  | "EFECTUADA"
  | "FINALIZADA"
  | "CANCELADA"
  | "BLOQUEADA"
  | "CERRADA"
  | "ADEUDADA";

export const ESTADO_RESERVA_LABELS: Record<EstadoReserva, string> = {
  RESERVADA: "Reservada",
  CONFIRMADA: "Confirmada",
  EFECTUADA: "Efectuada",
  FINALIZADA: "Finalizada",
  CANCELADA: "Cancelada",
  BLOQUEADA: "Bloqueada",
  CERRADA: "Cerrada",
  ADEUDADA: "Adeudada",
};

// --- Reservas ---

export interface HuespedReserva {
  idUsuario: string;
  nombreApellido: string;
  email: string;
}

export interface TarifaReserva {
  precio: number;
  moneda: string;
}

export interface Pago {
  method: string;
  transactionId: string;
  amount: TarifaReserva;
  status: string;
}

export interface Review {
  rating: number;
  comment: string;
  createdAt: string;
}

export interface ReservaDTOResponse {
  id: string;
  idHabitacion: string;
  hotelId: number;
  createdAt: string;
  checkIn: string;
  checkOut: string;
  precioNoche: number;
  precioTotal: number;
  huesped: HuespedReserva;
  pagos: Pago[];
  clientReview: Review | null;
  hostReview: Review | null;
  estadoReserva: EstadoReserva;
}

export interface ReservaDTORequest {
  idHabitacion: string;
  checkIn: string;
  checkOut: string;
  huesped: HuespedReserva;
}

export interface PagoDTORequest {
  method: string;
  transactionId: string;
  amount: number;
  currency: string;
}

export interface ReviewDTORequest {
  rating: number;
  comment: string;
}

// --- Habitaciones Disponibles ---

export interface HotelSimpleDTO {
  id: number;
  nombre: string;
  categoria: number;
  domicilio: string;
}

export interface HabitacionDisponibleDTO {
  id: string;
  habitacionId: number;
  capacidad: number;
  precioNoche: number;
  tipoHabitacion: string;
  hotel: HotelSimpleDTO;
}

// --- Paginación ---

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
