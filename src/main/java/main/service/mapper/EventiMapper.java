package main.service.mapper;

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
    @Mapping(target = "prenotazione", source = "prenotazione", qualifiedByName = "prenotazioniId")
    EventiDTO toDto(Eventi s);

    @Named("prenotazioniId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PrenotazioniDTO toDtoPrenotazioniId(Prenotazioni prenotazioni);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
