package main.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.domain.Sale;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    @Query(
        "SELECT s FROM Sale s WHERE s.id NOT IN (" +
        "SELECT p.sala.id FROM Prenotazioni p " +
        "WHERE p.data = :data " +
        "AND p.oraInizio < :fine " +
        "AND p.oraFine > :inizio " +
        "AND p.stato.codice != 'CANCELLED')"
    )
    List<Sale> findFreeSales(@Param("data") LocalDate data, @Param("inizio") LocalTime inizio, @Param("fine") LocalTime fine);
}
