package main.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Prenotazioni entity.
 */
@Repository
public interface PrenotazioniRepository extends JpaRepository<Prenotazioni, UUID> {
    // US2: Metodo aggiunto per filtrare le prenotazioni per sala
    Page<Prenotazioni> findBySalaId(UUID salaId, Pageable pageable);

    default Optional<Prenotazioni> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Prenotazioni> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Prenotazioni> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select prenotazioni from Prenotazioni prenotazioni left join fetch prenotazioni.stato left join fetch prenotazioni.utente left join fetch prenotazioni.sala",
        countQuery = "select count(prenotazioni) from Prenotazioni prenotazioni"
    )
    Page<Prenotazioni> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select prenotazioni from Prenotazioni prenotazioni left join fetch prenotazioni.stato left join fetch prenotazioni.utente left join fetch prenotazioni.sala"
    )
    List<Prenotazioni> findAllWithToOneRelationships();

    @Query(
        "select prenotazioni from Prenotazioni prenotazioni left join fetch prenotazioni.stato left join fetch prenotazioni.utente left join fetch prenotazioni.sala where prenotazioni.id =:id"
    )
    Optional<Prenotazioni> findOneWithToOneRelationships(@Param("id") UUID id);

    // inplementazione metodo per la disponibilita sale

    @Query(
        "SELECT COUNT(p) > 0 " +
        "FROM Prenotazioni p WHERE p.sala = :sala AND p.data = :data " +
        "AND ((p.oraInizio < :oraFine) AND (p.oraFine > :oraInizio)) " +
        "AND p.stato.codice = 'CONFIRMED'"
    )
    boolean existsOverlappingConfirmedPrenotazione(
        @Param("sala") Sale sala,
        @Param("data") LocalDate data,
        @Param("oraInizio") LocalTime oraInizio,
        @Param("oraFine") LocalTime oraFine
    );
}
