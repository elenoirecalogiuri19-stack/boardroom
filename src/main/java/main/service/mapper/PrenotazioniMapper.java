package main.service.mapper;

import main.domain.Prenotazioni;
import main.service.dto.PrenotazioniDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { UtentiMapper.class, SaleMapper.class, StatiPrenotazioneMapper.class })
public interface PrenotazioniMapper extends EntityMapper<PrenotazioniDTO, Prenotazioni> {
    @Override
    @Mapping(target = "data", source = "data")
    @Mapping(target = "oraInizio", source = "oraInizio")
    @Mapping(target = "oraFine", source = "oraFine")
    @Mapping(target = "sala", source = "sala")
    @Mapping(target = "stato", source = "stato")
    @Mapping(target = "utente", source = "utente")
    @Mapping(target = "eventoId", source = "evento.id")
    PrenotazioniDTO toDto(Prenotazioni entity);

    @Override
    @Mapping(target = "utente", source = "utente")
    @Mapping(target = "sala", source = "sala")
    Prenotazioni toEntity(PrenotazioniDTO dto);
}
