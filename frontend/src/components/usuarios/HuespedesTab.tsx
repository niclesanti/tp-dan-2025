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
import { Pencil, Trash2, CreditCard, ChevronDown, ChevronUp } from "lucide-react";
import { useBuscarUsuarios } from "@/hooks/useBuscarUsuarios";
import { useEliminarHuesped, useCrearHuesped, useActualizarHuesped } from "@/hooks/useHuespedes";
import { UsuariosSearchBar } from "./UsuariosSearchBar";
import { HuespedFormDialog } from "./HuespedFormDialog";
import { DeleteConfirmDialog } from "./DeleteConfirmDialog";
import { TarjetasSection } from "./TarjetasSection";
import type { Huesped, UsuarioBusqueda } from "@/types/usuario";
import type { HuespedFormValues, HuespedUpdateFormValues } from "@/lib/validators/huesped";

export function HuespedesTab() {
  const [search, setSearch] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Huesped | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Huesped | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const { data: searchResult, isLoading: searchLoading } = useBuscarUsuarios(search);

  const crearHuesped = useCrearHuesped();
  const actualizarHuesped = useActualizarHuesped();
  const eliminarHuesped = useEliminarHuesped();

  const allUsers: UsuarioBusqueda[] = searchResult?.content ?? [];
  const huespedes = allUsers.filter((u) => u.tipo === "HUESPED") as Huesped[];

  const handleCreate = (data: HuespedFormValues | HuespedUpdateFormValues) => {
    crearHuesped.mutate(data as HuespedFormValues, {
      onSuccess: () => setCreateOpen(false),
    });
  };

  const handleEdit = (data: HuespedFormValues | HuespedUpdateFormValues) => {
    if (!editTarget) return;
    actualizarHuesped.mutate(
      { id: editTarget.id, data: data as HuespedUpdateFormValues },
      { onSuccess: () => setEditTarget(null) }
    );
  };

  const handleDelete = () => {
    if (!deleteTarget) return;
    eliminarHuesped.mutate(deleteTarget.id, {
      onSuccess: () => setDeleteTarget(null),
    });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div className="w-full max-w-sm">
          <UsuariosSearchBar onSearch={setSearch} />
        </div>
        <Button onClick={() => setCreateOpen(true)}>Nuevo Huésped</Button>
      </div>

      {searchLoading ? (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      ) : huespedes.length === 0 ? (
        <div className="py-12 text-center">
          <p className="text-sm text-muted-foreground">No se encontraron huéspedes</p>
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
                <TableHead className="w-[100px]">Acciones</TableHead>
                <TableHead className="w-[40px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {huespedes.map((h) => (
                <TableRow key={h.id}>
                  <TableCell className="font-medium">{h.nombre}</TableCell>
                  <TableCell className="text-muted-foreground">{h.email}</TableCell>
                  <TableCell>
                    <Badge variant="secondary">{h.dni}</Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground">{h.telefono}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => setEditTarget(h)}
                        aria-label="Editar" title="Editar"
                      >
                        <Pencil className="size-3" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => setDeleteTarget(h)}
                        aria-label="Eliminar" title="Eliminar"
                      >
                        <Trash2 className="size-3 text-destructive" />
                      </Button>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Button
                      variant="ghost"
                      size="icon-sm"
                      onClick={() =>
                        setExpandedId(expandedId === h.id ? null : h.id)
                      }
                      aria-label="Ver tarjetas" title="Tarjetas"
                    >
                      <CreditCard className="size-3" />
                      {expandedId === h.id ? (
                        <ChevronUp className="size-3" />
                      ) : (
                        <ChevronDown className="size-3" />
                      )}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {expandedId !== null && huespedes.some((h) => h.id === expandedId) && (
                <TableRow key={`${expandedId}-tarjetas`}>
                  <TableCell colSpan={6} className="bg-muted/30 p-4">
                    <TarjetasSection huespedId={expandedId} />
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      )}

      <HuespedFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onSubmit={handleCreate}
        isLoading={crearHuesped.isPending}
      />

      <HuespedFormDialog
        open={editTarget !== null}
        onOpenChange={(open) => { if (!open) setEditTarget(null); }}
        huesped={editTarget}
        onSubmit={handleEdit}
        isLoading={actualizarHuesped.isPending}
      />

      <DeleteConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Eliminar Huésped"
        description={`¿Estás seguro de que deseas eliminar a ${deleteTarget?.nombre}? Esta acción eliminará también todas sus tarjetas de crédito.`}
        onConfirm={handleDelete}
        isLoading={eliminarHuesped.isPending}
      />
    </div>
  );
}
