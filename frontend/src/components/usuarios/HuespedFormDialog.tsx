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
import { huespedSchema, huespedUpdateSchema } from "@/lib/validators/huesped";
import type { HuespedFormValues, HuespedUpdateFormValues } from "@/lib/validators/huesped";
import type { Huesped } from "@/types/usuario";
import { useBancos } from "@/hooks/useBancos";

interface HuespedFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  huesped?: Huesped | null;
  onSubmit: (data: HuespedFormValues | HuespedUpdateFormValues) => void;
  isLoading?: boolean;
}

export function HuespedFormDialog({
  open,
  onOpenChange,
  huesped,
  onSubmit,
  isLoading,
}: HuespedFormDialogProps) {
  const isEditing = !!huesped;
  const { data: bancos = [], isLoading: isLoadingBancos } = useBancos();
  const formId = useId();

  const createForm = useForm<HuespedFormValues>({
    resolver: zodResolver(huespedSchema),
    defaultValues: {
      nombre: "",
      email: "",
      telefono: "",
      dni: "",
      fechaNacimiento: "",
      tarjetaCredito: {
        numero: "",
        nombreTitular: "",
        fechaVencimiento: "",
        cvc: "",
        esPrincipal: true,
      },
    },
  });

  const editForm = useForm<HuespedUpdateFormValues>({
    resolver: zodResolver(huespedUpdateSchema),
    defaultValues: {
      nombre: "",
      email: "",
      telefono: "",
      dni: "",
      fechaNacimiento: "",
    },
  });

  const form = isEditing ? editForm : createForm;

  useEffect(() => {
    if (open) {
      if (isEditing && huesped) {
        editForm.reset({
          nombre: huesped.nombre,
          email: huesped.email,
          telefono: huesped.telefono,
          dni: huesped.dni,
          fechaNacimiento: huesped.fechaNacimiento,
        });
      } else {
        createForm.reset({
          nombre: "",
          email: "",
          telefono: "",
          dni: "",
          fechaNacimiento: "",
          tarjetaCredito: {
            numero: "",
            nombreTitular: "",
            fechaVencimiento: "",
            cvc: "",
            esPrincipal: true,
          },
        });
      }
    }
  }, [open, isEditing, huesped, editForm, createForm]);

  const handleSubmit = form.handleSubmit((data) => {
    onSubmit(data);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>
            {isEditing ? "Editar Huésped" : "Nuevo Huésped"}
          </DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
          <form id={formId} onSubmit={handleSubmit} className="space-y-4">
            <FieldGroup>
              <Field>
                <FieldLabel>Nombre</FieldLabel>
                <Input
                  {...(isEditing ? editForm.register("nombre") : createForm.register("nombre"))}
                  placeholder="Nombre completo"
                />
                <FieldError errors={isEditing ? [editForm.formState.errors.nombre] : [createForm.formState.errors.nombre]} />
              </Field>

              <Field>
                <FieldLabel>Email</FieldLabel>
                <Input
                  {...(isEditing ? editForm.register("email") : createForm.register("email"))}
                  type="email"
                  placeholder="email@ejemplo.com"
                />
                <FieldError errors={isEditing ? [editForm.formState.errors.email] : [createForm.formState.errors.email]} />
              </Field>

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
                  <FieldLabel>DNI</FieldLabel>
                  <Input
                    {...(isEditing ? editForm.register("dni") : createForm.register("dni"))}
                    placeholder="12345678"
                    maxLength={8}
                  />
                  <FieldError errors={isEditing ? [editForm.formState.errors.dni] : [createForm.formState.errors.dni]} />
                </Field>
              </div>

              <Field>
                <FieldLabel>Fecha de Nacimiento</FieldLabel>
                <Input
                  {...(isEditing ? editForm.register("fechaNacimiento") : createForm.register("fechaNacimiento"))}
                  type="date"
                />
                <FieldError errors={isEditing ? [editForm.formState.errors.fechaNacimiento] : [createForm.formState.errors.fechaNacimiento]} />
              </Field>

              {!isEditing && (
                <>
                  <div className="pt-2">
                    <h4 className="text-sm font-medium text-foreground">Tarjeta de Crédito</h4>
                    <p className="text-xs text-muted-foreground">Obligatoria para el registro</p>
                  </div>

                  <Field>
                    <FieldLabel>Número de Tarjeta</FieldLabel>
                    <Input
                      {...createForm.register("tarjetaCredito.numero")}
                      placeholder="1234567890123456"
                      maxLength={22}
                    />
                    <FieldError errors={[createForm.formState.errors.tarjetaCredito?.numero]} />
                  </Field>

                  <Field>
                    <FieldLabel>Nombre del Titular</FieldLabel>
                    <Input
                      {...createForm.register("tarjetaCredito.nombreTitular")}
                      placeholder="Nombre como aparece en la tarjeta"
                    />
                    <FieldError errors={[createForm.formState.errors.tarjetaCredito?.nombreTitular]} />
                  </Field>

                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                    <Field>
                      <FieldLabel>Vencimiento</FieldLabel>
                      <Input
                        {...createForm.register("tarjetaCredito.fechaVencimiento")}
                        placeholder="MM/YY"
                        maxLength={5}
                      />
                      <FieldError errors={[createForm.formState.errors.tarjetaCredito?.fechaVencimiento]} />
                    </Field>

                    <Field>
                      <FieldLabel>CVC</FieldLabel>
                      <Input
                        {...createForm.register("tarjetaCredito.cvc")}
                        placeholder="123"
                        maxLength={4}
                      />
                      <FieldError errors={[createForm.formState.errors.tarjetaCredito?.cvc]} />
                    </Field>

                    <Field>
                    <FieldLabel>Banco</FieldLabel>
                    <Select
                      value={createForm.watch("tarjetaCredito.bancoId")?.toString() ?? ""}
                      onValueChange={(val) =>
                        createForm.setValue("tarjetaCredito.bancoId", Number(val), {
                          shouldValidate: true,
                        })
                      }
                      items={bancos.map((b) => ({
                        value: b.id.toString(),
                        label: b.nombre,
                      }))}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder={isLoadingBancos ? "Cargando bancos..." : "Seleccionar banco"} />
                      </SelectTrigger>
                      <SelectContent>
                        {bancos.map((b) => (
                          <SelectItem key={b.id} value={b.id.toString()}>
                            {b.nombre}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                      <FieldError errors={[createForm.formState.errors.tarjetaCredito?.bancoId]} />
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
            {isEditing ? "Guardar Cambios" : "Crear Huésped"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}