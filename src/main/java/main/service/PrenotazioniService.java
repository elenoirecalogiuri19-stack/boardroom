package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.PrenotazioniDTO;
import main.service.mapper.PrenotazioniMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link main.domain.Prenotazioni}.
 */
@Service
@Transactional
public class PrenotazioniService {

    private static final Logger LOG = LoggerFactory.getLogger(PrenotazioniService.class);

    private final PrenotazioniRepository prenotazioniRepository;

    private final StatiPrenotazioneRepository statiPrenotazioneRepository;

    private final PrenotazioniMapper prenotazioniMapper;

    public PrenotazioniService(
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        PrenotazioniMapper prenotazioniMapper
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.prenotazioniMapper = prenotazioniMapper;
    }

    /**
     * Save a prenotazioni.
     *
     * @param prenotazioniDTO the entity to save.
     * @return the persisted entity.
     */
    public PrenotazioniDTO save(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to save Prenotazioni : {}", prenotazioniDTO);
        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);
        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

    /**
     * Update a prenotazioni.
     *
     * @param prenotazioniDTO the entity to save.
     * @return the persisted entity.
     */
    public PrenotazioniDTO update(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to update Prenotazioni : {}", prenotazioniDTO);
        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);
        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

    /**
     * Partially update a prenotazioni.
     *
     * @param prenotazioniDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PrenotazioniDTO> partialUpdate(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to partially update Prenotazioni : {}", prenotazioniDTO);

        return prenotazioniRepository
            .findById(prenotazioniDTO.getId())
            .map(existingPrenotazioni -> {
                prenotazioniMapper.partialUpdate(existingPrenotazioni, prenotazioniDTO);

                return existingPrenotazioni;
            })
            .map(prenotazioniRepository::save)
            .map(prenotazioniMapper::toDto);
    }

    /**
     * Get all the prenotazionis.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PrenotazioniDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Prenotazionis");
        return prenotazioniRepository.findAll(pageable).map(prenotazioniMapper::toDto);
    }

    /**
     * Get all the prenotazionis with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PrenotazioniDTO> findAllWithEagerRelationships(Pageable pageable) {
        return prenotazioniRepository.findAllWithEagerRelationships(pageable).map(prenotazioniMapper::toDto);
    }

    /**
     * Get one prenotazioni by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PrenotazioniDTO> findOne(UUID id) {
        LOG.debug("Request to get Prenotazioni : {}", id);
        return prenotazioniRepository.findOneWithEagerRelationships(id).map(prenotazioniMapper::toDto);
    }

    /**
     * Delete the prenotazioni by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Prenotazioni : {}", id);
        prenotazioniRepository.deleteById(id);
    }

    /**
     *
     * Metodo nuova prenotazione che
     * ne verifica la disponibilita
     * e gestisce lo stato
     *
     */
    public PrenotazioniDTO creaPrenotazione(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to create Prenotazioni : {}", prenotazioniDTO);

        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);

        validaPrenotazione(prenotazioni);

        StatiPrenotazione statoW = statiPrenotazioneRepository
            .findByCodice(StatoCodice.WAITING)
            .orElseThrow(() -> new EntityNotFoundException("Stato WAITING non trovato"));
        prenotazioni.setStato(statoW);

        boolean sovraposizione = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
            prenotazioni.getSala(),
            prenotazioni.getData(),
            prenotazioni.getOraInizio(),
            prenotazioni.getOraFine()
        );

        if (sovraposizione) {
            StatiPrenotazione statoR = statiPrenotazioneRepository
                .findByCodice(StatoCodice.REJECTED)
                .orElseThrow(() -> new EntityNotFoundException("Stato REJECTED non trovato"));
            prenotazioni.setStato(statoR);
        }

        prenotazioni = prenotazioniRepository.save(prenotazioni);

        return prenotazioniMapper.toDto(prenotazioni);
    }

    /**
     *
     * Metodo per confermare la prenotazione
     *
     */

    public PrenotazioniDTO confermaPrenotazione(UUID prenotazioneId) {
        LOG.debug("Request to create Prenotazioni : {}", prenotazioneId);

        Prenotazioni prenotazioni = prenotazioniRepository
            .findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovato"));

        if (prenotazioni.getStato().getCodice() != StatoCodice.WAITING) {
            throw new IllegalStateException("la Prenotazione non e in stato WAITING");
        }

        boolean sovrapposizione = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
            prenotazioni.getSala(),
            prenotazioni.getData(),
            prenotazioni.getOraInizio(),
            prenotazioni.getOraFine()
        );

        if (sovrapposizione) {
            throw new IllegalStateException("non e posibile confermare la prenotazione: conflito con prenotazione gia esistente");
        }

        StatiPrenotazione statoC = statiPrenotazioneRepository
            .findByCodice(StatoCodice.CANCELLED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CANCELLED non trovato"));
        prenotazioni.setStato(statoC);

        prenotazioni = prenotazioniRepository.save(prenotazioni);

        return prenotazioniMapper.toDto(prenotazioni);
    }

    /**
     * Metodo per la validazione dei dati
     *
     */

    public void validaPrenotazione(Prenotazioni prenotazioni) {
        if (prenotazioni.getOraInizio().isAfter(prenotazioni.getOraFine())) {
            throw new IllegalArgumentException("l'ora di inizie deve esere inferiore dello ora fine");
        }
        if (prenotazioni.getData().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("la data della prenotazione non deve essere nel pasato");
        }
    }
}
