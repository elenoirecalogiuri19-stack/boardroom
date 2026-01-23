package main.repository;

import java.util.Optional;
import java.util.UUID;
import main.domain.StatiPrenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatiPrenotazioneRepository extends JpaRepository<StatiPrenotazione, UUID> {
    Optional<StatiPrenotazione> findByCodice(String codice);
}
