package main.service;

import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
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

@Service
@Transactional
public class PrenotazioniService {

    private static final Logger LOG = LoggerFactory.getLogger(PrenotazioniService.class);

    private final PrenotazioniRepository prenotazioniRepository;

    private final PrenotazioniMapper prenotazioniMapper;

    private final StatiPrenotazioneRepository statiPrenotazioneRepository;

    public PrenotazioniService(
        PrenotazioniRepository prenotazioniRepository,
        PrenotazioniMapper prenotazioniMapper,
        StatiPrenotazioneRepository statiPrenotazioneRepository
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.prenotazioniMapper = prenotazioniMapper;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
    }

    public PrenotazioniDTO save(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to save Prenotazioni : {}", prenotazioniDTO);
        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);
        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

    public PrenotazioniDTO update(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to update Prenotazioni : {}", prenotazioniDTO);
        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);
        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

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

    @Transactional(readOnly = true)
    public Page<PrenotazioniDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Prenotazionis");
        return prenotazioniRepository.findAll(pageable).map(prenotazioniMapper::toDto);
    }

    public Page<PrenotazioniDTO> findAllWithEagerRelationships(Pageable pageable) {
        return prenotazioniRepository.findAllWithEagerRelationships(pageable).map(prenotazioniMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<PrenotazioniDTO> findOne(UUID id) {
        LOG.debug("Request to get Prenotazioni : {}", id);
        return prenotazioniRepository.findOneWithEagerRelationships(id).map(prenotazioniMapper::toDto);
    }

    public void delete(UUID id) {
        LOG.debug("Request to cancel Prenotazioni : {}", id);
        prenotazioniRepository
            .findById(id)
            .ifPresent(prenotazione -> {
                statiPrenotazioneRepository
                    .findAll()
                    .stream()
                    .filter(s -> s.getCodice().equals(StatoCodice.CANCELLED))
                    .findFirst()
                    .ifPresent(statoAnnullato -> {
                        prenotazione.setStato(statoAnnullato);
                        prenotazioniRepository.save(prenotazione);
                        LOG.debug("Prenotazione {} marcata come CANCELLED", id);
                    });
            });
    }
}
