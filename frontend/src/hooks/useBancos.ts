import { useQuery } from "@tanstack/react-query";
import { usuarioService } from "@/services/usuario.service";

export function useBancos() {
  return useQuery({
    queryKey: ["bancos"],
    queryFn: () => usuarioService.listarBancos(),
    staleTime: 5 * 60 * 1000,
  });
}
