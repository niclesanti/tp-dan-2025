import { useState, useMemo } from "react";
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
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  CalendarCheck,
  Eye,
  LogIn,
  XCircle,
} from "lucide-react";
import { useReservasPorHuesped, useCancelarReserva, useCheckIn } from "@/hooks/useReservas";
import { EstadoBadge } from "./EstadoBadge";
import { ReservaDetailDialog } from "./ReservaDetailDialog";
import type { ReservaDTOResponse } from "@/types/reserva";

export function GestionReservasTab() {
  const [huespedId, setHuespedId] = useState("");
  const [searchId, setSearchId] = useState("");
  const [page, setPage] = useState(0);
  const [detailTarget, setDetailTarget] = useState<ReservaDTOResponse | null>(null);
  const [cancelTarget, setCancelTarget] = useState<ReservaDTOResponse | null>(null);
  const [checkInTarget, setCheckInTarget] = useState<ReservaDTOResponse | null>(null);

  const params = useMemo(
    () => ({ page, size: 10 }),
    [page]
  );

  const { data: result, isLoading } = useReservasPorHuesped(searchId, params);
  const cancelarReserva = useCancelarReserva();
  const checkIn = useCheckIn();

  const reservas = result?.content ?? [];
  const totalPages = result?.totalPages ?? 0;

  const handleSearch = () => {
    setSearchId(huespedId.trim());
    setPage(0);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") handleSearch();
  };

  const handleCancel = () => {
    if (!cancelTarget) return;
    cancelarReserva.mutate(cancelTarget.id, {
      onSuccess: () => setCancelTarget(null),
    });
  };

  const handleCheckIn = () => {
    if (!checkInTarget) return;
    checkIn.mutate(checkInTarget.id, {
      onSuccess: () => setCheckInTarget(null),
    });
  };

  return (
    <div className="space-y-4">
      {/* Barra de búsqueda */}
      <div className="flex items-center gap-2">
        <Input
          placeholder="ID del huésped..."
          value={huespedId}
          onChange={(e) => setHuespedId(e.target.value)}
          onKeyDown={handleKeyDown}
          className="max-w-xs"
        />
        <Button onClick={handleSearch} disabled={!huespedId.trim()}>
          Buscar
        </Button>
      </div>

      {/* Resultados */}
      {!searchId ? (
        <div className="py-16 text-center">
          <CalendarCheck className="mx-auto mb-3 size-12 text-muted-foreground/30" />
          <p className="text-sm text-muted-foreground">
            Ingresá el ID de un huésped para ver sus reservas
          </p>
        </div>
      ) : isLoading ? (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      ) : reservas.length === 0 ? (
        <div className="py-12 text-center">
          <CalendarCheck className="mx-auto mb-3 size-10 text-muted-foreground/40" />
          <p className="text-sm text-muted-foreground">
            No se encontraron reservas para el huésped "{searchId}"
          </p>
        </div>
      ) : (
        <>
          <div className="rounded-xl border border-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[80px]">ID</TableHead>
                  <TableHead>Hotel</TableHead>
                  <TableHead>Habitación</TableHead>
                  <TableHead>Check-in</TableHead>
                  <TableHead>Check-out</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Total</TableHead>
                  <TableHead className="w-[180px]">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {reservas.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>
                      <Badge variant="secondary" className="font-mono text-xs">
                        {r.id.slice(0, 8)}...
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      Hotel #{r.hotelId}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      Hab. {r.idHabitacion}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {new Date(r.checkIn).toLocaleDateString("es-AR")}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {new Date(r.checkOut).toLocaleDateString("es-AR")}
                    </TableCell>
                    <TableCell>
                      <EstadoBadge estado={r.estadoReserva} />
                    </TableCell>
                    <TableCell className="font-medium">
                      USD {r.precioTotal.toLocaleString("es-AR")}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <Button
                          variant="ghost"
                          size="icon-sm"
                          onClick={() => setDetailTarget(r)}
                          aria-label="Ver detalles"
                          title="Ver detalles"
                        >
                          <Eye className="size-3" />
                        </Button>

                        {r.estadoReserva === "CONFIRMADA" && (
                          <Button
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => setCheckInTarget(r)}
                            disabled={checkIn.isPending}
                            aria-label="Check-in"
                            title="Check-in"
                          >
                            <LogIn className="size-3 text-emerald-500" />
                          </Button>
                        )}

                        {r.estadoReserva === "RESERVADA" && (
                          <Button
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => setCancelTarget(r)}
                            disabled={cancelarReserva.isPending}
                            aria-label="Cancelar reserva"
                            title="Cancelar reserva"
                          >
                            <XCircle className="size-3 text-destructive" />
                          </Button>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {totalPages > 1 && (
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    text="Anterior"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    aria-disabled={page === 0}
                    className={
                      page === 0
                        ? "pointer-events-none opacity-50"
                        : "cursor-pointer"
                    }
                  />
                </PaginationItem>
                {Array.from({ length: totalPages }, (_, i) => (
                  <PaginationItem key={i}>
                    <PaginationLink
                      isActive={i === page}
                      onClick={() => setPage(i)}
                      className="cursor-pointer"
                    >
                      {i + 1}
                    </PaginationLink>
                  </PaginationItem>
                ))}
                <PaginationItem>
                  <PaginationNext
                    text="Siguiente"
                    onClick={() =>
                      setPage((p) => Math.min(totalPages - 1, p + 1))
                    }
                    aria-disabled={page >= totalPages - 1}
                    className={
                      page >= totalPages - 1
                        ? "pointer-events-none opacity-50"
                        : "cursor-pointer"
                    }
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          )}
        </>
      )}

      <ReservaDetailDialog
        open={detailTarget !== null}
        onOpenChange={(open) => {
          if (!open) setDetailTarget(null);
        }}
        reserva={detailTarget}
      />

      <AlertDialog open={cancelTarget !== null} onOpenChange={(open) => { if (!open) setCancelTarget(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancelar Reserva</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Estás seguro de que deseas cancelar la reserva <strong>{cancelTarget?.id.slice(0, 8)}...</strong>?
              Esta acción es irreversible y la reserva se eliminará de la lista de la habitación.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={cancelarReserva.isPending}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCancel}
              disabled={cancelarReserva.isPending}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {cancelarReserva.isPending ? "Cancelando..." : "Confirmar Cancelación"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={checkInTarget !== null} onOpenChange={(open) => { if (!open) setCheckInTarget(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Realizar Check-in</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Confirmas el check-in para la reserva <strong>{checkInTarget?.id.slice(0, 8)}...</strong>?
              El cliente ingresará al hotel y la reserva cambiará a estado EFECTUADA.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={checkIn.isPending}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCheckIn}
              disabled={checkIn.isPending}
              className="bg-emerald-600 text-white hover:bg-emerald-700"
            >
              {checkIn.isPending ? "Procesando..." : "Confirmar Check-in"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
