package edu.utn.frsf.isi.dan.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.UsuarioDTOResponse;

public interface UserService {

    public HuespedDTOResponse createUsuarioHuesped(HuespedDTORequest huespedRequest);

    public HuespedDTOResponse updateUsuarioHuesped(Integer id, HuespedDTOUpdate huespedUpdate);

    public void deleteUsuarioHuesped(Integer id);

    public void createUsuarioPropietario(PropietarioDTORequest propietarioRequest);

    public Page<UsuarioDTOResponse> buscarPorNombre(String nombre, Pageable pageable);

    public Page<UsuarioDTOResponse> buscarPorDni(String dni, Pageable pageable);

    public UsuarioDTOResponse buscarPorDniExacto(String dni);
}
