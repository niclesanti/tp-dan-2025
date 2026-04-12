package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TarifaService {

    TarifaDTOResponse crearTarifa(TarifaDTORequest request);

    TarifaDTOResponse buscarTarifaPorId(Integer id);

    Page<TarifaDTOResponse> buscarTarifas(Pageable pageable);

    void eliminarTarifa(Integer id);
}
