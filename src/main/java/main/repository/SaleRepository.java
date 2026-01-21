package main.repository;

import java.util.UUID;
import main.domain.Sale;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    @Query("select s from Sale s where s.id not in (select e.prenotazione.id from Eventi e where e.prenotazione.id is not null)")
    List<Sale> findAllFreeSales();
}
