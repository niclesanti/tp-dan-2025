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

interface PagoFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  reservaId: string;
  montoPendiente: number;
}

const METODOS_PAGO = [
  "TARJETA_CREDITO",
  "TARJETA_DEBITO",
  "TRANSFERENCIA",
  "EFECTIVO",
  "MERCADO_PAGO",
];

const MONEDAS = ["USD", "ARS"];

export function PagoFormDialog({
  open,
  onOpenChange,
  reservaId,
  montoPendiente,
}: PagoFormDialogProps) {
  const formId = useId();
  const agregarPago = useAgregarPago();

  const form = useForm<PagoFormValues>({
    resolver: zodResolver(pagoSchema),
    defaultValues: {
      method: "",
      transactionId: "",
      amount: montoPendiente > 0 ? montoPendiente : 0,
      currency: "USD",
    },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        method: "",
        transactionId: "",
        amount: montoPendiente > 0 ? montoPendiente : 0,
        currency: "USD",
      });
    }
  }, [open, montoPendiente, form]);

  const handleSubmit = form.handleSubmit((data) => {
    agregarPago.mutate(
      { id: reservaId, data },
      {
        onSuccess: () => onOpenChange(false),
      }
    );
  });

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
                  USD {montoPendiente.toLocaleString("es-AR")}
                </p>
              </div>
            )}

            <form id={formId} onSubmit={handleSubmit} className="space-y-4">
              <FieldGroup>
                <Field>
                  <FieldLabel>Método de pago *</FieldLabel>
                  <Select
                    value={form.watch("method") ?? ""}
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

                <Field>
                  <FieldLabel>ID de transacción *</FieldLabel>
                  <Input
                    {...form.register("transactionId")}
                    placeholder="TXN-12345"
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
                    <FieldLabel>Moneda *</FieldLabel>
                    <Select
                      value={form.watch("currency") ?? "USD"}
                      onValueChange={(val) =>
                        val && form.setValue("currency", val, { shouldValidate: true })
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {MONEDAS.map((m) => (
                          <SelectItem key={m} value={m}>
                            {m}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FieldError errors={[form.formState.errors.currency]} />
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
          <Button type="submit" form={formId} disabled={agregarPago.isPending}>
            {agregarPago.isPending && <Spinner className="mr-2 size-4" />}
            Registrar Pago
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
