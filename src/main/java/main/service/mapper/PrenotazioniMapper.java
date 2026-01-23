package main.service.mapper;

import main.domain.Prenotazioni;
import main.service.dto.PrenotazioniDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PrenotazioniMapper extends EntityMapper<PrenotazioniDTO, Prenotazioni> {

    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "data", source = "data")
    @Mapping(target = "oraInizio", source = "oraInizio")
    @Mapping(target = "oraFine", source = "oraFine")
    PrenotazioniDTO toDto(Prenotazioni s);

    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "data", source = "data")
    @Mapping(target = "oraInizio", source = "oraInizio")
    @Mapping(target = "oraFine", source = "oraFine")
    Prenotazioni toEntity(PrenotazioniDTO dto);
}
