import { useMutation, useQueryClient } from "@tanstack/react-query";
import { usuarioService } from "@/services/usuario.service";
import type { HuespedCreateRequest, HuespedUpdateRequest } from "@/types/usuario";
import { toast } from "sonner";

export function useCrearHuesped() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: HuespedCreateRequest) => usuarioService.crearHuesped(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Huésped creado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useActualizarHuesped() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: HuespedUpdateRequest }) =>
      usuarioService.actualizarHuesped(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Huésped actualizado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useEliminarHuesped() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => usuarioService.eliminarHuesped(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Huésped eliminado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}
