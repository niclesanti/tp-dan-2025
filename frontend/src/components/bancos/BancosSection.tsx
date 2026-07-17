import { useState, useMemo } from "react";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useBancos, useCrearBanco, useActualizarBanco, useEliminarBanco } from "@/hooks/useBancos";
import { UsuariosSearchBar } from "@/components/usuarios/UsuariosSearchBar";
import { BancosTable } from "./BancosTable";
import { BancoFormDialog } from "./BancoFormDialog";
import { DeleteConfirmDialog } from "@/components/usuarios/DeleteConfirmDialog";
import type { Banco } from "@/types/usuario";
import type { BancoFormValues } from "@/lib/validators/banco";

export function BancosSection() {
  const [search, setSearch] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Banco | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Banco | null>(null);

  const { data: bancos = [], isLoading } = useBancos();
  const crearBanco = useCrearBanco();
  const actualizarBanco = useActualizarBanco();
  const eliminarBanco = useEliminarBanco();

  const filteredBancos = useMemo(
    () =>
      bancos.filter((b) =>
        b.nombre.toLowerCase().includes(search.toLowerCase())
      ),
    [bancos, search]
  );

  const handleCreate = (data: BancoFormValues) => {
    crearBanco.mutate(data, {
      onSuccess: () => setCreateOpen(false),
    });
  };

  const handleEdit = (data: BancoFormValues) => {
    if (!editTarget) return;
    actualizarBanco.mutate(
      { id: editTarget.id, data },
      { onSuccess: () => setEditTarget(null) }
    );
  };

  const handleDelete = () => {
    if (!deleteTarget) return;
    eliminarBanco.mutate(deleteTarget.id, {
      onSuccess: () => setDeleteTarget(null),
    });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div className="w-full max-w-sm">
          <UsuariosSearchBar onSearch={setSearch} placeholder="Buscar banco..." />
        </div>
        <Button onClick={() => setCreateOpen(true)}>Nuevo Banco</Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      ) : filteredBancos.length === 0 ? (
        <div className="py-12 text-center">
          <p className="text-sm text-muted-foreground">No se encontraron bancos</p>
        </div>
      ) : (
        <BancosTable
          bancos={filteredBancos}
          onEdit={setEditTarget}
          onDelete={setDeleteTarget}
        />
      )}

      <BancoFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onSubmit={handleCreate}
        isLoading={crearBanco.isPending}
      />

      <BancoFormDialog
        open={editTarget !== null}
        onOpenChange={(open) => { if (!open) setEditTarget(null); }}
        banco={editTarget}
        onSubmit={handleEdit}
        isLoading={actualizarBanco.isPending}
      />

      <DeleteConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Eliminar Banco"
        description={`¿Estás seguro de que deseas eliminar el banco "${deleteTarget?.nombre}"? Esta acción no se puede deshacer.`}
        onConfirm={handleDelete}
        isLoading={eliminarBanco.isPending}
      />
    </div>
  );
}
