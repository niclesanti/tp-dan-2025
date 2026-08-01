import { useMutation, useQueryClient } from "@tanstack/react-query";
import { usuarioService } from "@/services/usuario.service";
import type { PropietarioCreateRequest } from "@/types/usuario";
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
