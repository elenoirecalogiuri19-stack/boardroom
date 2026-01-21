package main.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrenotazioniRepository extends JpaRepository<Prenotazioni, UUID> {
    @Query(
        "select p from Prenotazioni p where p.data = :data " +
        "and p.stato.codice != 'CANCELLED' " +
        "and (p.oraInizio < :fine and p.oraFine > :inizio)"
    )
    List<Prenotazioni> findOccupiedRooms(@Param("data") LocalDate data, @Param("inizio") LocalTime inizio, @Param("fine") LocalTime fine);

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
}
