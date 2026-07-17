import { z } from "zod";

const telefonoRegex = /^[0-9+\-\s()]{7,30}$/;
const cuitRegex = /^(\d{11}|\d{2}-\d{8}-\d{1})$/;

export const hotelCreateSchema = z.object({
  nombre: z.string().min(2, "Mínimo 2 caracteres").max(255),
  cuit: z.string().regex(cuitRegex, "CUIT inválido (XX-XXXXXXXX-X o 11 dígitos)"),
  domicilio: z.string().min(5, "Mínimo 5 caracteres").max(255),
  latitud: z.number().min(-90).max(90).nullable(),
  longitud: z.number().min(-180).max(180).nullable(),
  telefono: z.string().regex(telefonoRegex, "Teléfono inválido"),
  correoContacto: z.string().email("Email inválido"),
  categoria: z.number().int().min(1, "Mínimo 1 estrella").max(5, "Máximo 5 estrellas"),
});

export const hotelUpdateSchema = z.object({
  categoria: z.number().int().min(1).max(5),
  telefono: z.string().regex(telefonoRegex, "Teléfono inválido"),
  correoContacto: z.string().email("Email inválido"),
});

export type HotelCreateFormValues = z.infer<typeof hotelCreateSchema>;
export type HotelUpdateFormValues = z.infer<typeof hotelUpdateSchema>;
