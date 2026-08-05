import { useEffect, useId } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQuery } from "@tanstack/react-query";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Spinner } from "@/components/ui/spinner";
import { pagoSchema } from "@/lib/validators/reserva";
import type { PagoFormValues } from "@/lib/validators/reserva";
import { useAgregarPago } from "@/hooks/useReservas";
import { usuarioService } from "@/services/usuario.service";
import type { PagoDTORequest } from "@/types/reserva";

interface PagoFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  reservaId: string;
  montoPendiente: number;
  dni: string;
}

const METODOS_PAGO = ["TARJETA_CREDITO", "EFECTIVO"];

export function PagoFormDialog({
  open,
  onOpenChange,
  reservaId,
  montoPendiente,
  dni,
}: PagoFormDialogProps) {
  const formId = useId();
  const agregarPago = useAgregarPago();

  const form = useForm<PagoFormValues>({
    resolver: zodResolver(pagoSchema),
    defaultValues: {
      method: "TARJETA_CREDITO",
      transactionId: "",
      amount: montoPendiente > 0 ? montoPendiente : 0,
      currency: "USD",
    },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        method: "TARJETA_CREDITO",
        transactionId: "",
        amount: montoPendiente > 0 ? montoPendiente : 0,
        currency: "USD",
      });
    }
  }, [open, montoPendiente, form]);

  const method = form.watch("method");

  const {
    data: nroTarjeta,
    isLoading: cargandoTarjeta,
    isError: errorTarjeta,
  } = useQuery({
    queryKey: ["tarjeta-principal", dni],
    queryFn: () => usuarioService.obtenerTarjetaPrincipalPorDni(dni),
    enabled: open && !!dni && method === "TARJETA_CREDITO",
    retry: false,
  });

  const handleSubmit = form.handleSubmit((data) => {
    const payload: PagoDTORequest = {
      method: data.method,
      transactionId: data.transactionId || "",
      amount: data.amount,
      currency: "USD",
      nroTarjeta:
        data.method === "TARJETA_CREDITO" && nroTarjeta ? nroTarjeta : undefined,
    };
    agregarPago.mutate(
      { id: reservaId, data: payload },
      {
        onSuccess: () => onOpenChange(false),
      }
    );
  });

  const tarjetaBloqueada = cargandoTarjeta || (method === "TARJETA_CREDITO" && errorTarjeta);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>Agregar Pago</DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
            {montoPendiente > 0 && (
              <div className="mb-4 rounded-lg border border-border bg-muted/30 p-3">
                <p className="text-xs text-muted-foreground">Monto pendiente</p>
                <p className="text-lg font-semibold text-foreground">
                  USD {montoPendiente?.toLocaleString("es-AR") ?? "—"}
                </p>
              </div>
            )}

            <form id={formId} onSubmit={handleSubmit} className="space-y-4">
              <FieldGroup>
                <Field>
                  <FieldLabel>Método de pago *</FieldLabel>
                  <Select
                    value={method}
                    onValueChange={(val) =>
                      val && form.setValue("method", val, { shouldValidate: true })
                    }
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Seleccionar método" />
                    </SelectTrigger>
                    <SelectContent>
                      {METODOS_PAGO.map((m) => (
                        <SelectItem key={m} value={m}>
                          {m.replace(/_/g, " ")}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FieldError errors={[form.formState.errors.method]} />
                </Field>

                {method === "TARJETA_CREDITO" && (
                  <div className="flex items-center gap-2 text-xs">
                    {cargandoTarjeta ? (
                      <>
                        <Spinner className="size-3" />
                        <span className="text-muted-foreground">
                          Buscando tarjeta principal...
                        </span>
                      </>
                    ) : errorTarjeta ? (
                      <span className="text-destructive">
                        El huésped no tiene tarjeta principal registrada
                      </span>
                    ) : nroTarjeta ? (
                      <span className="text-muted-foreground">
                        Tarjeta principal: **** {String(nroTarjeta).slice(-4)}
                      </span>
                    ) : null}
                  </div>
                )}

                <Field>
                  <FieldLabel>ID de transacción</FieldLabel>
                  <Input
                    {...form.register("transactionId")}
                    placeholder="TXN-12345 (opcional)"
                  />
                  <FieldError errors={[form.formState.errors.transactionId]} />
                </Field>

                <div className="grid grid-cols-2 gap-4">
                  <Field>
                    <FieldLabel>Monto *</FieldLabel>
                    <Input
                      {...form.register("amount", { valueAsNumber: true })}
                      type="number"
                      min={0.01}
                      step={0.01}
                    />
                    <FieldError errors={[form.formState.errors.amount]} />
                  </Field>

                  <Field>
                    <FieldLabel>Moneda</FieldLabel>
                    <Input value="USD" readOnly />
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
            disabled={agregarPago.isPending}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            form={formId}
            disabled={agregarPago.isPending || tarjetaBloqueada}
          >
            {agregarPago.isPending && <Spinner className="mr-2 size-4" />}
            Registrar Pago
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
