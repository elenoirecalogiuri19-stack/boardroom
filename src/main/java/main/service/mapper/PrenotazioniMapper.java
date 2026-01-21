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
    @Mapping(target = "stato", source = "stato", qualifiedByName = "statiPrenotazioneCodice")
    @Mapping(target = "utente", source = "utente", qualifiedByName = "utentiNome")
    @Mapping(target = "sala", source = "sala", qualifiedByName = "saleNome")
    PrenotazioniDTO toDto(Prenotazioni s);

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

    @Named("saleNome")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    SaleDTO toDtoSaleNome(Sale sale);
}
