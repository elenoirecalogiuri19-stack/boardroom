package main.repository;

import java.util.UUID;
import main.domain.StatiPrenotazione;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the StatiPrenotazione entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StatiPrenotazioneRepository extends JpaRepository<StatiPrenotazione, UUID> {}
