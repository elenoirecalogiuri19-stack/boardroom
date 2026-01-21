package main.repository;

import java.util.UUID;
import main.domain.Eventi;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Eventi entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventiRepository extends JpaRepository<Eventi, UUID> {}
