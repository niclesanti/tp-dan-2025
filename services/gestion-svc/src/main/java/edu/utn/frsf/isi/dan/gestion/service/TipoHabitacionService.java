package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dto.*;
import java.util.List;

public interface TipoHabitacionService {

    /**
     * Crea un nuevo tipo de habitación.
     */
    TipoHabitacionDTOResponse crearTipoHabitacion(TipoHabitacionDTORequest request);

    /**
     * Busca un tipo de habitación por ID.
     */
    TipoHabitacionDTOResponse buscarTipoHabitacionPorId(Integer id);

    /**
     * Busca todos los tipos de habitación.
     */
    List<TipoHabitacionDTOResponse> buscarTiposHabitacion();

    /**
     * Actualiza los datos de un tipo de habitación existente.
     */
    TipoHabitacionDTOResponse actualizarTipoHabitacion(Integer id, TipoHabitacionDTOUpdate request);

    /**
     * Elimina un tipo de habitación por ID.
     */
    void eliminarTipoHabitacion(Integer id);
}
