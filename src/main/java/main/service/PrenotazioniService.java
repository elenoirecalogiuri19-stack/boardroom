package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.PrenotazioniDTO;
import main.service.mapper.PrenotazioniMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrenotazioniService {

    private static final Logger log = LoggerFactory.getLogger(PrenotazioniService.class);

    private final PrenotazioniRepository prenotazioniRepository;
    private final StatiPrenotazioneRepository statiPrenotazioneRepository;
    private final SaleRepository saleRepository;
    private final PrenotazioniMapper prenotazioniMapper;

    public PrenotazioniService(
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        SaleRepository saleRepository,
        PrenotazioniMapper prenotazioniMapper
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.saleRepository = saleRepository;
        this.prenotazioniMapper = prenotazioniMapper;
    }

    // --- SALVA GENERICO (POST/PUT) ---
    public PrenotazioniDTO save(PrenotazioniDTO dto) {
        Prenotazioni pren = prenotazioniMapper.toEntity(dto);
        pren = prenotazioniRepository.save(pren);
        return prenotazioniMapper.toDto(pren);
    }

    // --- PARTIAL UPDATE ---
    public Optional<PrenotazioniDTO> partialUpdate(PrenotazioniDTO dto) {
        return prenotazioniRepository
            .findById(dto.getId())
            .map(existing -> {
                prenotazioniMapper.partialUpdate(existing, dto);
                return existing;
            })
            .map(prenotazioniRepository::save)
            .map(prenotazioniMapper::toDto);
    }

    // --- CREAZIONE PRENOTAZIONE PRIVATA (US4) ---
    public PrenotazioniDTO creaPrenotazione(PrenotazioniDTO dto) {
        log.debug("Creazione prenotazione privata");

        Prenotazioni pren = prenotazioniMapper.toEntity(dto);

        // Controllo sala
        Sale sala = saleRepository.findById(dto.getSala().getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));
        pren.setSala(sala);

        // Controllo sovrapposizione
        boolean occupata = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
            sala,
            pren.getData(),
            pren.getOraInizio(),
            pren.getOraFine()
        );
        if (occupata) {
            throw new IllegalStateException("Sala già occupata in quel periodo");
        }

        // Imposta tipo evento e stato iniziale
        pren.setTipoEvento(TipoEvento.PRIVATO);

        StatiPrenotazione waiting = statiPrenotazioneRepository
            .findByCodice(StatoCodice.WAITING)
            .stream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Stato CONFIRMED non configurato"));
        pren.setStato(waiting);

        return prenotazioniMapper.toDto(prenotazioniRepository.save(pren));
    }

    // --- ELIMINA PRENOTAZIONE (DELETE) ---
    public void delete(UUID id) {
        Prenotazioni pren = prenotazioniRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata"));

        StatiPrenotazione cancelled = statiPrenotazioneRepository
            .findByCodice(StatoCodice.CANCELLED)
            .stream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Stato CANCELLED non configurato"));

        pren.setStato(cancelled);
        prenotazioniRepository.save(pren);
    }

    // --- CONFERMA PRENOTAZIONE (US6) ---
    public Optional<PrenotazioniDTO> confermaPrenotazione(UUID id) {
        return prenotazioniRepository
            .findById(id)
            .map(pren -> {
                StatiPrenotazione confirmed = statiPrenotazioneRepository
                    .findByCodice(StatoCodice.CONFIRMED)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Stato CONFIRMED non configurato"));
                pren.setStato(confirmed);
                return prenotazioniRepository.save(pren);
            })
            .map(prenotazioniMapper::toDto);
    }

    // --- TROVA UNA PRENOTAZIONE ---
    @Transactional(readOnly = true)
    public Optional<PrenotazioniDTO> findOne(UUID id) {
        return prenotazioniRepository.findById(id).map(prenotazioniMapper::toDto);
    }

    // --- TROVA TUTTE LE PRENOTAZIONI (US2) ---
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PrenotazioniDTO> getAll(
        org.springframework.data.domain.Pageable pageable,
        boolean eagerload,
        UUID salaId
    ) {
        if (salaId != null) {
            return prenotazioniRepository.findBySalaId(salaId, pageable).map(prenotazioniMapper::toDto);
        }
        return prenotazioniRepository.findAll(pageable).map(prenotazioniMapper::toDto);
    }
}
