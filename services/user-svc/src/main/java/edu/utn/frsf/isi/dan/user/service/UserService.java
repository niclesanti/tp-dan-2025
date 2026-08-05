package edu.utn.frsf.isi.dan.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.TarjetaPrincipalDTO;
import edu.utn.frsf.isi.dan.user.dto.UsuarioDTOResponse;

public interface UserService {

    // Gestión de usuarios huéspedes
    public HuespedDTOResponse createUsuarioHuesped(HuespedDTORequest huespedRequest);
    public HuespedDTOResponse updateUsuarioHuesped(Integer id, HuespedDTOUpdate huespedUpdate);
    public void deleteUsuarioHuesped(Integer id);

    // Gestion de propietarios
    public PropietarioDTOResponse createUsuarioPropietario(PropietarioDTORequest propietarioRequest);
    public PropietarioDTOResponse updateUsuarioPropietario(Integer id, PropietarioDTOUpdate propietarioUpdate);
    public void deleteUsuarioPropietario(Integer id);
    
    // Búsqueda usuarios
    public Page<UsuarioDTOResponse> buscarPorNombre(String nombre, Pageable pageable);
    public Page<UsuarioDTOResponse> buscarPorDni(String dni, Pageable pageable);
    public UsuarioDTOResponse buscarPorDniExacto(String dni);

    // Métodos de gestión de tarjetas de crédito
    TarjetaCreditoDTOResponse agregarTarjeta(Integer huespedId, TarjetaCreditoDTORequest tarjetacreditoRequest);
    void eliminarTarjeta(Integer huespedId, Integer tarjetaId);
    TarjetaCreditoDTOResponse cambiarTarjetaPrincipal(Integer huespedId, Integer tarjetaId);
    Page<TarjetaCreditoDTOResponse> listarTarjetas(Integer huespedId, Pageable pageable);
    TarjetaPrincipalDTO obtenerTarjetaPrincipalPorDni(String dni);
}
