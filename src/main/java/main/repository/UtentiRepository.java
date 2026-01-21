package main.repository;

import java.util.UUID;
import main.domain.Utenti;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Utenti entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UtentiRepository extends JpaRepository<Utenti, UUID> {}
