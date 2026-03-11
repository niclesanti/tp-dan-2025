package edu.utn.frsf.isi.dan.user.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;

@Repository
public interface TarjetaCreditoRepository extends JpaRepository<TarjetaCredito, Integer> {

    Optional<TarjetaCredito> findByHuespedIdAndEsPrincipalTrue(Integer huespedId);

    Page<TarjetaCredito> findByHuespedId(Integer huespedId, Pageable pageable);
}