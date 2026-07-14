import api from "./api";
import type {
  PageResponse,
  Usuario,
  Huesped,
  HuespedCreateRequest,
  HuespedUpdateRequest,
  Propietario,
  PropietarioCreateRequest,
  PropietarioUpdateRequest,
  TarjetaCredito,
  TarjetaCreditoCreateRequest,
  Banco,
} from "@/types/usuario";

export const usuarioService = {
  // --- Búsquedas ---
  buscarPorNombre: (nombre: string, page = 0, size = 10) =>
    api
      .get<PageResponse<Usuario>>("/users/buscar-nombre", {
        params: { nombre, page, size },
      })
      .then((r) => r.data),

  buscarPorDniExacto: (dni: string) =>
    api
      .get<Usuario>(`/users/dni/${dni}`)
      .then((r) => r.data),

  buscarPorDni: (dni: string, page = 0, size = 10) =>
    api
      .get<PageResponse<Usuario>>("/users/buscar-dni", {
        params: { dni, page, size },
      })
      .then((r) => r.data),

  // --- Huéspedes ---
  crearHuesped: (data: HuespedCreateRequest) =>
    api
      .post<Huesped>("/users/huesped", data)
      .then((r) => r.data),

  actualizarHuesped: (id: number, data: HuespedUpdateRequest) =>
    api
      .put<Huesped>(`/users/huesped/${id}`, data)
      .then((r) => r.data),

  eliminarHuesped: (id: number) =>
    api.delete(`/users/huesped/${id}`),

  // --- Propietarios ---
  crearPropietario: (data: PropietarioCreateRequest) =>
    api
      .post<Propietario>("/users/propietario", data)
      .then((r) => r.data),

  actualizarPropietario: (id: number, data: PropietarioUpdateRequest) =>
    api
      .put<Propietario>(`/users/propietario/${id}`, data)
      .then((r) => r.data),

  eliminarPropietario: (id: number) =>
    api.delete(`/users/propietario/${id}`),

  // --- Tarjetas de crédito ---
  listarTarjetas: (huespedId: number, page = 0, size = 10) =>
    api
      .get<PageResponse<TarjetaCredito>>(
        `/users/huespedes/${huespedId}/tarjetas`,
        { params: { page, size } }
      )
      .then((r) => r.data),

  agregarTarjeta: (huespedId: number, data: TarjetaCreditoCreateRequest) =>
    api
      .post<TarjetaCredito>(
        `/users/huespedes/${huespedId}/tarjetas`,
        data
      )
      .then((r) => r.data),

  eliminarTarjeta: (huespedId: number, tarjetaId: number) =>
    api.delete(`/users/huespedes/${huespedId}/tarjetas/${tarjetaId}`),

  cambiarTarjetaPrincipal: (huespedId: number, tarjetaId: number) =>
    api
      .patch<TarjetaCredito>(
        `/users/huespedes/${huespedId}/tarjetas/${tarjetaId}/principal`
      )
      .then((r) => r.data),

  // --- Bancos ---
  listarBancos: () =>
    api.get<Banco[]>("/users/bancos").then((r) => r.data),
};
