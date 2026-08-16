package edu.utn.frsf.isi.dan.reservas_svc.mapper;

import edu.utn.frsf.isi.dan.reservas_svc.dto.ReviewDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReviewDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructConfig.class)
public interface ReviewMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now().toString())")
    Review toEntity(ReviewDTORequest dto);

    ReviewDTOResponse toResponse(Review review);
}
