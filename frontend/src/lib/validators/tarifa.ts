import { z } from "zod";

export const tarifaCreateSchema = z
  .object({
    idTipoHabitacion: z.number().int().positive("Seleccioná un tipo de habitación"),
    precioNoche: z
      .number()
      .min(0.01, "El precio mínimo es $0.01"),
    esPromocional: z.boolean(),
    fechaInicio: z.string().optional(),
    fechaFin: z.string().optional(),
  })
  .refine(
    (data) => {
      if (data.esPromocional) {
        return !!data.fechaInicio && data.fechaInicio.length > 0;
      }
      return true;
    },
    { message: "La fecha de inicio es obligatoria para tarifas promocionales", path: ["fechaInicio"] }
  )
  .refine(
    (data) => {
      if (data.esPromocional) {
        return !!data.fechaFin && data.fechaFin.length > 0;
      }
      return true;
    },
    { message: "La fecha de fin es obligatoria para tarifas promocionales", path: ["fechaFin"] }
  )
  .refine(
    (data) => {
      if (data.esPromocional && data.fechaInicio && data.fechaFin) {
        return data.fechaFin >= data.fechaInicio;
      }
      return true;
    },
    { message: "La fecha de fin debe ser posterior a la fecha de inicio", path: ["fechaFin"] }
  );

export type TarifaCreateFormValues = z.infer<typeof tarifaCreateSchema>;
