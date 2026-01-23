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

    // US2 – Sale libere: solo prenotazioni CONFIRMED bloccano la sala
    @Query(
        "SELECT s FROM Sale s WHERE s.id NOT IN (" +
<<<<<<< Updated upstream
        "SELECT p.sala.id FROM Prenotazioni p " +
        "WHERE p.data = :data " +
        "AND p.oraInizio < :fine " +
        "AND p.oraFine > :inizio " +
        "AND p.stato.codice != 'CANCELLED')"
=======
            "SELECT p.sala.id FROM Prenotazioni p " +
            "WHERE p.stato.codice = 'CONFIRMED' " +
            "AND p.data = :data " +
            "AND p.oraInizio < :fine " +
            "AND p.oraFine > :inizio)"
>>>>>>> Stashed changes
    )
    List<Sale> findFreeSales(
        @Param("data") LocalDate data,
        @Param("inizio") LocalTime inizio,
        @Param("fine") LocalTime fine
    );
}
