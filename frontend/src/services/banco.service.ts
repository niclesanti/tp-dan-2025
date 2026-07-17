import api from "./api";
import type { Banco, BancoCreateRequest, BancoUpdateRequest } from "@/types/usuario";

export const bancoService = {
  listar: () =>
    api.get<Banco[]>("/users/bancos").then((r) => r.data),

  crear: (data: BancoCreateRequest) =>
    api.post<Banco>("/users/bancos", data).then((r) => r.data),

  actualizar: (id: number, data: BancoUpdateRequest) =>
    api.put<Banco>(`/users/bancos/${id}`, data).then((r) => r.data),

  eliminar: (id: number) =>
    api.delete(`/users/bancos/${id}`),
};
