import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { bancoService } from "@/services/banco.service";
import type { BancoCreateRequest, BancoUpdateRequest } from "@/types/usuario";
import { toast } from "sonner";

export function useBancos() {
  return useQuery({
    queryKey: ["bancos"],
    queryFn: () => bancoService.listar(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useCrearBanco() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: BancoCreateRequest) => bancoService.crear(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bancos"] });
      toast.success("Banco creado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useActualizarBanco() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: BancoUpdateRequest }) =>
      bancoService.actualizar(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bancos"] });
      toast.success("Banco actualizado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useEliminarBanco() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => bancoService.eliminar(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bancos"] });
      toast.success("Banco eliminado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}
