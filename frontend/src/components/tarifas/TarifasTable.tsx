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
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Trash2, Receipt } from "lucide-react";
import type { Tarifa } from "@/types/hotel";

interface TarifasTableProps {
  tarifas: Tarifa[];
  isLoading: boolean;
  totalPages: number;
  page: number;
  onPageChange: (page: number) => void;
  onDelete: (tarifa: Tarifa) => void;
  isDeleting?: boolean;
}

function getEstado(tarifa: Tarifa): { label: string; variant: "default" | "secondary" | "outline" | "destructive" } {
  const today = new Date().toISOString().split("T")[0];
  if (!tarifa.fechaInicio && !tarifa.fechaFin) {
    return { label: "Vigente", variant: "default" };
  }
  if (tarifa.fechaInicio && tarifa.fechaFin) {
    if (tarifa.fechaInicio <= today && tarifa.fechaFin >= today) {
      return { label: "Promocional", variant: "outline" };
    }
    if (tarifa.fechaFin < today) {
      return { label: "Expirada", variant: "secondary" };
    }
    return { label: "Próxima", variant: "secondary" };
  }
  return { label: "Vigente", variant: "default" };
}

export function TarifasTable({
  tarifas,
  isLoading,
  totalPages,
  page,
  onPageChange,
  onDelete,
  isDeleting,
}: TarifasTableProps) {
  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner />
      </div>
    );
  }

  if (tarifas.length === 0) {
    return (
      <div className="py-12 text-center">
        <Receipt className="mx-auto mb-3 size-10 text-muted-foreground/40" />
        <p className="text-sm text-muted-foreground">No se encontraron tarifas</p>
      </div>
    );
  }

  return (
    <>
      <div className="rounded-xl border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[50px]">ID</TableHead>
              <TableHead>Tipo Habitación</TableHead>
              <TableHead className="text-right">Precio/Noche</TableHead>
              <TableHead>Fecha Inicio</TableHead>
              <TableHead>Fecha Fin</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead className="w-[80px]">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {tarifas.map((t) => {
              const estado = getEstado(t);
              return (
                <TableRow key={t.id}>
                  <TableCell>
                    <Badge variant="secondary">{t.id}</Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">{t.tipoHabitacion.nombre}</Badge>
                  </TableCell>
                  <TableCell className="text-right font-medium">
                    ${t.precioNoche.toLocaleString("es-AR")}{" "}
                    <span className="text-xs font-normal text-muted-foreground">/noche</span>
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {t.fechaInicio ?? "—"}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {t.fechaFin ?? "—"}
                  </TableCell>
                  <TableCell>
                    <Badge variant={estado.variant}>{estado.label}</Badge>
                  </TableCell>
                  <TableCell>
                    <Button
                      variant="ghost"
                      size="icon-sm"
                      onClick={() => onDelete(t)}
                      disabled={isDeleting}
                      aria-label="Eliminar tarifa"
                      title="Eliminar"
                    >
                      <Trash2 className="size-3 text-destructive" />
                    </Button>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>

      {totalPages > 1 && (
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                text="Anterior"
                onClick={() => onPageChange(Math.max(0, page - 1))}
                aria-disabled={page === 0}
                className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
              />
            </PaginationItem>
            {Array.from({ length: totalPages }, (_, i) => (
              <PaginationItem key={i}>
                <PaginationLink
                  isActive={i === page}
                  onClick={() => onPageChange(i)}
                  className="cursor-pointer"
                >
                  {i + 1}
                </PaginationLink>
              </PaginationItem>
            ))}
            <PaginationItem>
              <PaginationNext
                text="Siguiente"
                onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
                aria-disabled={page >= totalPages - 1}
                className={page >= totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}
    </>
  );
}
