package main.repository;

import java.util.List;
import java.util.UUID;
import main.domain.Sale;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    // Questo serve per la US2 (Swagger /api/sales)
    @Query("select sale from Sale sale where sale.id not in (select p.sala.id from Prenotazioni p)")
    List<Sale> findAllFreeSales();
    // Questo (già incluso in JpaRepository) serve per i menu a tendina
    // List<Sale> findAll();
}
