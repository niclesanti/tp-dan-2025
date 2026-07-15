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
import { StarRating } from "./StarRating";
import { hotelCreateSchema, hotelUpdateSchema } from "@/lib/validators/hotel";
import type { HotelCreateFormValues, HotelUpdateFormValues } from "@/lib/validators/hotel";
import type { Hotel } from "@/types/hotel";

interface HotelFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  hotel?: Hotel | null;
  onSubmit: (data: HotelCreateFormValues | HotelUpdateFormValues) => void;
  isLoading?: boolean;
}

export function HotelFormDialog({
  open,
  onOpenChange,
  hotel,
  onSubmit,
  isLoading,
}: HotelFormDialogProps) {
  const isEditing = !!hotel;
  const formId = useId();

  const createForm = useForm<HotelCreateFormValues>({
    resolver: zodResolver(hotelCreateSchema),
    defaultValues: {
      nombre: "",
      cuit: "",
      domicilio: "",
      latitud: null,
      longitud: null,
      telefono: "",
      correoContacto: "",
      categoria: 3,
    },
  });

  const editForm = useForm<HotelUpdateFormValues>({
    resolver: zodResolver(hotelUpdateSchema),
    defaultValues: {
      categoria: 3,
      telefono: "",
      correoContacto: "",
    },
  });

  useEffect(() => {
    if (open) {
      if (isEditing && hotel) {
        editForm.reset({
          categoria: hotel.categoria,
          telefono: hotel.telefono,
          correoContacto: hotel.correoContacto,
        });
      } else {
        createForm.reset({
          nombre: "",
          cuit: "",
          domicilio: "",
          latitud: null,
          longitud: null,
          telefono: "",
          correoContacto: "",
          categoria: 3,
        });
      }
    }
  }, [open, isEditing, hotel, editForm, createForm]);

  const handleCreateSubmit = createForm.handleSubmit((data) => {
    onSubmit(data);
  });

  const handleEditSubmit = editForm.handleSubmit((data) => {
    onSubmit(data);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>
            {isEditing ? "Editar Hotel" : "Nuevo Hotel"}
          </DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
            <form
              id={formId}
              onSubmit={isEditing ? handleEditSubmit : handleCreateSubmit}
              className="space-y-4"
            >
              <FieldGroup>
                {!isEditing ? (
                  <Field>
                    <FieldLabel>Nombre</FieldLabel>
                    <Input
                      {...createForm.register("nombre")}
                      placeholder="Nombre del hotel"
                    />
                    <FieldError errors={[createForm.formState.errors.nombre]} />
                  </Field>
                ) : (
                  <Field>
                    <FieldLabel>Nombre</FieldLabel>
                    <Input
                      value={hotel?.nombre ?? ""}
                      readOnly
                      disabled
                      className="opacity-60"
                    />
                  </Field>
                )}

                {!isEditing && (
                  <>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <Field>
                        <FieldLabel>CUIT</FieldLabel>
                        <Input
                          {...createForm.register("cuit")}
                          placeholder="XX-XXXXXXXX-X"
                          maxLength={20}
                        />
                        <FieldError errors={[createForm.formState.errors.cuit]} />
                      </Field>

                      <Field>
                        <FieldLabel>Domicilio</FieldLabel>
                        <Input
                          {...createForm.register("domicilio")}
                          placeholder="Dirección completa"
                        />
                        <FieldError errors={[createForm.formState.errors.domicilio]} />
                      </Field>
                    </div>

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <Field>
                        <FieldLabel>Latitud (opcional)</FieldLabel>
                        <Input
                          {...createForm.register("latitud", { valueAsNumber: true })}
                          type="number"
                          step="any"
                          placeholder="-34.6037"
                        />
                        <FieldError errors={[createForm.formState.errors.latitud]} />
                      </Field>

                      <Field>
                        <FieldLabel>Longitud (opcional)</FieldLabel>
                        <Input
                          {...createForm.register("longitud", { valueAsNumber: true })}
                          type="number"
                          step="any"
                          placeholder="-58.3816"
                        />
                        <FieldError errors={[createForm.formState.errors.longitud]} />
                      </Field>
                    </div>
                  </>
                )}

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <Field>
                    <FieldLabel>Teléfono</FieldLabel>
                    <Input
                      {...(isEditing ? editForm.register("telefono") : createForm.register("telefono"))}
                      placeholder="+54 11 1234-5678"
                    />
                    <FieldError errors={isEditing ? [editForm.formState.errors.telefono] : [createForm.formState.errors.telefono]} />
                  </Field>

                  <Field>
                    <FieldLabel>Email de Contacto</FieldLabel>
                    <Input
                      {...(isEditing ? editForm.register("correoContacto") : createForm.register("correoContacto"))}
                      type="email"
                      placeholder="contact@hotel.com"
                    />
                    <FieldError errors={isEditing ? [editForm.formState.errors.correoContacto] : [createForm.formState.errors.correoContacto]} />
                  </Field>
                </div>

                <Field>
                  <FieldLabel>Categoría</FieldLabel>
                  <StarRating
                    value={isEditing ? editForm.watch("categoria") : createForm.watch("categoria")}
                    readonly={false}
                    onChange={(val) =>
                      isEditing
                        ? editForm.setValue("categoria", val, { shouldValidate: true })
                        : createForm.setValue("categoria", val, { shouldValidate: true })
                    }
                  />
                  <FieldError errors={isEditing ? [editForm.formState.errors.categoria] : [createForm.formState.errors.categoria]} />
                </Field>
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
            {isEditing ? "Guardar Cambios" : "Crear Hotel"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
