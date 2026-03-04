package main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventiRepository extends JpaRepository<Eventi, UUID> {
    @Query("SELECT e FROM Eventi e LEFT JOIN e.prenotazione p " + "WHERE e.tipo = :tipo AND (p IS NULL OR p.stato.codice = :stato)")
    List<Eventi> findPublicConfirmed(@Param("tipo") TipoEvento tipo, @Param("stato") StatoCodice stato);

    @Query(
        """
            select e from Eventi e
            join fetch e.prenotazione p
            join fetch p.sala
            where e.id = :id
        """
    )
    Optional<Eventi> findByIdWithPrenotazioneAndSala(UUID id);

    /**
     * Carica l'evento con lock pessimistico (PESSIMISTIC_WRITE).
     * Usato in inviaEmailPrenotazione per evitare race condition
     * sul conteggio posti degli eventi pubblici.
     */
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Eventi e WHERE e.id = :id")
    Optional<Eventi> findByIdWithLock(@Param("id") UUID id);
}
