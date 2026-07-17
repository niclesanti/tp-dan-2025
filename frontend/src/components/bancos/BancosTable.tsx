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
import { Pencil, Trash2 } from "lucide-react";
import type { Banco } from "@/types/usuario";

interface BancosTableProps {
  bancos: Banco[];
  onEdit: (banco: Banco) => void;
  onDelete: (banco: Banco) => void;
}

export function BancosTable({ bancos, onEdit, onDelete }: BancosTableProps) {
  return (
    <div className="rounded-xl border border-border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[80px]">ID</TableHead>
            <TableHead>Nombre</TableHead>
            <TableHead className="w-[100px]">Acciones</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {bancos.map((b) => (
            <TableRow key={b.id}>
              <TableCell>
                <Badge variant="secondary">{b.id}</Badge>
              </TableCell>
              <TableCell className="font-medium">{b.nombre}</TableCell>
              <TableCell>
                <div className="flex items-center gap-1">
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    onClick={() => onEdit(b)}
                    aria-label="Editar"
                    title="Editar"
                  >
                    <Pencil className="size-3" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    onClick={() => onDelete(b)}
                    aria-label="Eliminar"
                    title="Eliminar"
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
  );
}
