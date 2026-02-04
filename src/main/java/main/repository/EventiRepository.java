package main.repository;

import java.util.List;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventiRepository extends JpaRepository<Eventi, UUID> {
    @Query("SELECT e FROM Eventi e WHERE e.tipo = :tipo AND e.prenotazione.stato.codice = :stato")
    List<Eventi> findPublicConfirmed(@Param("tipo") TipoEvento tipo, @Param("stato") StatoCodice stato);
}
