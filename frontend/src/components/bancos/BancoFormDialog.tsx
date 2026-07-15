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
import { bancoSchema } from "@/lib/validators/banco";
import type { BancoFormValues } from "@/lib/validators/banco";
import type { Banco } from "@/types/usuario";

interface BancoFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  banco?: Banco | null;
  onSubmit: (data: BancoFormValues) => void;
  isLoading?: boolean;
}

export function BancoFormDialog({
  open,
  onOpenChange,
  banco,
  onSubmit,
  isLoading,
}: BancoFormDialogProps) {
  const isEditing = !!banco;
  const formId = useId();

  const form = useForm<BancoFormValues>({
    resolver: zodResolver(bancoSchema),
    defaultValues: {
      nombre: "",
    },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        nombre: banco?.nombre ?? "",
      });
    }
  }, [open, banco, form]);

  const handleSubmit = form.handleSubmit((data) => {
    onSubmit(data);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>
            {isEditing ? "Editar Banco" : "Nuevo Banco"}
          </DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
            <form id={formId} onSubmit={handleSubmit} className="space-y-4">
              <FieldGroup>
                <Field>
                  <FieldLabel>Nombre</FieldLabel>
                  <Input
                    {...form.register("nombre")}
                    placeholder="Nombre del banco"
                  />
                  <FieldError errors={[form.formState.errors.nombre]} />
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
            {isEditing ? "Guardar Cambios" : "Crear Banco"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
