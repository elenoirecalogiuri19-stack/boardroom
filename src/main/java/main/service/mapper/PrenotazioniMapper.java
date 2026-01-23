package main.service.mapper;

import main.domain.Prenotazioni;
import main.service.dto.PrenotazioniDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { StatiPrenotazioneMapper.class, UtentiMapper.class, SaleMapper.class })
public interface PrenotazioniMapper extends EntityMapper<PrenotazioniDTO, Prenotazioni> {
    @Mapping(target = "stato", source = "stato")
    @Mapping(target = "utente", source = "utente")
    @Mapping(target = "sala", source = "sala")
    PrenotazioniDTO toDto(Prenotazioni s);

    @Mapping(target = "stato", source = "stato")
    @Mapping(target = "utente", source = "utente")
    @Mapping(target = "sala", source = "sala")
    Prenotazioni toEntity(PrenotazioniDTO dto);
}
