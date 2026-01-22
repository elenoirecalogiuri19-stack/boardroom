package main.repository;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the StatiPrenotazione entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StatiPrenotazioneRepository extends JpaRepository<StatiPrenotazione, UUID> {
    Optional<StatiPrenotazione> findByCodice(@NotNull StatoCodice codice);
}
