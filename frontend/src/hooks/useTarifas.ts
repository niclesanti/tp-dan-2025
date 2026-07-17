import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { tarifaService } from "@/services/tarifa.service";
import type { TarifaCreateRequest } from "@/types/hotel";
import { toast } from "sonner";

export function useBuscarTarifas(params: { page?: number; size?: number }) {
  return useQuery({
    queryKey: ["tarifas", params],
    queryFn: () => tarifaService.buscarTarifas(params),
    staleTime: 5 * 60 * 1000,
  });
}

export function useCrearTarifa() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: TarifaCreateRequest) => tarifaService.crearTarifa(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tarifas"] });
      toast.success("Tarifa creada exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useEliminarTarifa() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => tarifaService.eliminarTarifa(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tarifas"] });
      toast.success("Tarifa eliminada exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}
