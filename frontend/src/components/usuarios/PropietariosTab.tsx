import { useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Spinner } from "@/components/ui/spinner";
import { Pencil, Trash2 } from "lucide-react";
import { useBuscarUsuarios } from "@/hooks/useBuscarUsuarios";
import { useEliminarPropietario, useCrearPropietario, useActualizarPropietario } from "@/hooks/usePropietarios";
import { UsuariosSearchBar } from "./UsuariosSearchBar";
import { PropietarioFormDialog } from "./PropietarioFormDialog";
import { DeleteConfirmDialog } from "./DeleteConfirmDialog";
import type { Propietario, UsuarioBusqueda } from "@/types/usuario";
import type { PropietarioFormValues, PropietarioUpdateFormValues } from "@/lib/validators/propietario";

export function PropietariosTab() {
  const [search, setSearch] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Propietario | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Propietario | null>(null);

  const { data: searchResult, isLoading: searchLoading } = useBuscarUsuarios(search);

  const crearPropietario = useCrearPropietario();
  const actualizarPropietario = useActualizarPropietario();
  const eliminarPropietario = useEliminarPropietario();

  const allUsers: UsuarioBusqueda[] = searchResult?.content ?? [];
  const propietarios = allUsers.filter((u) => u.tipo === "PROPIETARIO") as Propietario[];

  const handleCreate = (data: PropietarioFormValues | PropietarioUpdateFormValues) => {
    crearPropietario.mutate(data as PropietarioFormValues, {
      onSuccess: () => setCreateOpen(false),
    });
  };

  const handleEdit = (data: PropietarioFormValues | PropietarioUpdateFormValues) => {
    if (!editTarget) return;
    actualizarPropietario.mutate(
      { id: editTarget.id, data: data as PropietarioUpdateFormValues },
      { onSuccess: () => setEditTarget(null) }
    );
  };

  const handleDelete = () => {
    if (!deleteTarget) return;
    eliminarPropietario.mutate(deleteTarget.id, {
      onSuccess: () => setDeleteTarget(null),
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
                <TableHead>Banco</TableHead>
                <TableHead className="w-[100px]">Acciones</TableHead>
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
                  <TableCell className="text-muted-foreground">
                    {p.cuentaBancaria?.nombreBanco ?? "—"}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => setEditTarget(p)}
                        aria-label="Editar" title="Editar"
                      >
                        <Pencil className="size-3" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => setDeleteTarget(p)}
                        aria-label="Eliminar" title="Eliminar"
                      >
                        <Trash2 className="size-3 text-destructive" />
                      </Button>
                    </div>
                  </TableCell>
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

      <PropietarioFormDialog
        open={editTarget !== null}
        onOpenChange={(open) => { if (!open) setEditTarget(null); }}
        propietario={editTarget}
        onSubmit={handleEdit}
        isLoading={actualizarPropietario.isPending}
      />

      <DeleteConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Eliminar Propietario"
        description={`¿Estás seguro de que deseas eliminar a ${deleteTarget?.nombre}?`}
        onConfirm={handleDelete}
        isLoading={eliminarPropietario.isPending}
      />
    </div>
  );
}
