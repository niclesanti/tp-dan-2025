import { useQuery } from "@tanstack/react-query";
import { usuarioService } from "@/services/usuario.service";


export function useBuscarPorNombre(nombre: string, page = 0, size = 10) {
  return useQuery({
    queryKey: ["usuarios", "nombre", nombre, page, size],
    queryFn: () => usuarioService.buscarPorNombre(nombre, page, size),
    enabled: nombre.length > 0,
    staleTime: 30_000,
  });
}

export function useBuscarPorDni(dni: string, page = 0, size = 10) {
  return useQuery({
    queryKey: ["usuarios", "dni", dni, page, size],
    queryFn: () => usuarioService.buscarPorDni(dni, page, size),
    enabled: dni.length >= 7,
    staleTime: 30_000,
  });
}

export function useUsuarioPorDniExacto(dni: string) {
  return useQuery({
    queryKey: ["usuarios", "dni-exacto", dni],
    queryFn: () => usuarioService.buscarPorDniExacto(dni),
    enabled: dni.length >= 7,
    staleTime: 30_000,
  });
}

export function useListarUsuarios(page = 0, size = 10) {
  return useQuery({
    queryKey: ["usuarios", "listar", page, size],
    queryFn: () => usuarioService.buscarPorNombre("", page, size),
    staleTime: 30_000,
  });
}
