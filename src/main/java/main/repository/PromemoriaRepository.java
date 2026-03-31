package main.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Promemoria;
import main.domain.enumeration.TipoPromemoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromemoriaRepository extends JpaRepository<Promemoria, UUID> {
    /**
     * Query principale dello scheduler.
     *
     * Trova tutti i promemoria che:
     * 1. Sono abilitati (l'utente non li ha disattivati)
     * 2. NON sono ancora stati inviati (anti-duplicati)
     * 3. La data dell'evento è esattamente "oggi + giorniPrima"
     *    → WEEK_BEFORE:     dataEvento = oggi + 7
     *    → TWO_DAYS_BEFORE: dataEvento = oggi + 2
     *    → DAY_BEFORE:      dataEvento = oggi + 1
     *    → SAME_DAY:        dataEvento = oggi + 0
     *
     * Eager fetch di prenotazione, sala, utente, user per evitare N+1.
     */
    @Query(
        """
        SELECT pr FROM Promemoria pr
        LEFT JOIN FETCH pr.prenotazione p
        LEFT JOIN FETCH p.sala
        LEFT JOIN FETCH p.stato
        LEFT JOIN FETCH p.utente u
        LEFT JOIN FETCH u.user
        LEFT JOIN FETCH p.evento e
        WHERE pr.abilitato = true
          AND pr.inviato = false
          AND (
               (pr.tipo = main.domain.enumeration.TipoPromemoria.WEEK_BEFORE
                AND p.data = :oggi + 7)
            OR (pr.tipo = main.domain.enumeration.TipoPromemoria.TWO_DAYS_BEFORE
                AND p.data = :oggi + 2)
            OR (pr.tipo = main.domain.enumeration.TipoPromemoria.DAY_BEFORE
                AND p.data = :oggi + 1)
            OR (pr.tipo = main.domain.enumeration.TipoPromemoria.SAME_DAY
                AND p.data = :oggi)
          )
        """
    )
    List<Promemoria> findDaInviareOggi(@Param("oggi") LocalDate oggi);

    /** Tutti i promemoria di una prenotazione (per visualizzazione frontend) */
    List<Promemoria> findByPrenotazioneId(UUID prenotazioneId);

    /** Trova un promemoria specifico per prenotazione + tipo (per upsert) */
    Optional<Promemoria> findByPrenotazioneIdAndTipo(UUID prenotazioneId, TipoPromemoria tipo);

    /** Tutti i promemoria attivi e non inviati di una prenotazione */
    List<Promemoria> findByPrenotazioneIdAndAbilitatoTrue(UUID prenotazioneId);
}
