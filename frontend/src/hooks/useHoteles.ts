import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { hotelService } from "@/services/hotel.service";
import type {
  HotelCreateRequest,
  HotelUpdateRequest,
  HabitacionCreateRequest,
  HabitacionUpdateRequest,
  Amenity,
} from "@/types/hotel";
import { toast } from "sonner";

export function useBuscarHoteles(params: {
  nombre?: string;
  categoria?: number;
  domicilio?: string;
  amenity?: Amenity;
  page?: number;
  size?: number;
}) {
  return useQuery({
    queryKey: ["hoteles", params],
    queryFn: () => hotelService.buscarHoteles(params),
    staleTime: 5 * 60 * 1000,
  });
}

export function useHotel(id: number) {
  return useQuery({
    queryKey: ["hoteles", id],
    queryFn: () => hotelService.buscarHotelPorId(id),
    enabled: !!id,
  });
}

export function useBuscarHabitaciones(params: {
  cantidadHuespedes?: number;
  idTipoHabitacion?: number;
  precioMinimo?: number;
  precioMaximo?: number;
  page?: number;
  size?: number;
}) {
  return useQuery({
    queryKey: ["habitaciones", params],
    queryFn: () => hotelService.buscarHabitaciones(params),
    staleTime: 5 * 60 * 1000,
  });
}

export function useTarifaVigente(habitacionId: number) {
  return useQuery({
    queryKey: ["tarifa-vigente", habitacionId],
    queryFn: () => hotelService.obtenerTarifaVigente(habitacionId),
    enabled: !!habitacionId,
  });
}

export function useTiposHabitacion() {
  return useQuery({
    queryKey: ["tipos-habitacion"],
    queryFn: () => hotelService.listarTiposHabitacion(),
    staleTime: 10 * 60 * 1000,
  });
}

export function useCrearHotel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: HotelCreateRequest) => hotelService.crearHotel(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["hoteles"] });
      toast.success("Hotel creado exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useActualizarHotel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: HotelUpdateRequest }) =>
      hotelService.actualizarHotel(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["hoteles"] });
      toast.success("Hotel actualizado exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useCerrarHotel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => hotelService.cerrarHotel(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["hoteles"] });
      toast.success("Hotel cerrado exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useAgregarAmenities() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, amenities }: { id: number; amenities: Amenity[] }) =>
      hotelService.agregarAmenities(id, amenities),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["hoteles"] });
      toast.success("Amenities actualizadas");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useEliminarAmenity() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, amenityId }: { id: number; amenityId: number }) =>
      hotelService.eliminarAmenity(id, amenityId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["hoteles"] });
      toast.success("Amenity eliminada");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useCrearHabitacion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: HabitacionCreateRequest) => hotelService.crearHabitacion(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["habitaciones"] });
      toast.success("Habitación creada exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useActualizarHabitacion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: HabitacionUpdateRequest }) =>
      hotelService.actualizarHabitacion(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["habitaciones"] });
      toast.success("Habitación actualizada exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useEliminarHabitacion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => hotelService.eliminarHabitacion(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["habitaciones"] });
      toast.success("Habitación eliminada exitosamente");
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

const HOTELES_DROPDOWN_PARAMS = { page: 0, size: 1000 } as const;

export function useHotelesDropdown() {
  return useQuery({
    queryKey: ["hoteles", "dropdown", HOTELES_DROPDOWN_PARAMS],
    queryFn: () => hotelService.buscarHoteles(HOTELES_DROPDOWN_PARAMS),
    select: (data) => data.content,
    staleTime: 10 * 60 * 1000,
  });
}
