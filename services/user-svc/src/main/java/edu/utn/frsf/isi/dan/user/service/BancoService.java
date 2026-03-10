package edu.utn.frsf.isi.dan.user.service;

import java.util.List;

import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;

public interface BancoService {

    // Métodos para gestión de bancos
    BancoDTOResponse crearBanco(BancoDTORequest bancoRequest);
    BancoDTOResponse actualizarBanco(Integer id, BancoDTOUpdate bancoUpdate);
    void eliminarBanco(Integer id);

    // Métodos para consulta de bancos
    BancoDTOResponse buscarBancoPorId(Integer id);
    List<BancoDTOResponse> listarBancos();

}
