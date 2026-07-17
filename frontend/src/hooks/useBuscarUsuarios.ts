import { useBuscarPorNombre, useBuscarPorDni, useListarUsuarios } from "./useUsuarios";

export function useBuscarUsuarios(search: string) {
  const isDniSearch = /^\d+$/.test(search) && search.length >= 7;

  const { data: nombreResult, isLoading: nombreLoading } = useBuscarPorNombre(
    isDniSearch || search.length === 0 ? "" : search
  );
  const { data: dniResult, isLoading: dniLoading } = useBuscarPorDni(
    isDniSearch ? search : ""
  );
  const { data: allResult, isLoading: allLoading } = useListarUsuarios();

  const isLoading = search.length === 0 ? allLoading : (isDniSearch ? dniLoading : nombreLoading);
  const data = search.length === 0 ? allResult : (isDniSearch ? dniResult : nombreResult);

  return { data, isLoading };
}
