import { useEffect, useId } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field, FieldLabel, FieldError, FieldGroup } from "@/components/ui/field";
import { Spinner } from "@/components/ui/spinner";
import { Badge } from "@/components/ui/badge";
import { crearReservaSchema } from "@/lib/validators/reserva";
import type { CrearReservaFormValues } from "@/lib/validators/reserva";
import { useCrearReserva } from "@/hooks/useReservas";
import type { HabitacionDisponibleDTO } from "@/types/reserva";

interface CrearReservaDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  habitacion: HabitacionDisponibleDTO | null;
  checkInDefault?: string;
  checkOutDefault?: string;
}

export function CrearReservaDialog({
  open,
  onOpenChange,
  habitacion,
  checkInDefault,
  checkOutDefault,
}: CrearReservaDialogProps) {
  const formId = useId();
  const crearReserva = useCrearReserva();

  const form = useForm<CrearReservaFormValues>({
    resolver: zodResolver(crearReservaSchema),
    defaultValues: {
      nombreApellido: "",
      email: "",
      dni: "",
      checkIn: checkInDefault ?? "",
      checkOut: checkOutDefault ?? "",
    },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        nombreApellido: "",
        email: "",
        dni: "",
        checkIn: checkInDefault ?? "",
        checkOut: checkOutDefault ?? "",
      });
    }
  }, [open, checkInDefault, checkOutDefault, form]);

  const handleSubmit = form.handleSubmit((data) => {
    if (!habitacion) return;

    crearReserva.mutate(
      {
        idHabitacion: habitacion.id,
        checkIn: new Date(data.checkIn).toISOString(),
        checkOut: new Date(data.checkOut).toISOString(),
        huesped: {
          nombreApellido: data.nombreApellido,
          email: data.email,
          dni: data.dni,
        },
      },
      {
        onSuccess: () => onOpenChange(false),
      }
    );
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>Crear Reserva</DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
            {habitacion && (
              <div className="mb-4 rounded-lg border border-border bg-muted/30 p-3">
                <p className="text-xs font-medium text-muted-foreground">
                  Habitación seleccionada
                </p>
                <div className="mt-1 flex items-center gap-2">
                  <span className="text-sm font-medium text-foreground">
                    {habitacion.hotel.nombre}
                  </span>
                  <Badge variant="outline">{habitacion.tipoHabitacion}</Badge>
                </div>
                <p className="mt-1 text-sm font-medium text-foreground">
                  USD {habitacion.precioNoche?.toLocaleString("es-AR") ?? "—"} / noche
                </p>
              </div>
            )}

            <form
              id={formId}
              onSubmit={handleSubmit}
              className="space-y-4"
            >
              <FieldGroup>
                <Field>
                  <FieldLabel>Nombre completo del huésped *</FieldLabel>
                  <Input
                    {...form.register("nombreApellido")}
                    placeholder="Juan Pérez"
                  />
                  <FieldError errors={[form.formState.errors.nombreApellido]} />
                </Field>

                <Field>
                  <FieldLabel>Email del huésped *</FieldLabel>
                  <Input
                    {...form.register("email")}
                    type="email"
                    placeholder="juan@email.com"
                  />
                  <FieldError errors={[form.formState.errors.email]} />
                </Field>

                <Field>
                  <FieldLabel>DNI del huésped *</FieldLabel>
                  <Input
                    {...form.register("dni")}
                    inputMode="numeric"
                    placeholder="12345678"
                    maxLength={8}
                  />
                  <FieldError errors={[form.formState.errors.dni]} />
                </Field>

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <Field>
                    <FieldLabel>Check-in *</FieldLabel>
                    <Input
                      {...form.register("checkIn")}
                      type="date"
                      min={new Date().toISOString().split("T")[0]}
                    />
                    <FieldError errors={[form.formState.errors.checkIn]} />
                  </Field>

                  <Field>
                    <FieldLabel>Check-out *</FieldLabel>
                    <Input
                      {...form.register("checkOut")}
                      type="date"
                      min={form.watch("checkIn") || new Date().toISOString().split("T")[0]}
                    />
                    <FieldError errors={[form.formState.errors.checkOut]} />
                  </Field>
                </div>
              </FieldGroup>
            </form>
          </div>
        </div>
        <DialogFooter className="flex-shrink-0">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={crearReserva.isPending}
          >
            Cancelar
          </Button>
          <Button type="submit" form={formId} disabled={crearReserva.isPending}>
            {crearReserva.isPending && <Spinner className="mr-2 size-4" />}
            Confirmar Reserva
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
