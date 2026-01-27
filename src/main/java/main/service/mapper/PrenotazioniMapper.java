package main.service.mapper;

import main.domain.Prenotazioni;
import main.service.dto.PrenotazioniDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Prenotazioni} and its DTO {@link PrenotazioniDTO}.
 */
@Mapper(componentModel = "spring", uses = { UtentiMapper.class, SaleMapper.class, StatiPrenotazioneMapper.class })
public interface PrenotazioniMapper extends EntityMapper<PrenotazioniDTO, Prenotazioni> {
    @Mapping(target = "utente", source = "utente")
    @Mapping(target = "sala", source = "sala")
    @Mapping(target = "stato", source = "stato")
    PrenotazioniDTO toDto(Prenotazioni s);

    @Override
    @Mapping(target = "utente", ignore = true)
    @Mapping(target = "sala", ignore = true)
    @Mapping(target = "stato", ignore = true)
    Prenotazioni toEntity(PrenotazioniDTO dto);
}
