import api from "./api";
import type {
  Hotel,
  HotelCreateRequest,
  HotelUpdateRequest,
  Habitacion,
  HabitacionCreateRequest,
  HabitacionUpdateRequest,
  TipoHabitacion,
  Tarifa,
  PageResponse,
  Amenity,
} from "@/types/hotel";

const BASE = "/gestion";

export const hotelService = {
  buscarHoteles: (params: {
    nombre?: string;
    categoria?: number;
    domicilio?: string;
    amenity?: Amenity;
    page?: number;
    size?: number;
  }) =>
    api.get<PageResponse<Hotel>>(`${BASE}/hoteles`, { params }).then((r) => r.data),

  buscarHotelPorId: (id: number) =>
    api.get<Hotel>(`${BASE}/hoteles/${id}`).then((r) => r.data),

  crearHotel: (data: HotelCreateRequest) =>
    api.post<Hotel>(`${BASE}/hoteles`, data).then((r) => r.data),

  actualizarHotel: (id: number, data: HotelUpdateRequest) =>
    api.put<Hotel>(`${BASE}/hoteles/${id}`, data).then((r) => r.data),

  cerrarHotel: (id: number) =>
    api.patch<Hotel>(`${BASE}/hoteles/${id}/cerrar`).then((r) => r.data),

  agregarAmenities: (id: number, amenities: Amenity[]) =>
    api.put<Hotel>(`${BASE}/hoteles/${id}/amenities`, amenities).then((r) => r.data),

  eliminarAmenity: (id: number, amenityId: number) =>
    api.delete(`${BASE}/hoteles/${id}/amenities/${amenityId}`),

  buscarHabitaciones: (params: {
    cantidadHuespedes?: number;
    idTipoHabitacion?: number;
    precioMinimo?: number;
    precioMaximo?: number;
    page?: number;
    size?: number;
  }) =>
    api.get<PageResponse<Habitacion>>(`${BASE}/habitaciones`, { params }).then((r) => r.data),

  buscarHabitacionPorId: (id: number) =>
    api.get<Habitacion>(`${BASE}/habitaciones/${id}`).then((r) => r.data),

  crearHabitacion: (data: HabitacionCreateRequest) =>
    api.post<Habitacion>(`${BASE}/habitaciones`, data).then((r) => r.data),

  actualizarHabitacion: (id: number, data: HabitacionUpdateRequest) =>
    api.put<Habitacion>(`${BASE}/habitaciones/${id}`, data).then((r) => r.data),

  eliminarHabitacion: (id: number) =>
    api.delete(`${BASE}/habitaciones/${id}`),

  obtenerTarifaVigente: (id: number) =>
    api.get<Tarifa>(`${BASE}/habitaciones/${id}/tarifa-vigente`).then((r) => r.data),

  listarTiposHabitacion: () =>
    api.get<TipoHabitacion[]>(`${BASE}/tipos-habitacion`).then((r) => r.data),
};
