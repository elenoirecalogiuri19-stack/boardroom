package main.service.mapper;

import main.domain.User;
import main.domain.Utenti;
import main.service.dto.UserDTO;
import main.service.dto.UtentiDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Utenti} and its DTO {@link UtentiDTO}.
 */
@Mapper(componentModel = "spring")
public interface UtentiMapper extends EntityMapper<UtentiDTO, Utenti> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    UtentiDTO toDto(Utenti s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
