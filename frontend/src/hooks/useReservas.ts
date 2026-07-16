import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { reservaService } from "@/services/reserva.service";
import type {
  ReservaDTORequest,
  PagoDTORequest,
  ReviewDTORequest,
} from "@/types/reserva";
import { toast } from "sonner";

// --- Queries ---

export function useBuscarHabitacionesDisponibles(
  params: {
    checkIn: string;
    checkOut: string;
    capacidad?: number;
    precioMin?: number;
    precioMax?: number;
    categoriaHotel?: number;
    page?: number;
    size?: number;
  },
  enabled = true
) {
  return useQuery({
    queryKey: ["habitaciones-disponibles", params],
    queryFn: () => reservaService.buscarHabitacionesDisponibles(params),
    staleTime: 2 * 60 * 1000,
    enabled,
  });
}

export function useReservasPorHuesped(
  huespedId: string,
  params?: { page?: number; size?: number }
) {
  return useQuery({
    queryKey: ["reservas-huesped", huespedId, params],
    queryFn: () => reservaService.buscarReservasPorHuesped(huespedId, params),
    enabled: !!huespedId,
    staleTime: 30 * 1000,
  });
}

export function useReserva(id: string) {
  return useQuery({
    queryKey: ["reserva", id],
    queryFn: () => reservaService.obtenerReserva(id),
    enabled: !!id,
  });
}

// --- Mutations ---

export function useCrearReserva() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ReservaDTORequest) => reservaService.crearReserva(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reservas-huesped"] });
      toast.success("Reserva creada exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useActualizarEstadoReserva() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, estado }: { id: string; estado: string }) =>
      reservaService.actualizarEstado(id, estado),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reservas-huesped"] });
      qc.invalidateQueries({ queryKey: ["reserva"] });
      toast.success("Estado actualizado exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useCheckIn() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => reservaService.checkIn(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reservas-huesped"] });
      qc.invalidateQueries({ queryKey: ["reserva"] });
      toast.success("Check-in realizado exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useAgregarPago() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: PagoDTORequest }) =>
      reservaService.agregarPago(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reservas-huesped"] });
      qc.invalidateQueries({ queryKey: ["reserva"] });
      toast.success("Pago registrado exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useReviewCliente() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ReviewDTORequest }) =>
      reservaService.reviewCliente(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reservas-huesped"] });
      qc.invalidateQueries({ queryKey: ["reserva"] });
      toast.success("Review del cliente enviada");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useReviewHost() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ReviewDTORequest }) =>
      reservaService.reviewHost(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reservas-huesped"] });
      qc.invalidateQueries({ queryKey: ["reserva"] });
      toast.success("Review del host enviada");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useCancelarReserva() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => reservaService.cancelarReserva(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reservas-huesped"] });
      qc.invalidateQueries({ queryKey: ["reserva"] });
      toast.success("Reserva cancelada");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}
