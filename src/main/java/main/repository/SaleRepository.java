package main.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Sale;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    @Query(
        "SELECT s " +
        "FROM Sale s " +
        "WHERE(:capienza IS NULL OR s.capienza >= :capienza)" +
        "AND s.id NOT IN (" +
        "SELECT p.sala.id " +
        "FROM Prenotazioni p " +
        "WHERE p.data = :data " +
        "AND p.oraInizio < :fine " +
        "AND p.oraFine > :inizio " +
        "AND p.stato.codice IN (" +
        "main.domain.enumeration.StatoCodice.CONFIRMED," +
        "main.domain.enumeration.StatoCodice.WAITING) " +
        ")" +
        "ORDER BY s.capienza DESC "
    )
    List<Sale> findFreeSales(
        @Param("data") LocalDate data,
        @Param("inizio") LocalTime c,
        @Param("fine") LocalTime fine,
        @Param("capienza") Integer capienza
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sale s WHERE s.id = :id")
    Optional<Sale> findByIdWithLock(@Param("id") UUID id);
}
