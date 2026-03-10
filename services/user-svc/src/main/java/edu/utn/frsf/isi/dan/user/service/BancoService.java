package edu.utn.frsf.isi.dan.user.service;

import java.util.List;

import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOUpdate;

public interface BancoService {

    // Métodos para gestión de bancos
    BancoDTOResponse crearBanco(BancoDTORequest bancoRequest);
    BancoDTOResponse actualizarBanco(Integer id, BancoDTOUpdate bancoUpdate);
    void eliminarBanco(Integer id);

    // Métodos para consulta de bancos
    BancoDTOResponse buscarBancoPorId(Integer id);
    List<BancoDTOResponse> listarBancos();

    // Métodos de gestión de tarjetas de crédito 
    TarjetaCreditoDTOResponse agregarTarjeta(Integer huespedId, TarjetaCreditoDTORequest tarjetacreditoRequest);
    void eliminarTarjeta(Integer huespedId, Integer tarjetaId);

    // Métodos de consulta de tarjetas de crédito
    TarjetaCreditoDTOResponse cambiarTarjetaPrincipal(Integer huespedId, Integer tarjetaId);
    List<TarjetaCreditoDTOResponse> listarTarjetas(Integer huespedId);


}
