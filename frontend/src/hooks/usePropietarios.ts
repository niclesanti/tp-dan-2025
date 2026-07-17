import { useMutation, useQueryClient } from "@tanstack/react-query";
import { usuarioService } from "@/services/usuario.service";
import type { PropietarioCreateRequest, PropietarioUpdateRequest } from "@/types/usuario";
import { toast } from "sonner";

export function useCrearPropietario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: PropietarioCreateRequest) =>
      usuarioService.crearPropietario(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Propietario creado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useActualizarPropietario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: PropietarioUpdateRequest }) =>
      usuarioService.actualizarPropietario(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Propietario actualizado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}

export function useEliminarPropietario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => usuarioService.eliminarPropietario(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["usuarios"] });
      toast.success("Propietario eliminado exitosamente");
    },
    onError: (err: Error) => {
      toast.error(err.message);
    },
  });
}
