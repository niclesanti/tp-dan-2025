import { z } from "zod";

export const habitacionCreateSchema = z.object({
  numero: z.number().int().positive("Debe ser un número positivo"),
  piso: z.number().int().positive("Debe ser un número positivo"),
  idTipoHabitacion: z.number().int().positive("Seleccioná un tipo"),
  idHotel: z.number().int().positive("Seleccioná un hotel"),
});

export const habitacionUpdateSchema = z.object({
  numero: z.number().int().positive("Debe ser un número positivo"),
  piso: z.number().int().positive("Debe ser un número positivo"),
  idTipoHabitacion: z.number().int().positive("Seleccioná un tipo"),
});

export type HabitacionCreateFormValues = z.infer<typeof habitacionCreateSchema>;
export type HabitacionUpdateFormValues = z.infer<typeof habitacionUpdateSchema>;
