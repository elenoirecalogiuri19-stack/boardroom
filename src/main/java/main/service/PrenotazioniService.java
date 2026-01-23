package main.service;

import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
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

    private final Logger LOG = LoggerFactory.getLogger(PrenotazioniService.class);
    private final PrenotazioniRepository prenotazioniRepository;
    private final PrenotazioniMapper prenotazioniMapper;
    private final StatiPrenotazioneRepository statiPrenotazioneRepository;
    private final SaleRepository saleRepository;

    public PrenotazioniService(
        PrenotazioniRepository prenotazioniRepository,
        PrenotazioniMapper prenotazioniMapper,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        SaleRepository saleRepository
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.prenotazioniMapper = prenotazioniMapper;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.saleRepository = saleRepository;
    }

    public PrenotazioniDTO save(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to save Prenotazioni : {}", prenotazioniDTO);

        // US4: Gestione prezzo per evento PRIVATO
        if ("PRIVATO".equalsIgnoreCase(prenotazioniDTO.getTipoEvento())) {
            prenotazioniDTO.setPrezzo(null);
        }

        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);

        // US4: Impostazione automatica stato CONFIRMED
        statiPrenotazioneRepository
            .findAll()
            .stream()
            .filter(s -> s.getCodice() == StatoCodice.CONFIRMED)
            .findFirst()
            .ifPresent(prenotazioni::setStato);

        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

    public PrenotazioniDTO update(PrenotazioniDTO prenotazioniDTO) {
        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);
        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

    public Optional<PrenotazioniDTO> partialUpdate(PrenotazioniDTO prenotazioniDTO) {
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
        return prenotazioniRepository.findAll(pageable).map(prenotazioniMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<PrenotazioniDTO> findOne(UUID id) {
        return prenotazioniRepository.findOneWithEagerRelationships(id).map(prenotazioniMapper::toDto);
    }

    public void delete(UUID id) {
        // US6: Soft Delete (Annullamento logico)
        LOG.debug("US6: Soft Delete per prenotazione : {}", id);
        prenotazioniRepository
            .findById(id)
            .ifPresent(prenotazione -> {
                statiPrenotazioneRepository
                    .findAll()
                    .stream()
                    .filter(s -> s.getCodice() == StatoCodice.CANCELLED)
                    .findFirst()
                    .ifPresent(stato -> {
                        prenotazione.setStato(stato);
                        prenotazioniRepository.save(prenotazione);
                    });
            });
    }
}
