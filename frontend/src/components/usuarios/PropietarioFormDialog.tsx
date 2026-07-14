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
import { propietarioSchema, propietarioUpdateSchema } from "@/lib/validators/propietario";
import type { PropietarioFormValues, PropietarioUpdateFormValues } from "@/lib/validators/propietario";
import type { Propietario } from "@/types/usuario";
import { useBancos } from "@/hooks/useBancos";

interface PropietarioFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  propietario?: Propietario | null;
  onSubmit: (data: PropietarioFormValues | PropietarioUpdateFormValues) => void;
  isLoading?: boolean;
}

export function PropietarioFormDialog({
  open,
  onOpenChange,
  propietario,
  onSubmit,
  isLoading,
}: PropietarioFormDialogProps) {
  const isEditing = !!propietario;
  const { data: bancos = [] } = useBancos();
  const formId = useId();

  const createForm = useForm<PropietarioFormValues>({
    resolver: zodResolver(propietarioSchema),
    defaultValues: {
      nombre: "",
      email: "",
      telefono: "",
      dni: "",
      idHotel: null,
      cuentaBancaria: {
        numeroCuenta: "",
        cbu: "",
        alias: "",
        bancoId: 0,
      },
    },
  });

  const editForm = useForm<PropietarioUpdateFormValues>({
    resolver: zodResolver(propietarioUpdateSchema),
    defaultValues: {
      nombre: "",
      email: "",
      telefono: "",
      dni: "",
      idHotel: null,
    },
  });

  const form = isEditing ? editForm : createForm;

  useEffect(() => {
    if (open) {
      if (isEditing && propietario) {
        editForm.reset({
          nombre: propietario.nombre,
          email: propietario.email,
          telefono: propietario.telefono,
          dni: propietario.dni,
          idHotel: propietario.idHotel,
        });
      } else {
        createForm.reset({
          nombre: "",
          email: "",
          telefono: "",
          dni: "",
          idHotel: null,
          cuentaBancaria: {
            numeroCuenta: "",
            cbu: "",
            alias: "",
            bancoId: 0,
          },
        });
      }
    }
  }, [open, isEditing, propietario, editForm, createForm]);

  const handleSubmit = form.handleSubmit((data) => {
    onSubmit(data);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>
            {isEditing ? "Editar Propietario" : "Nuevo Propietario"}
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
                <FieldLabel>ID Hotel (opcional)</FieldLabel>
                <Input
                  {...(isEditing ? editForm.register("idHotel", { setValueAs: (v) => v === "" || v === null || v === undefined ? null : Number(v) }) : createForm.register("idHotel", { setValueAs: (v) => v === "" || v === null || v === undefined ? null : Number(v) }))}
                  type="number"
                  placeholder="Dejar vacío si no tiene"
                />
              </Field>

              {!isEditing && (
                <>
                  <div className="pt-2">
                    <h4 className="text-sm font-medium text-foreground">Cuenta Bancaria</h4>
                    <p className="text-xs text-muted-foreground">Obligatoria para el registro</p>
                  </div>

                  <Field>
                    <FieldLabel>Número de Cuenta</FieldLabel>
                    <Input
                      {...createForm.register("cuentaBancaria.numeroCuenta")}
                      placeholder="Número de cuenta"
                    />
                    <FieldError errors={[createForm.formState.errors.cuentaBancaria?.numeroCuenta]} />
                  </Field>

                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <Field>
                      <FieldLabel>CBU</FieldLabel>
                      <Input
                        {...createForm.register("cuentaBancaria.cbu")}
                        placeholder="22 dígitos"
                        maxLength={22}
                      />
                      <FieldError errors={[createForm.formState.errors.cuentaBancaria?.cbu]} />
                    </Field>

                    <Field>
                      <FieldLabel>Alias</FieldLabel>
                      <Input
                        {...createForm.register("cuentaBancaria.alias")}
                        placeholder="mi.cuenta.123"
                      />
                      <FieldError errors={[createForm.formState.errors.cuentaBancaria?.alias]} />
                    </Field>
                  </div>

                  <Field>
                    <FieldLabel>Banco</FieldLabel>
                    <Select
                      value={createForm.watch("cuentaBancaria.bancoId")?.toString() || ""}
                      onValueChange={(val) =>
                        createForm.setValue("cuentaBancaria.bancoId", Number(val), {
                          shouldValidate: true,
                        })
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="Seleccionar banco" />
                      </SelectTrigger>
                      <SelectContent>
                        {bancos.map((b) => (
                          <SelectItem key={b.id} value={b.id.toString()}>
                            {b.nombre}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FieldError errors={[createForm.formState.errors.cuentaBancaria?.bancoId]} />
                  </Field>
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
            {isEditing ? "Guardar Cambios" : "Crear Propietario"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}