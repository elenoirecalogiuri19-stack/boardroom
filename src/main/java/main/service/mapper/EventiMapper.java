package main.service.mapper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.service.dto.EventiDTO;
import main.service.dto.PrenotazioniDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Eventi} and its DTO {@link EventiDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventiMapper extends EntityMapper<EventiDTO, Eventi> {
    @Mapping(target = "prenotazioneId", source = "prenotazione.id")
    @Mapping(target = "data", source = "prenotazione.data")
    @Mapping(target = "oraInizio", source = "prenotazione.oraInizio")
    @Mapping(target = "oraFine", source = "prenotazione.oraFine")
    @Mapping(target = "salaNome", source = "prenotazione.sala.nome")
    EventiDTO toDto(Eventi s);

    @Mapping(target = "prenotazione", source = "prenotazioneId")
    Eventi toEntity(EventiDTO dto);

    default Prenotazioni fromId(UUID id) {
        if (id == null) return null;
        Prenotazioni p = new Prenotazioni();
        p.setId(id);
        return p;
    }

    List<EventiDTO> toDto(List<Eventi> eventiList);

    @Named("prenotazioniId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PrenotazioniDTO toDtoPrenotazioniId(Prenotazioni prenotazioni);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
