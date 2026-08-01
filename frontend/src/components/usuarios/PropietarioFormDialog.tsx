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
import { propietarioSchema } from "@/lib/validators/propietario";
import type { PropietarioFormValues } from "@/lib/validators/propietario";
import { useBancos } from "@/hooks/useBancos";
import { useHotelesDropdown } from "@/hooks/useHoteles";

interface PropietarioFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: PropietarioFormValues) => void;
  isLoading?: boolean;
}

export function PropietarioFormDialog({
  open,
  onOpenChange,
  onSubmit,
  isLoading,
}: PropietarioFormDialogProps) {
  const { data: bancos = [], isLoading: isLoadingBancos } = useBancos();
  const { data: hoteles = [], isLoading: isLoadingHoteles } = useHotelesDropdown();
  const formId = useId();

  const createForm = useForm<PropietarioFormValues>({
    resolver: zodResolver(propietarioSchema),
    defaultValues: {
      nombre: "",
      email: "",
      telefono: "",
      dni: "",
      idHotel: undefined as unknown as number,
      cuentaBancaria: {
        numeroCuenta: "",
        cbu: "",
        alias: "",
      },
    },
  });

  useEffect(() => {
    if (open) {
      createForm.reset({
        nombre: "",
        email: "",
        telefono: "",
        dni: "",
        idHotel: undefined as unknown as number,
        cuentaBancaria: {
          numeroCuenta: "",
          cbu: "",
          alias: "",
        },
      });
    }
  }, [open, createForm]);

  const handleSubmit = createForm.handleSubmit((data) => {
    onSubmit(data);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>Nuevo Propietario</DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
          <form id={formId} onSubmit={handleSubmit} className="space-y-4">
            <FieldGroup>
              <Field>
                <FieldLabel>Nombre</FieldLabel>
                <Input
                  {...createForm.register("nombre")}
                  placeholder="Nombre completo"
                />
                <FieldError errors={[createForm.formState.errors.nombre]} />
              </Field>

              <Field>
                <FieldLabel>Email</FieldLabel>
                <Input
                  {...createForm.register("email")}
                  type="email"
                  placeholder="email@ejemplo.com"
                />
                <FieldError errors={[createForm.formState.errors.email]} />
              </Field>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Field>
                  <FieldLabel>Teléfono</FieldLabel>
                  <Input
                    {...createForm.register("telefono")}
                    placeholder="+54 11 1234-5678"
                  />
                  <FieldError errors={[createForm.formState.errors.telefono]} />
                </Field>

                <Field>
                  <FieldLabel>DNI</FieldLabel>
                  <Input
                    {...createForm.register("dni")}
                    placeholder="12345678"
                    maxLength={8}
                  />
                  <FieldError errors={[createForm.formState.errors.dni]} />
                </Field>
              </div>

              <Field>
                <FieldLabel>Hotel</FieldLabel>
                <Select
                  value={createForm.watch("idHotel")?.toString() ?? ""}
                  onValueChange={(val) =>
                    createForm.setValue("idHotel", Number(val), { shouldValidate: true })
                  }
                  items={hoteles.map((h) => ({ value: h.id.toString(), label: h.nombre }))}
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
                  value={createForm.watch("cuentaBancaria.bancoId")?.toString() ?? ""}
                  onValueChange={(val) =>
                    createForm.setValue("cuentaBancaria.bancoId", Number(val), {
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
                <FieldError errors={[createForm.formState.errors.cuentaBancaria?.bancoId]} />
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
            Crear Propietario
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
