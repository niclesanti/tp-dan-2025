import api from "./api";
import type { Tarifa, TarifaCreateRequest, PageResponse } from "@/types/hotel";

const BASE = "/gestion/tarifas";

export const tarifaService = {
  buscarTarifas: (params: { page?: number; size?: number }) =>
    api.get<PageResponse<Tarifa>>(BASE, { params }).then((r) => r.data),

  buscarTarifaPorId: (id: number) =>
    api.get<Tarifa>(`${BASE}/${id}`).then((r) => r.data),

  crearTarifa: (data: TarifaCreateRequest) =>
    api.post<Tarifa>(BASE, data).then((r) => r.data),

  eliminarTarifa: (id: number) =>
    api.delete(`${BASE}/${id}`),
};
