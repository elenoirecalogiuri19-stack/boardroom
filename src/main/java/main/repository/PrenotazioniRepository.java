package main.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrenotazioniRepository extends JpaRepository<Prenotazioni, UUID> {
    default Optional<Prenotazioni> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Prenotazioni> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Prenotazioni> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    Page<Prenotazioni> findBySalaId(UUID salaId, Pageable pageable);

    @Query(
        value = "select p from Prenotazioni p left join fetch p.stato left join fetch p.utente left join fetch p.sala",
        countQuery = "select count(p) from Prenotazioni p"
    )
    Page<Prenotazioni> findAllWithToOneRelationships(Pageable pageable);

    @Query("select p from Prenotazioni p left join fetch p.stato left join fetch p.utente left join fetch p.sala")
    List<Prenotazioni> findAllWithToOneRelationships();

    @Query("select p from Prenotazioni p left join fetch p.stato left join fetch p.utente left join fetch p.sala where p.id =:id")
    Optional<Prenotazioni> findOneWithToOneRelationships(@Param("id") UUID id);

    @Query(
        "SELECT COUNT(p) > 0 FROM Prenotazioni p WHERE p.sala = :sala AND p.data = :data " +
        "AND ((p.oraInizio < :oraFine) AND (p.oraFine > :oraInizio)) " +
        "AND p.stato.codice = main.domain.enumeration.StatoCodice.CONFIRMED"
    )
    boolean existsOverlappingConfirmedPrenotazione(
        @Param("sala") Sale sala,
        @Param("data") LocalDate data,
        @Param("oraInizio") LocalTime oraInizio,
        @Param("oraFine") LocalTime oraFine
    );
}
