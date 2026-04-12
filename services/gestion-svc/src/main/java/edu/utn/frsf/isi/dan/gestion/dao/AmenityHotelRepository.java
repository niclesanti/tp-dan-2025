package edu.utn.frsf.isi.dan.gestion.dao;

import edu.utn.frsf.isi.dan.gestion.model.AmenityHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmenityHotelRepository extends JpaRepository<AmenityHotel, Long> {

    Optional<AmenityHotel> findByIdAndHotelId(Long id, Integer hotelId);
}
