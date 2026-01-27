package main.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.domain.Sale;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Sale entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    // Questo è il metodo che mancava e che ha bloccato il build!
    @Query(
        "select s from Sale s where s.id not in (select p.sala.id from Prenotazioni p where p.data = :data and " +
        "((p.oraInizio <= :oraInizio and p.oraFine > :oraInizio) or " +
        "(p.oraInizio < :oraFine and p.oraFine >= :oraFine) or " +
        "(p.oraInizio >= :oraInizio and p.oraFine <= :oraFine)))"
    )
    List<Sale> findFreeSales(@Param("data") LocalDate data, @Param("oraInizio") LocalTime oraInizio, @Param("oraFine") LocalTime oraFine);
}
