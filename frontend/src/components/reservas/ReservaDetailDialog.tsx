import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
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
import { StarRating } from "@/components/hoteles/StarRating";
import {
  CreditCard,
  Star,
  LogIn,
  XCircle,
  User,
  Building2,
  Calendar,
  DollarSign,
} from "lucide-react";
import { useCancelarReserva, useCheckIn } from "@/hooks/useReservas";
import { EstadoBadge } from "./EstadoBadge";
import { PagoFormDialog } from "./PagoFormDialog";
import { ReviewFormDialog } from "./ReviewFormDialog";
import type { ReservaDTOResponse } from "@/types/reserva";

interface ReservaDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  reserva: ReservaDTOResponse | null;
}

export function ReservaDetailDialog({
  open,
  onOpenChange,
  reserva,
}: ReservaDetailDialogProps) {
  const [pagoOpen, setPagoOpen] = useState(false);
  const [reviewOpen, setReviewOpen] = useState(false);
  const [cancelConfirmOpen, setCancelConfirmOpen] = useState(false);
  const [checkInConfirmOpen, setCheckInConfirmOpen] = useState(false);

  const cancelarReserva = useCancelarReserva();
  const checkIn = useCheckIn();

  if (!reserva) return null;

  const totalPagado = reserva.pagos.reduce((acc, p) => acc + (p.amount?.precio ?? 0), 0);
  const montoPendiente = reserva.precioTotal - totalPagado;

  const handleCancel = () => {
    cancelarReserva.mutate(reserva.id, {
      onSuccess: () => {
        setCancelConfirmOpen(false);
        onOpenChange(false);
      },
    });
  };

  const handleCheckIn = () => {
    checkIn.mutate(reserva.id, {
      onSuccess: () => {
        setCheckInConfirmOpen(false);
      },
    });
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader className="flex-shrink-0">
            <DialogTitle className="flex items-center gap-2">
              Detalle de Reserva
              <EstadoBadge estado={reserva.estadoReserva} />
            </DialogTitle>
          </DialogHeader>

          <div className="flex-1 min-h-0 overflow-y-auto">
            <div className="pr-2 pb-4 space-y-5">
              {/* Info principal */}
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div className="space-y-3">
                  <div className="flex items-center gap-2 text-sm">
                    <User className="size-4 text-muted-foreground" />
                    <div>
                      <p className="text-xs text-muted-foreground">Huésped</p>
                      <p className="font-medium text-foreground">
                        {reserva.huesped.nombreApellido}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {reserva.huesped.email}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 text-sm">
                    <Building2 className="size-4 text-muted-foreground" />
                    <div>
                      <p className="text-xs text-muted-foreground">Hotel</p>
                      <p className="font-medium text-foreground">
                        Hotel #{reserva.hotelId}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Habitación {reserva.idHabitacion}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="space-y-3">
                  <div className="flex items-center gap-2 text-sm">
                    <Calendar className="size-4 text-muted-foreground" />
                    <div>
                      <p className="text-xs text-muted-foreground">Fechas</p>
                      <p className="font-medium text-foreground">
                        {new Date(reserva.checkIn).toLocaleDateString("es-AR")} →{" "}
                        {new Date(reserva.checkOut).toLocaleDateString("es-AR")}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 text-sm">
                    <DollarSign className="size-4 text-muted-foreground" />
                    <div>
                      <p className="text-xs text-muted-foreground">Precio</p>
                      <p className="font-medium text-foreground">
                        USD {reserva.precioNoche.toLocaleString("es-AR")} / noche
                      </p>
                      <p className="text-sm font-semibold text-foreground">
                        Total: USD {reserva.precioTotal.toLocaleString("es-AR")}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <Separator />

              {/* Pagos */}
              <div>
                <div className="mb-2 flex items-center justify-between">
                  <h3 className="text-sm font-medium text-foreground">Pagos</h3>
                  {["RESERVADA", "CONFIRMADA", "ADEUDADA"].includes(reserva.estadoReserva) && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPagoOpen(true)}
                    >
                      <CreditCard className="mr-1 size-3" />
                      Agregar Pago
                    </Button>
                  )}
                </div>

                {reserva.pagos.length === 0 ? (
                  <p className="text-xs text-muted-foreground">
                    No hay pagos registrados
                  </p>
                ) : (
                  <div className="space-y-2">
                    {reserva.pagos.map((pago, idx) => (
                      <div
                        key={idx}
                        className="flex items-center justify-between rounded-lg border border-border bg-muted/30 p-2.5"
                      >
                        <div className="flex items-center gap-2">
                          <CreditCard className="size-3 text-muted-foreground" />
                          <div>
                            <p className="text-xs font-medium text-foreground">
                              {pago.method} — {pago.transactionId}
                            </p>
                            <p className="text-xs text-muted-foreground">
                              {pago.status}
                            </p>
                          </div>
                        </div>
                        <span className="text-sm font-medium text-foreground">
                          USD {pago.amount?.precio?.toLocaleString("es-AR") ?? "—"}
                        </span>
                      </div>
                    ))}
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Total pagado:</span>
                      <span className="font-medium text-foreground">
                        USD {totalPagado.toLocaleString("es-AR")}
                      </span>
                    </div>
                    {montoPendiente > 0 && (
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Pendiente:</span>
                        <span className="font-medium text-yellow-400">
                          USD {montoPendiente.toLocaleString("es-AR")}
                        </span>
                      </div>
                    )}
                  </div>
                )}
              </div>

              <Separator />

              {/* Reviews */}
              <div>
                <div className="mb-2 flex items-center justify-between">
                  <h3 className="text-sm font-medium text-foreground">Reviews</h3>
                  {["EFECTUADA", "FINALIZADA"].includes(reserva.estadoReserva) &&
                    !reserva.clientReview && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setReviewOpen(true)}
                      >
                        <Star className="mr-1 size-3" />
                        Agregar Review
                      </Button>
                    )}
                </div>

                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="rounded-lg border border-border p-3">
                    <p className="mb-1 text-xs font-medium text-muted-foreground">
                      Review del cliente
                    </p>
                    {reserva.clientReview ? (
                      <div className="space-y-1">
                        <StarRating value={reserva.clientReview.rating} />
                        <p className="text-sm text-foreground">
                          {reserva.clientReview.comment}
                        </p>
                      </div>
                    ) : (
                      <p className="text-xs text-muted-foreground">
                        Sin review
                      </p>
                    )}
                  </div>

                  <div className="rounded-lg border border-border p-3">
                    <p className="mb-1 text-xs font-medium text-muted-foreground">
                      Review del host
                    </p>
                    {reserva.hostReview ? (
                      <div className="space-y-1">
                        <StarRating value={reserva.hostReview.rating} />
                        <p className="text-sm text-foreground">
                          {reserva.hostReview.comment}
                        </p>
                      </div>
                    ) : (
                      <p className="text-xs text-muted-foreground">
                        Sin review
                      </p>
                    )}
                  </div>
                </div>
              </div>

              <Separator />

              {/* Acciones */}
              <div className="flex flex-wrap gap-2">
                {reserva.estadoReserva === "CONFIRMADA" && (
                  <Button onClick={() => setCheckInConfirmOpen(true)} disabled={checkIn.isPending}>
                    <LogIn className="mr-1 size-4" />
                    Check-in
                  </Button>
                )}

                {reserva.estadoReserva === "RESERVADA" && (
                  <Button
                    variant="destructive"
                    onClick={() => setCancelConfirmOpen(true)}
                    disabled={cancelarReserva.isPending}
                  >
                    <XCircle className="mr-1 size-4" />
                    Cancelar Reserva
                  </Button>
                )}
              </div>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <PagoFormDialog
        open={pagoOpen}
        onOpenChange={setPagoOpen}
        reservaId={reserva.id}
        montoPendiente={montoPendiente}
      />

      <ReviewFormDialog
        open={reviewOpen}
        onOpenChange={setReviewOpen}
        reservaId={reserva.id}
      />

      <AlertDialog open={cancelConfirmOpen} onOpenChange={setCancelConfirmOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancelar Reserva</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Estás seguro de que deseas cancelar esta reserva? Esta acción es irreversible y la reserva se eliminará de la lista de la habitación.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={cancelarReserva.isPending}>No, mantener</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCancel}
              disabled={cancelarReserva.isPending}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {cancelarReserva.isPending ? "Cancelando..." : "Sí, cancelar reserva"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={checkInConfirmOpen} onOpenChange={setCheckInConfirmOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Realizar Check-in</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Confirmas el check-in para esta reserva? El cliente ingresará al hotel y la reserva cambiará a estado EFECTUADA.
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
    </>
  );
}
