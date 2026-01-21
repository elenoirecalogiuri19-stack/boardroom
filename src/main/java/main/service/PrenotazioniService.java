package main.service;

import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.repository.PrenotazioniRepository;
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

    private final PrenotazioniMapper prenotazioniMapper;

    public PrenotazioniService(PrenotazioniRepository prenotazioniRepository, PrenotazioniMapper prenotazioniMapper) {
        this.prenotazioniRepository = prenotazioniRepository;
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
}
