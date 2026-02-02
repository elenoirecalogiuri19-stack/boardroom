package main.service.mapper;

import java.util.List;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.service.dto.PrenotazioniDTO;
import main.service.dto.StatiPrenotazioneDTO;
import main.service.dto.UtentiDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { UtentiMapper.class, SaleMapper.class, StatiPrenotazioneMapper.class })
public interface PrenotazioniMapper extends EntityMapper<PrenotazioniDTO, Prenotazioni> {
    @Override
    @Mapping(target = "titoloEvento", source = "evento.titolo")
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

    // I Named methods rimangono per la gestione specifica dei campi
    @Named("statiPrenotazioneCodice")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codice", source = "codice")
    StatiPrenotazioneDTO toDtoStatiPrenotazioneCodice(StatiPrenotazione statiPrenotazione);

    @Named("utentiNome")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    UtentiDTO toDtoUtentiNome(Utenti utenti);
}
