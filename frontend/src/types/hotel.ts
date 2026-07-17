export type Amenity =
  | "PILETA"
  | "SAUNA"
  | "GIMNASIO"
  | "RESTAURANTE"
  | "BAR"
  | "ESTACIONAMIENTO"
  | "WIFI"
  | "AIRE_ACONDICIONADO"
  | "CALENTADOR"
  | "TV_CABLE"
  | "SERVICIO_HABITACIONES"
  | "LIMPIEZA_DIARIA"
  | "PISCINA_CUBIERTA"
  | "PISCINA_DESCUBIERTA"
  | "SPA"
  | "SALA_JUEGOS"
  | "SALA_REUNIONES"
  | "TRANSPORTE_AEROPUERTO";

export const AMENITY_LABELS: Record<Amenity, string> = {
  PILETA: "Pileta",
  SAUNA: "Sauna",
  GIMNASIO: "Gimnasio",
  RESTAURANTE: "Restaurante",
  BAR: "Bar",
  ESTACIONAMIENTO: "Estacionamiento",
  WIFI: "WiFi",
  AIRE_ACONDICIONADO: "Aire Acondicionado",
  CALENTADOR: "Calentador",
  TV_CABLE: "TV Cable",
  SERVICIO_HABITACIONES: "Serv. Habitaciones",
  LIMPIEZA_DIARIA: "Limpieza Diaria",
  PISCINA_CUBIERTA: "Piscina Cubierta",
  PISCINA_DESCUBIERTA: "Piscina Descubierta",
  SPA: "Spa",
  SALA_JUEGOS: "Sala de Juegos",
  SALA_REUNIONES: "Sala de Reuniones",
  TRANSPORTE_AEROPUERTO: "Transporte Aeropuerto",
};

export interface AmenityHotel {
  id: number;
  amenity: Amenity;
}

export interface Hotel {
  id: number;
  nombre: string;
  cuit: string;
  domicilio: string;
  latitud: number | null;
  longitud: number | null;
  telefono: string;
  correoContacto: string;
  categoria: number;
  fechaCierre: string | null;
  amenities: AmenityHotel[];
}

export interface HotelCreateRequest {
  nombre: string;
  cuit: string;
  domicilio: string;
  latitud: number | null;
  longitud: number | null;
  telefono: string;
  correoContacto: string;
  categoria: number;
}

export interface HotelUpdateRequest {
  categoria: number;
  telefono: string;
  correoContacto: string;
}

export interface TipoHabitacion {
  id: number;
  nombre: string;
  descripcion: string;
  capacidad: number;
}

export interface Habitacion {
  id: number;
  numero: number;
  piso: number;
  tipoHabitacion: TipoHabitacion;
  idHotel: number;
  nombreHotel: string;
}

export interface HabitacionCreateRequest {
  numero: number;
  piso: number;
  idTipoHabitacion: number;
  idHotel: number;
}

export interface HabitacionUpdateRequest {
  numero: number;
  piso: number;
  idTipoHabitacion: number;
}

export interface Tarifa {
  id: number;
  fechaInicio: string | null;
  fechaFin: string | null;
  tipoHabitacion: TipoHabitacion;
  precioNoche: number;
}

export interface TarifaCreateRequest {
  fechaInicio?: string | null;
  fechaFin?: string | null;
  idTipoHabitacion: number;
  precioNoche: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
