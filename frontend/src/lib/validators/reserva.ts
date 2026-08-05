import { z } from "zod";

export const buscarHabitacionesSchema = z.object({
  checkIn: z.string().min(1, "Fecha de check-in requerida"),
  checkOut: z.string().min(1, "Fecha de check-out requerida"),
  capacidad: z.number().int().min(1).optional().nullable(),
  precioMin: z.number().min(0).optional().nullable(),
  precioMax: z.number().min(0).optional().nullable(),
  categoriaHotel: z.number().int().min(1).max(5).optional().nullable(),
});

export type BuscarHabitacionesFormValues = z.infer<typeof buscarHabitacionesSchema>;

export const crearReservaSchema = z.object({
  nombreApellido: z.string().min(2, "Mínimo 2 caracteres").max(200),
  email: z.string().email("Email inválido"),
  dni: z.string().regex(/^\d{7,8}$/, "El DNI debe tener 7 u 8 dígitos"),
  checkIn: z.string().min(1, "Fecha de check-in requerida"),
  checkOut: z.string().min(1, "Fecha de check-out requerida"),
});

export type CrearReservaFormValues = z.infer<typeof crearReservaSchema>;

export const pagoSchema = z.object({
  method: z.string().min(1, "Método de pago requerido"),
  transactionId: z.string().optional(),
  amount: z.number().min(0.01, "El monto debe ser mayor a 0"),
  currency: z.literal("USD"),
  nroTarjeta: z.string().optional(),
});

export type PagoFormValues = z.infer<typeof pagoSchema>;

export const reviewSchema = z.object({
  rating: z.number().min(1, "Mínimo 1 estrella").max(5, "Máximo 5 estrellas"),
  comment: z.string().min(1, "El comentario es requerido").max(500, "Máximo 500 caracteres"),
});

export type ReviewFormValues = z.infer<typeof reviewSchema>;
