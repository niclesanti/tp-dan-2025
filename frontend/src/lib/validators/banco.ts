import { z } from "zod";

export const bancoSchema = z.object({
  nombre: z
    .string()
    .min(1, "El nombre es requerido")
    .max(255, "Máximo 255 caracteres"),
});

export type BancoFormValues = z.infer<typeof bancoSchema>;
