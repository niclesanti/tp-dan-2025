import { z } from "zod";

const telefonoRegex = /^[0-9+\-\s()]{7,20}$/;
const dniRegex = /^\d{7,8}$/;
const numeroTarjetaRegex = /^\d{13,22}$/;
const fechaVencimientoRegex = /^(0[1-9]|1[0-2])\/([0-9]{2})$/;
const cvcRegex = /^\d{3,4}$/;
const fechaNacimientoRegex = /^\d{4}-\d{2}-\d{2}$/;

export const tarjetaSchema = z.object({
  numero: z
    .string()
    .regex(numeroTarjetaRegex, "El número debe tener entre 13 y 22 dígitos"),
  nombreTitular: z
    .string()
    .min(3, "Mínimo 3 caracteres")
    .max(255, "Máximo 255 caracteres"),
  fechaVencimiento: z
    .string()
    .regex(fechaVencimientoRegex, "Formato MM/YY"),
  cvc: z.string().regex(cvcRegex, "CVC debe tener 3 o 4 dígitos"),
  esPrincipal: z.boolean(),
  bancoId: z.number().int().positive("Seleccioná un banco"),
});

export const huespedSchema = z.object({
  nombre: z
    .string()
    .min(2, "Mínimo 2 caracteres")
    .max(255, "Máximo 255 caracteres"),
  email: z.string().email("Email inválido"),
  telefono: z
    .string()
    .regex(telefonoRegex, "Formato de teléfono inválido"),
  dni: z.string().regex(dniRegex, "DNI debe tener 7 u 8 dígitos"),
  fechaNacimiento: z
    .string()
    .regex(fechaNacimientoRegex, "Formato de fecha inválido (YYYY-MM-DD)"),
  tarjetaCredito: tarjetaSchema,
});

export const huespedUpdateSchema = z.object({
  nombre: z
    .string()
    .min(2, "Mínimo 2 caracteres")
    .max(255, "Máximo 255 caracteres"),
  email: z.string().email("Email inválido"),
  telefono: z
    .string()
    .regex(telefonoRegex, "Formato de teléfono inválido"),
  dni: z.string().regex(dniRegex, "DNI debe tener 7 u 8 dígitos"),
  fechaNacimiento: z
    .string()
    .regex(fechaNacimientoRegex, "Formato de fecha inválido (YYYY-MM-DD)"),
});

export type HuespedFormValues = z.infer<typeof huespedSchema>;
export type HuespedUpdateFormValues = z.infer<typeof huespedUpdateSchema>;
export type TarjetaFormValues = z.infer<typeof tarjetaSchema>;
