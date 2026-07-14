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
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Spinner } from "@/components/ui/spinner";
import { tarjetaSchema } from "@/lib/validators/huesped";
import type { TarjetaFormValues } from "@/lib/validators/huesped";
import { useBancos } from "@/hooks/useBancos";

interface TarjetaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: TarjetaFormValues) => void;
  isLoading?: boolean;
}

export function TarjetaFormDialog({
  open,
  onOpenChange,
  onSubmit,
  isLoading,
}: TarjetaFormDialogProps) {
  const { data: bancos = [] } = useBancos();
  const formId = useId();

  const form = useForm<TarjetaFormValues>({
    resolver: zodResolver(tarjetaSchema),
    defaultValues: {
      numero: "",
      nombreTitular: "",
      fechaVencimiento: "",
      cvc: "",
      esPrincipal: false,
      bancoId: 0,
    },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        numero: "",
        nombreTitular: "",
        fechaVencimiento: "",
        cvc: "",
        esPrincipal: false,
        bancoId: 0,
      });
    }
  }, [open, form]);

  const handleSubmit = form.handleSubmit((data) => {
    onSubmit(data);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>Agregar Tarjeta de Crédito</DialogTitle>
        </DialogHeader>
        <ScrollArea className="flex-1 min-h-0">
          <form id={formId} onSubmit={handleSubmit} className="space-y-4">
            <FieldGroup>
              <Field>
                <FieldLabel>Número de Tarjeta</FieldLabel>
                <Input
                  {...form.register("numero")}
                  placeholder="1234567890123456"
                  maxLength={22}
                />
                <FieldError errors={[form.formState.errors.numero]} />
              </Field>

              <Field>
                <FieldLabel>Nombre del Titular</FieldLabel>
                <Input
                  {...form.register("nombreTitular")}
                  placeholder="Nombre como aparece en la tarjeta"
                />
                <FieldError errors={[form.formState.errors.nombreTitular]} />
              </Field>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <Field>
                  <FieldLabel>Vencimiento</FieldLabel>
                  <Input
                    {...form.register("fechaVencimiento")}
                    placeholder="MM/YY"
                    maxLength={5}
                  />
                  <FieldError errors={[form.formState.errors.fechaVencimiento]} />
                </Field>

                <Field>
                  <FieldLabel>CVC</FieldLabel>
                  <Input
                    {...form.register("cvc")}
                    placeholder="123"
                    maxLength={4}
                  />
                  <FieldError errors={[form.formState.errors.cvc]} />
                </Field>

                <Field>
                  <FieldLabel>Banco</FieldLabel>
                  <Select
                    value={form.watch("bancoId")?.toString() || ""}
                    onValueChange={(val) =>
                      form.setValue("bancoId", Number(val), {
                        shouldValidate: true,
                      })
                    }
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Seleccionar" />
                    </SelectTrigger>
                    <SelectContent>
                      {bancos.map((b) => (
                        <SelectItem key={b.id} value={b.id.toString()}>
                          {b.nombre}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FieldError errors={[form.formState.errors.bancoId]} />
                </Field>
              </div>

              <Field>
                <label className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    {...form.register("esPrincipal")}
                    className="size-4 rounded border-input"
                  />
                  <span className="text-foreground">Establecer como tarjeta principal</span>
                </label>
              </Field>
            </FieldGroup>
          </form>
        </ScrollArea>
        <DialogFooter className="flex-shrink-0">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancelar
          </Button>
          <Button type="submit" form={formId} disabled={isLoading}>
            {isLoading && <Spinner className="mr-2 size-4" />}
            Agregar Tarjeta
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}