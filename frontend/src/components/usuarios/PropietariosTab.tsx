import { useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useBuscarUsuarios } from "@/hooks/useBuscarUsuarios";
import { useCrearPropietario } from "@/hooks/usePropietarios";
import { UsuariosSearchBar } from "./UsuariosSearchBar";
import { PropietarioFormDialog } from "./PropietarioFormDialog";
import type { Propietario, UsuarioBusqueda } from "@/types/usuario";
import type { PropietarioFormValues } from "@/lib/validators/propietario";

export function PropietariosTab() {
  const [search, setSearch] = useState("");
  const [createOpen, setCreateOpen] = useState(false);

  const { data: searchResult, isLoading: searchLoading } = useBuscarUsuarios(search);

  const crearPropietario = useCrearPropietario();

  const allUsers: UsuarioBusqueda[] = searchResult?.content ?? [];
  const propietarios = allUsers.filter((u) => u.tipo === "PROPIETARIO") as Propietario[];

  const handleCreate = (data: PropietarioFormValues) => {
    crearPropietario.mutate(data, {
      onSuccess: () => setCreateOpen(false),
    });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div className="w-full max-w-sm">
          <UsuariosSearchBar onSearch={setSearch} />
        </div>
        <Button onClick={() => setCreateOpen(true)}>Nuevo Propietario</Button>
      </div>

      {searchLoading ? (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      ) : propietarios.length === 0 ? (
        <div className="py-12 text-center">
          <p className="text-sm text-muted-foreground">No se encontraron propietarios</p>
        </div>
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nombre</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>DNI</TableHead>
                <TableHead>Teléfono</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {propietarios.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-medium">{p.nombre}</TableCell>
                  <TableCell className="text-muted-foreground">{p.email}</TableCell>
                  <TableCell>
                    <Badge variant="secondary">{p.dni}</Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground">{p.telefono}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <PropietarioFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onSubmit={handleCreate}
        isLoading={crearPropietario.isPending}
      />
    </div>
  );
}
