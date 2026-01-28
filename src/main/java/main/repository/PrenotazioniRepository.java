package main.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrenotazioniRepository extends JpaRepository<Prenotazioni, UUID> {
    /**
     * Controlla se esiste una prenotazione confermata che si sovrappone
     * a una data/ora per una specifica sala.
     */
    @Query(
        "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Prenotazioni p " +
        "WHERE p.sala = :sala " +
        "AND p.data = :data " +
        "AND p.oraInizio < :oraFine " +
        "AND p.oraFine > :oraInizio " +
        "AND p.stato.codice = 'CONFIRMED'"
    )
    boolean existsOverlappingConfirmedPrenotazione(
        @Param("sala") Sale sala,
        @Param("data") LocalDate data,
        @Param("oraInizio") LocalTime oraInizio,
        @Param("oraFine") LocalTime oraFine
    );

    /**
     * Trova tutte le prenotazioni di una sala specifica con paginazione.
     */
    @Query("SELECT p FROM Prenotazioni p WHERE p.sala.id = :salaId")
    Page<Prenotazioni> findBySalaId(@Param("salaId") UUID salaId, Pageable pageable);

    @Query(
        """
            SELECT p
            FROM Prenotazioni p
            WHERE p.data < :oggi
            ORDER BY p.data DESC, p.oraInizio DESC
        """
    )
    List<Prenotazioni> findStorico(@Param("oggi") LocalDate oggi);

    @Query(
        """
            SELECT p
            FROM Prenotazioni p
            WHERE p.data >= :oggi
            ORDER BY p.data ASC, p.oraInizio ASC
        """
    )
    List<Prenotazioni> findOdierneEFuture(@Param("oggi") LocalDate oggi);
}
