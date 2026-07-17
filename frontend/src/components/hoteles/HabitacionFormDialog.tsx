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
import { habitacionCreateSchema, habitacionUpdateSchema } from "@/lib/validators/habitacion";
import type { HabitacionCreateFormValues, HabitacionUpdateFormValues } from "@/lib/validators/habitacion";
import type { Habitacion } from "@/types/hotel";
import { useBuscarHoteles, useTiposHabitacion } from "@/hooks/useHoteles";

interface HabitacionFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  habitacion?: Habitacion | null;
  onSubmit: (data: HabitacionCreateFormValues | HabitacionUpdateFormValues) => void;
  isLoading?: boolean;
}

export function HabitacionFormDialog({
  open,
  onOpenChange,
  habitacion,
  onSubmit,
  isLoading,
}: HabitacionFormDialogProps) {
  const isEditing = !!habitacion;
  const formId = useId();

  const { data: hotelesData, isLoading: isLoadingHoteles } = useBuscarHoteles({ page: 0, size: 100 });
  const { data: tipos = [], isLoading: isLoadingTipos } = useTiposHabitacion();

  const hoteles = hotelesData?.content ?? [];

  const createForm = useForm<HabitacionCreateFormValues>({
    resolver: zodResolver(habitacionCreateSchema),
    defaultValues: {
      numero: 0,
      piso: 1,
    },
  });

  const editForm = useForm<HabitacionUpdateFormValues>({
    resolver: zodResolver(habitacionUpdateSchema),
    defaultValues: {
      numero: 0,
      piso: 1,
    },
  });

  useEffect(() => {
    if (open) {
      if (isEditing && habitacion) {
        editForm.reset({
          numero: habitacion.numero,
          piso: habitacion.piso,
          idTipoHabitacion: habitacion.tipoHabitacion.id,
        });
      } else {
        createForm.reset({
          numero: 0,
          piso: 1,
        });
      }
    }
  }, [open, isEditing, habitacion, editForm, createForm]);

  const handleSubmit = (data: HabitacionCreateFormValues | HabitacionUpdateFormValues) => {
    onSubmit(data);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>
            {isEditing ? "Editar Habitación" : "Nueva Habitación"}
          </DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
            <form
              id={formId}
              onSubmit={isEditing ? editForm.handleSubmit(handleSubmit) : createForm.handleSubmit(handleSubmit)}
              className="space-y-4"
            >
              <FieldGroup>
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <Field>
                    <FieldLabel>Número</FieldLabel>
                    <Input
                      {...(isEditing ? editForm.register("numero", { valueAsNumber: true }) : createForm.register("numero", { valueAsNumber: true }))}
                      type="number"
                      placeholder="101"
                    />
                    <FieldError errors={isEditing ? [editForm.formState.errors.numero] : [createForm.formState.errors.numero]} />
                  </Field>

                  <Field>
                    <FieldLabel>Piso</FieldLabel>
                    <Input
                      {...(isEditing ? editForm.register("piso", { valueAsNumber: true }) : createForm.register("piso", { valueAsNumber: true }))}
                      type="number"
                      placeholder="1"
                    />
                    <FieldError errors={isEditing ? [editForm.formState.errors.piso] : [createForm.formState.errors.piso]} />
                  </Field>
                </div>

                <Field>
                  <FieldLabel>Tipo de Habitación</FieldLabel>
                  <Select
                    value={isEditing
                      ? editForm.watch("idTipoHabitacion")?.toString() ?? ""
                      : createForm.watch("idTipoHabitacion")?.toString() ?? ""
                    }
                    onValueChange={(val) => {
                      if (val) {
                        const numVal = Number(val);
                        if (isEditing) {
                          editForm.setValue("idTipoHabitacion", numVal, { shouldValidate: true });
                        } else {
                          createForm.setValue("idTipoHabitacion", numVal, { shouldValidate: true });
                        }
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
                  <FieldError errors={isEditing ? [editForm.formState.errors.idTipoHabitacion] : [createForm.formState.errors.idTipoHabitacion]} />
                </Field>

                {!isEditing && (
                  <Field>
                    <FieldLabel>Hotel</FieldLabel>
                    <Select
                      value={createForm.watch("idHotel")?.toString() ?? ""}
                      onValueChange={(val) => {
                        if (val) {
                          createForm.setValue("idHotel", Number(val), { shouldValidate: true });
                        }
                      }}
                      items={hoteles.map((h) => ({
                        value: h.id.toString(),
                        label: h.nombre,
                      }))}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder={isLoadingHoteles ? "Cargando hoteles..." : "Seleccionar hotel"} />
                      </SelectTrigger>
                      <SelectContent>
                        {hoteles.map((h) => (
                          <SelectItem key={h.id} value={h.id.toString()}>
                            {h.nombre}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FieldError errors={[createForm.formState.errors.idHotel]} />
                  </Field>
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
            {isEditing ? "Guardar Cambios" : "Crear Habitación"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
