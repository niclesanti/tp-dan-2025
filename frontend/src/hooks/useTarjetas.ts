import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { usuarioService } from "@/services/usuario.service";
import type { TarjetaCreditoCreateRequest } from "@/types/usuario";
import { toast } from "sonner";

export function useListarTarjetas(huespedId: number, enabled = true) {
  return useQuery({
    queryKey: ["tarjetas", huespedId],
    queryFn: () => usuarioService.listarTarjetas(huespedId),
    enabled,
    staleTime: 30_000,
  });
}

export function useAgregarTarjeta() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      huespedId,
      data,
    }: {
      huespedId: number;
      data: TarjetaCreditoCreateRequest;
    }) => usuarioService.agregarTarjeta(huespedId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tarjetas"] });
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Tarjeta agregada exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useEliminarTarjeta() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      huespedId,
      tarjetaId,
    }: {
      huespedId: number;
      tarjetaId: number;
    }) => usuarioService.eliminarTarjeta(huespedId, tarjetaId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tarjetas"] });
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Tarjeta eliminada exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useCambiarPrincipal() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      huespedId,
      tarjetaId,
    }: {
      huespedId: number;
      tarjetaId: number;
    }) => usuarioService.cambiarTarjetaPrincipal(huespedId, tarjetaId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tarjetas"] });
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Tarjeta principal cambiada");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}
