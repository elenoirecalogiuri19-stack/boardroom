package main.service.mapper;

import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.service.dto.PrenotazioniDTO;
import main.service.dto.SaleDTO;
import main.service.dto.StatiPrenotazioneDTO;
import main.service.dto.UtentiDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Prenotazioni} and its DTO {@link PrenotazioniDTO}.
 */
@Mapper(componentModel = "spring")
public interface PrenotazioniMapper extends EntityMapper<PrenotazioniDTO, Prenotazioni> {
    @Mapping(target = "stato", source = "stato")
    @Mapping(target = "utente", source = "utente")
    @Mapping(target = "sala", source = "sala")
    PrenotazioniDTO toDto(Prenotazioni s);

    @Override
    @Mapping(target = "utente", ignore = true)
    @Mapping(target = "sala", ignore = true)
    Prenotazioni toEntity(PrenotazioniDTO dto);

    @Named("statiPrenotazioneCodice")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codice", source = "codice")
    @Mapping(target = "descrizione", source = "descrizione")
    @Mapping(target = "ordineAzione", source = "ordineAzione")
    StatiPrenotazioneDTO toDtoStatiPrenotazioneCodice(StatiPrenotazione statiPrenotazione);

    @Named("utentiNome")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    UtentiDTO toDtoUtentiNome(Utenti utenti);

    @Named("saleNome")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    SaleDTO toDtoSaleNome(Sale sale);
}
