package main.repository;

import java.util.List;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.enumeration.TipoEvento;
import main.service.dto.EventiDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Eventi entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventiRepository extends JpaRepository<Eventi, UUID> {
    List<Eventi> findByTipo(TipoEvento tipo);
}
