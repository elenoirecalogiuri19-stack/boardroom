package main.repository;

import java.util.UUID;
import main.domain.PrenotazioneEventoPubblico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrenotazioneEventoPubblicoRepository extends JpaRepository<PrenotazioneEventoPubblico, UUID> {
    /**
     * Conta il numero di posti già prenotati per un evento pubblico.
     */
    @Query("SELECT COUNT(p) FROM PrenotazioneEventoPubblico p WHERE p.evento.id = :eventoId")
    long countByEventoId(@Param("eventoId") UUID eventoId);

    /**
     * Restituisce la sequenza del prossimo partecipante (1, 2, 3...).
     * Usata per generare il codice persona: A01, A02, ecc.
     */
    default long nextSequenzaPerEvento(UUID eventoId) {
        return countByEventoId(eventoId) + 1;
    }
}
