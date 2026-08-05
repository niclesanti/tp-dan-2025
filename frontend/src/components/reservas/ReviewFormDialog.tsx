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
import { Textarea } from "@/components/ui/textarea";
import { Field, FieldLabel, FieldError, FieldGroup } from "@/components/ui/field";
import { Spinner } from "@/components/ui/spinner";
import { StarRating } from "@/components/hoteles/StarRating";
import { reviewSchema } from "@/lib/validators/reserva";
import type { ReviewFormValues } from "@/lib/validators/reserva";
import { useReviewCliente, useReviewHost } from "@/hooks/useReservas";

interface ReviewFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  reservaId: string;
  tipo?: "cliente" | "host";
}

export function ReviewFormDialog({
  open,
  onOpenChange,
  reservaId,
  tipo = "cliente",
}: ReviewFormDialogProps) {
  const formId = useId();
  const reviewCliente = useReviewCliente();
  const reviewHost = useReviewHost();
  const mutation = tipo === "host" ? reviewHost : reviewCliente;

  const form = useForm<ReviewFormValues>({
    resolver: zodResolver(reviewSchema),
    defaultValues: {
      rating: 0,
      comment: "",
    },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        rating: 0,
        comment: "",
      });
    }
  }, [open, form]);

  const handleSubmit = form.handleSubmit((data) => {
    mutation.mutate(
      {
        id: reservaId,
        data: {
          rating: data.rating,
          comment: data.comment ?? "",
        },
      },
      {
        onSuccess: () => onOpenChange(false),
      }
    );
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle>
            {tipo === "host" ? "Review del Host" : "Review del Cliente"}
          </DialogTitle>
        </DialogHeader>
        <div className="flex-1 min-h-0 overflow-y-auto">
          <div className="pr-2 pb-4">
            <form id={formId} onSubmit={handleSubmit} className="space-y-4">
              <FieldGroup>
                <Field>
                  <FieldLabel>Calificación *</FieldLabel>
                  <StarRating
                    value={form.watch("rating")}
                    readonly={false}
                    onChange={(val) =>
                      form.setValue("rating", val, { shouldValidate: true })
                    }
                  />
                  <FieldError errors={[form.formState.errors.rating]} />
                </Field>

                <Field>
                  <FieldLabel>Comentario *</FieldLabel>
                  <Textarea
                    {...form.register("comment")}
                    placeholder={
                      tipo === "host"
                        ? "Describí tu experiencia con el huésped..."
                        : "Contá tu experiencia..."
                    }
                    rows={4}
                    maxLength={500}
                  />
                  <FieldError errors={[form.formState.errors.comment]} />
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
            disabled={mutation.isPending}
          >
            Cancelar
          </Button>
          <Button type="submit" form={formId} disabled={mutation.isPending}>
            {mutation.isPending && <Spinner className="mr-2 size-4" />}
            Enviar Review
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
