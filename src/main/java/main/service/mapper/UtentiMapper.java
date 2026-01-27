package main.service.mapper;

import main.domain.Utenti;
import main.service.dto.UtentiDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Utenti} and its DTO {@link UtentiDTO}.
 */
@Mapper(componentModel = "spring")
public interface UtentiMapper extends EntityMapper<UtentiDTO, Utenti> {
    @Named("nome")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    UtentiDTO toDtoNome(Utenti utenti);
}
