import { z } from "zod";

const telefonoRegex = /^[0-9+\-\s()]{7,20}$/;
const dniRegex = /^\d{7,8}$/;
const cbuRegex = /^\d{22}$/;
const aliasRegex = /^[a-zA-Z0-9.]+$/;

export const cuentaBancariaSchema = z.object({
  numeroCuenta: z
    .string()
    .min(6, "Mínimo 6 caracteres")
    .max(255, "Máximo 255 caracteres"),
  cbu: z.string().regex(cbuRegex, "CBU debe tener exactamente 22 dígitos"),
  alias: z
    .string()
    .min(6, "Mínimo 6 caracteres")
    .max(255, "Máximo 255 caracteres")
    .regex(aliasRegex, "Solo letras, números y puntos"),
  bancoId: z.number().int().positive("Seleccioná un banco"),
});

export const propietarioSchema = z.object({
  nombre: z
    .string()
    .min(2, "Mínimo 2 caracteres")
    .max(255, "Máximo 255 caracteres"),
  email: z.string().email("Email inválido"),
  telefono: z
    .string()
    .regex(telefonoRegex, "Formato de teléfono inválido"),
  dni: z.string().regex(dniRegex, "DNI debe tener 7 u 8 dígitos"),
  cuentaBancaria: cuentaBancariaSchema,
  idHotel: z.number().int().positive().nullable(),
});

export const propietarioUpdateSchema = z.object({
  nombre: z
    .string()
    .min(2, "Mínimo 2 caracteres")
    .max(255, "Máximo 255 caracteres"),
  email: z.string().email("Email inválido"),
  telefono: z
    .string()
    .regex(telefonoRegex, "Formato de teléfono inválido"),
  dni: z.string().regex(dniRegex, "DNI debe tener 7 u 8 dígitos"),
  idHotel: z.number().int().positive().nullable(),
});

export type PropietarioFormValues = z.infer<typeof propietarioSchema>;
export type PropietarioUpdateFormValues = z.infer<typeof propietarioUpdateSchema>;
export type CuentaBancariaFormValues = z.infer<typeof cuentaBancariaSchema>;
