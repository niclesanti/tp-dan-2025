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
import { tarifaCreateSchema } from "@/lib/validators/tarifa";
import type { TarifaCreateFormValues } from "@/lib/validators/tarifa";
import { useTiposHabitacion } from "@/hooks/useHoteles";

interface TarifaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: TarifaCreateFormValues) => void;
  isLoading?: boolean;
}

export function TarifaFormDialog({
  open,
  onOpenChange,
  onSubmit,
  isLoading,
}: TarifaFormDialogProps) {
  const formId = useId();
  const { data: tipos = [], isLoading: isLoadingTipos } = useTiposHabitacion();

  const form = useForm<TarifaCreateFormValues>({
    resolver: zodResolver(tarifaCreateSchema),
    defaultValues: {
      precioNoche: 0,
      esPromocional: false,
      fechaInicio: "",
      fechaFin: "",
    },
  });

  const esPromocional = form.watch("esPromocional");

  useEffect(() => {
    if (open) {
      form.reset({
        precioNoche: 0,
        esPromocional: false,
        fechaInicio: "",
        fechaFin: "",
      });
    }
  }, [open, form]);

  const handleSubmit = (data: TarifaCreateFormValues) => {
    onSubmit(data);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>Nueva Tarifa</DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
            <form
              id={formId}
              onSubmit={form.handleSubmit(handleSubmit)}
              className="space-y-4"
            >
              <FieldGroup>
                <Field>
                  <FieldLabel>Tipo de Habitación</FieldLabel>
                  <Select
                    value={form.watch("idTipoHabitacion")?.toString() ?? ""}
                    onValueChange={(val) => {
                      if (val) {
                        form.setValue("idTipoHabitacion", Number(val), { shouldValidate: true });
                      }
                    }}
                    items={tipos.map((t) => ({
                      value: t.id.toString(),
                      label: `${t.nombre} (cap. ${t.capacidad})`,
                    }))}
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder={isLoadingTipos ? "Cargando tipos..." : "Seleccionar tipo"} />
                    </SelectTrigger>
                    <SelectContent>
                      {tipos.map((t) => (
                        <SelectItem key={t.id} value={t.id.toString()}>
                          {t.nombre} (cap. {t.capacidad})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FieldError errors={[form.formState.errors.idTipoHabitacion]} />
                </Field>

                <Field>
                  <FieldLabel>Precio por noche ($)</FieldLabel>
                  <Input
                    {...form.register("precioNoche", { valueAsNumber: true })}
                    type="number"
                    min={0.01}
                    step={0.01}
                    placeholder="0.00"
                  />
                  <FieldError errors={[form.formState.errors.precioNoche]} />
                </Field>

                <Field>
                  <div className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      id={`${formId}-esPromocional`}
                      checked={esPromocional}
                      onChange={(e) => {
                        form.setValue("esPromocional", e.target.checked, { shouldValidate: true });
                        if (!e.target.checked) {
                          form.setValue("fechaInicio", "", { shouldValidate: true });
                          form.setValue("fechaFin", "", { shouldValidate: true });
                        }
                      }}
                      className="size-4 rounded border-input"
                    />
                    <label
                      htmlFor={`${formId}-esPromocional`}
                      className="text-sm font-medium text-foreground cursor-pointer"
                    >
                      Tarifa promocional
                    </label>
                  </div>
                  <p className="text-xs text-muted-foreground mt-1">
                    Sin marca: tarifa normal vigente desde hoy. Con marca: rango de fechas específico.
                  </p>
                </Field>

                {esPromocional && (
                  <>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <Field>
                        <FieldLabel>Fecha Inicio</FieldLabel>
                        <Input
                          {...form.register("fechaInicio")}
                          type="date"
                        />
                        <FieldError errors={[form.formState.errors.fechaInicio]} />
                      </Field>

                      <Field>
                        <FieldLabel>Fecha Fin</FieldLabel>
                        <Input
                          {...form.register("fechaFin")}
                          type="date"
                        />
                        <FieldError errors={[form.formState.errors.fechaFin]} />
                      </Field>
                    </div>
                  </>
                )}
              </FieldGroup>
            </form>
          </div>
        </div>
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
            Crear Tarifa
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
