import api from "./api";
import type {
  ReservaDTOResponse,
  ReservaDTORequest,
  PagoDTORequest,
  ReviewDTORequest,
  HabitacionDisponibleDTO,
  PageResponse,
} from "@/types/reserva";

const BASE_RESERVAS = "/reservas";

export const reservaService = {
  // --- Habitaciones disponibles ---

  buscarHabitacionesDisponibles: (params: {
    checkIn: string;
    checkOut: string;
    capacidad?: number;
    precioMin?: number;
    precioMax?: number;
    categoriaHotel?: number;
    page?: number;
    size?: number;
  }) =>
    api
      .get<PageResponse<HabitacionDisponibleDTO>>(
        `${BASE_RESERVAS}/habitaciones/disponibles`,
        { params }
      )
      .then((r) => r.data),

  // --- Reservas CRUD ---

  buscarReservasPorHuesped: (huespedId: string, params?: { page?: number; size?: number }) =>
    api
      .get<PageResponse<ReservaDTOResponse>>(
        `${BASE_RESERVAS}/reservas/huesped/${huespedId}`,
        { params }
      )
      .then((r) => r.data),

  obtenerReserva: (id: string) =>
    api
      .get<ReservaDTOResponse>(`${BASE_RESERVAS}/reservas/${id}`)
      .then((r) => r.data),

  crearReserva: (data: ReservaDTORequest) =>
    api
      .post<ReservaDTOResponse>(`${BASE_RESERVAS}/reservas`, data)
      .then((r) => r.data),

  actualizarEstado: (id: string, estado: string) =>
    api
      .patch<ReservaDTOResponse>(
        `${BASE_RESERVAS}/reservas/${id}/estado`,
        null,
        { params: { estado } }
      )
      .then((r) => r.data),

  checkIn: (id: string) =>
    api
      .post<ReservaDTOResponse>(`${BASE_RESERVAS}/reservas/${id}/check-in`)
      .then((r) => r.data),

  agregarPago: (id: string, data: PagoDTORequest) =>
    api
      .post<ReservaDTOResponse>(`${BASE_RESERVAS}/reservas/${id}/pagos`, data)
      .then((r) => r.data),

  reviewCliente: (id: string, data: ReviewDTORequest) =>
    api
      .post<ReservaDTOResponse>(
        `${BASE_RESERVAS}/reservas/${id}/reviews/cliente`,
        data
      )
      .then((r) => r.data),

  reviewHost: (id: string, data: ReviewDTORequest) =>
    api
      .post<ReservaDTOResponse>(
        `${BASE_RESERVAS}/reservas/${id}/reviews/host`,
        data
      )
      .then((r) => r.data),

  cancelarReserva: (id: string) =>
    api
      .delete(`${BASE_RESERVAS}/reservas/${id}`)
      .then((r) => r.status === 204 || r.data),
};
