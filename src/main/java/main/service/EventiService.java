package main.service;

import java.math.BigDecimal; // Import fondamentale per gestire i prezzi
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.repository.PrenotazioniRepository;
import main.service.dto.EventiDTO;
import main.service.mapper.EventiMapper;
import main.web.rest.errors.BadRequestAlertException;
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

    private final PrenotazioniRepository prenotazioniRepository;

    public EventiService(EventiRepository eventiRepository, EventiMapper eventiMapper, PrenotazioniRepository prenotazioniRepository) {
        this.eventiRepository = eventiRepository;
        this.eventiMapper = eventiMapper;
        this.prenotazioniRepository = prenotazioniRepository;
    }

    /**
     * logica di bisnes creaEvento
     */
    public EventiDTO createEvento(EventiDTO dto) {
        LOG.debug("REST request to save Eventi : {}", dto);
        return save(dto);
    }

    /**
     * Save a eventi.
     */
    public EventiDTO save(EventiDTO eventiDTO) {
        LOG.debug("Request to save Eventi : {}", eventiDTO);
        Eventi eventi = eventiMapper.toEntity(eventiDTO);
        applyPrivateEventRulesEntity(eventi); // logica US4 sul dominio
        eventi = eventiRepository.save(eventi);
        EventiDTO result = eventiMapper.toDto(eventi);
        applyPrivateEventRulesDTO(result);
        return result;
    }

    /**
     * Update a eventi.
     */
    public EventiDTO update(EventiDTO eventiDTO) {
        LOG.debug("Request to update Eventi : {}", eventiDTO);
        Eventi eventi = eventiMapper.toEntity(eventiDTO);
        applyPrivateEventRulesEntity(eventi);
        eventi = eventiRepository.save(eventi);
        EventiDTO result = eventiMapper.toDto(eventi);
        applyPrivateEventRulesDTO(result);
        return result;
    }

    /**
     * Partially update a eventi.
     */
    public Optional<EventiDTO> partialUpdate(EventiDTO eventiDTO) {
        LOG.debug("Request to partially update Eventi : {}", eventiDTO);

        return eventiRepository
            .findById(eventiDTO.getId())
            .map(existingEvent -> {
                eventiMapper.partialUpdate(existingEvent, eventiDTO);
                applyPrivateEventRulesEntity(existingEvent);
                return existingEvent;
            })
            .map(eventiRepository::save)
            .map(eventiMapper::toDto)
            .map(dto -> {
                applyPrivateEventRulesDTO(dto);
                return dto;
            });
    }

    /**
     * Get all the eventis.
     */
    @Transactional(readOnly = true)
    public Page<EventiDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Eventis");
        return eventiRepository
            .findAll(pageable)
            .map(eventi -> {
                applyPrivateEventRulesEntity(eventi);
                EventiDTO dto = eventiMapper.toDto(eventi);
                applyPrivateEventRulesDTO(dto);
                return dto;
            });
    }

    /**
     * Get one eventi by id.
     */
    @Transactional(readOnly = true)
    public Optional<EventiDTO> findOne(UUID id) {
        LOG.debug("Request to get Eventi : {}", id);
        return eventiRepository
            .findById(id)
            .map(eventi -> {
                applyPrivateEventRulesEntity(eventi);
                EventiDTO dto = eventiMapper.toDto(eventi);
                applyPrivateEventRulesDTO(dto);
                return dto;
            });
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

    /**
     *
     * Metodo per creare Eventi publici
     *
     */
    @Transactional
    public EventiDTO creaEventoPubblico(EventiDTO eventiDTO) {
        LOG.debug("Request to create Eventi : {}", eventiDTO);
        Prenotazioni pre = prenotazioniRepository
            .findById(eventiDTO.getPrenotazioneId())
            .orElseThrow(() -> new BadRequestAlertException("Prenotazione non trvata", "eventi", "prenotazioneNotFound"));

        if (!pre.getStato().getCodice().equals(StatoCodice.CONFIRMED)) {
            throw new BadRequestAlertException("Prenotazione non confermata", "eventi", "prenotazioneNotFound");
        }

        Eventi eventi = new Eventi();
        eventi.setTitolo(eventiDTO.getTitolo());
        eventi.setTipo(TipoEvento.PUBBLICO);
        eventi.setPrezzo(eventiDTO.getPrezzo());
        eventi.setDescrizione(eventiDTO.getDescrizione());
        eventi.setPrenotazione(pre);

        eventi = eventiRepository.save(eventi);

        return eventiMapper.toDto(eventi);
    }

    //campo prezzo di entita e dto evento inpostato a zero
    private void applyPrivateEventRulesDTO(EventiDTO dto) {
        if (TipoEvento.PRIVATO.equals(dto.getTipo())) {
            dto.setPrezzo(BigDecimal.ZERO);
        }
    }

    private void applyPrivateEventRulesEntity(Eventi eventi) {
        if (eventi != null && eventi.getTipo() == TipoEvento.PRIVATO) {
            eventi.setPrezzo(BigDecimal.ZERO);
        }
    }
}
