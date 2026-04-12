package edu.utn.frsf.isi.dan.gestion.dao;

import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    @Query("SELECT t FROM Tarifa t WHERE t.tipoHabitacion.id = :tipoHabitacionId " +
	    "AND t.fechaInicio <= :fecha " +
	    "AND (t.fechaFin IS NULL OR t.fechaFin >= :fecha) " +
	    "ORDER BY t.fechaInicio DESC")
	    List<Tarifa> buscarTarifasVigentesEnFecha(@Param("tipoHabitacionId") Integer tipoHabitacionId,
						      @Param("fecha") LocalDate fecha);

    @Query("SELECT t FROM Tarifa t WHERE t.tipoHabitacion.id = :tipoHabitacionId " +
	    "AND t.fechaInicio < :fechaReferencia " +
	    "ORDER BY t.fechaInicio DESC")
	    List<Tarifa> buscarTarifasAnteriores(@Param("tipoHabitacionId") Integer tipoHabitacionId,
						 @Param("fechaReferencia") LocalDate fechaReferencia);

    long countByTipoHabitacionId(Integer tipoHabitacionId);
}
