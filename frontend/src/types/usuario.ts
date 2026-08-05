export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  telefono: string;
  dni: string;
  tipo: "HUESPED" | "PROPIETARIO";
}

export interface Huesped extends Usuario {
  fechaNacimiento: string;
  tarjetaCredito: TarjetaCredito[];
}

export interface Propietario extends Usuario {
  cuentaBancaria: CuentaBancaria | null;
  idHotel: number | null;
}

export interface TarjetaCredito {
  id: number;
  numero: string;
  nombreTitular: string;
  fechaVencimiento: string;
  esPrincipal: boolean;
  nombreBanco: string;
}

export interface TarjetaPrincipalDTO {
  numero: string;
}

export interface CuentaBancaria {
  id: number;
  numeroCuenta: string;
  cbu: string;
  alias: string;
  nombreBanco: string;
}

export interface UsuarioBusqueda extends Usuario {
  fechaNacimiento?: string;
  tarjetaCredito?: TarjetaCredito[];
  cuentaBancaria?: CuentaBancaria | null;
  idHotel?: number | null;
}

export interface Banco {
  id: number;
  nombre: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface HuespedCreateRequest {
  nombre: string;
  email: string;
  telefono: string;
  dni: string;
  fechaNacimiento: string;
  tarjetaCredito: TarjetaCreditoCreateRequest;
}

export interface HuespedUpdateRequest {
  nombre: string;
  email: string;
  telefono: string;
  dni: string;
  fechaNacimiento: string;
}

export interface PropietarioCreateRequest {
  nombre: string;
  email: string;
  telefono: string;
  dni: string;
  cuentaBancaria: CuentaBancariaCreateRequest;
  idHotel: number | null;
}

export interface TarjetaCreditoCreateRequest {
  numero: string;
  nombreTitular: string;
  fechaVencimiento: string;
  cvc: string;
  esPrincipal: boolean;
  bancoId: number;
}

export interface CuentaBancariaCreateRequest {
  numeroCuenta: string;
  cbu: string;
  alias: string;
  bancoId: number;
}

export interface BancoCreateRequest {
  nombre: string;
}

export interface BancoUpdateRequest {
  nombre: string;
}
