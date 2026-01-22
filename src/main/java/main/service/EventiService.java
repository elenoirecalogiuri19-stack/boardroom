package main.service;

import java.math.BigDecimal; // Import fondamentale per gestire i prezzi
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.Eventi;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.service.dto.EventiDTO;
import main.service.mapper.EventiMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link main.domain.Eventi}.
 */
@Service
@Transactional
public class EventiService {

    private static final Logger LOG = LoggerFactory.getLogger(EventiService.class);

    private final EventiRepository eventiRepository;

    private final EventiMapper eventiMapper;

    public EventiService(EventiRepository eventiRepository, EventiMapper eventiMapper) {
        this.eventiRepository = eventiRepository;
        this.eventiMapper = eventiMapper;
    }

    /**
     * Save a eventi.
     */
    public EventiDTO save(EventiDTO eventiDTO) {
        LOG.debug("Request to save Eventi : {}", eventiDTO);
        applicaLogicaPrezzo(eventiDTO); // Applica regola US4 prima del salvataggio
        Eventi eventi = eventiMapper.toEntity(eventiDTO);
        eventi = eventiRepository.save(eventi);
        return eventiMapper.toDto(eventi);
    }

    /**
     * Update a eventi.
     */
    public EventiDTO update(EventiDTO eventiDTO) {
        LOG.debug("Request to update Eventi : {}", eventiDTO);
        applicaLogicaPrezzo(eventiDTO); // Applica regola US4 prima dell'aggiornamento
        Eventi eventi = eventiMapper.toEntity(eventiDTO);
        eventi = eventiRepository.save(eventi);
        return eventiMapper.toDto(eventi);
    }

    /**
     * Logica US4: Se l'evento è PRIVATO, il prezzo deve essere forzato a 0.
     */
    private void applicaLogicaPrezzo(EventiDTO eventiDTO) {
        if (eventiDTO.getTipo() != null && eventiDTO.getTipo().equals(TipoEvento.PRIVATO)) {
            LOG.debug("Evento PRIVATO rilevato: forzo il prezzo a 0.0");
            eventiDTO.setPrezzo(BigDecimal.ZERO);
        }
    }

    /**
     * Partially update a eventi.
     */
    public Optional<EventiDTO> partialUpdate(EventiDTO eventiDTO) {
        LOG.debug("Request to partially update Eventi : {}", eventiDTO);

        return eventiRepository
            .findById(eventiDTO.getId())
            .map(existingEventi -> {
                eventiMapper.partialUpdate(existingEventi, eventiDTO);

                // Controllo logica US4 anche per aggiornamenti parziali
                if (existingEventi.getTipo() == TipoEvento.PRIVATO) {
                    existingEventi.setPrezzo(BigDecimal.ZERO);
                }

                return existingEventi;
            })
            .map(eventiRepository::save)
            .map(eventiMapper::toDto);
    }

    /**
     * Get all the eventis.
     */
    @Transactional(readOnly = true)
    public Page<EventiDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Eventis");
        return eventiRepository.findAll(pageable).map(eventiMapper::toDto);
    }

    /**
     * Get one eventi by id.
     */
    @Transactional(readOnly = true)
    public Optional<EventiDTO> findOne(UUID id) {
        LOG.debug("Request to get Eventi : {}", id);
        return eventiRepository.findById(id).map(eventiMapper::toDto);
    }

    /**
     * Delete the eventi by id.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Eventi : {}", id);
        eventiRepository.deleteById(id);
    }

    /**
     * Get all evento publico
     */
    @Transactional(readOnly = true)
    public List<EventiDTO> findPublicEventi() {
        LOG.debug("Request to get all Eventi");
        List<Eventi> eventi = eventiRepository.findByTipo(TipoEvento.PUBBLICO);
        return eventiMapper.toDto(eventi);
    }
}
