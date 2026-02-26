package edu.utn.frsf.isi.dan.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOUpdate;
import edu.utn.frsf.isi.dan.user.model.Usuario;

public interface UserService {

    // Gestión de usuarios huéspedes
    public HuespedDTOResponse createUsuarioHuesped(HuespedDTORequest huespedRequest);
    public HuespedDTOResponse updateUsuarioHuesped(Integer id, HuespedDTOUpdate huespedUpdate);
    public void deleteUsuarioHuesped(Integer id);

    // Gestion de propietarios
    public PropietarioDTOResponse createUsuarioPropietario(PropietarioDTORequest propietarioRequest);
    public PropietarioDTOResponse updateUsuarioPropietario(Integer id, PropietarioDTOUpdate propietarioUpdate);
    public void deleteUsuarioPropietario(Integer id);

    // Búsqueda de usuarios
    public Page<Usuario> buscarPorNombre(String nombre, Pageable pageable);
    public Page<Usuario> buscarPorDni(String dni, Pageable pageable);
    public Usuario buscarPorDniExacto(String dni);
}
