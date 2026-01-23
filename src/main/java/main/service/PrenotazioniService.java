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

/**
 * Service Implementation for managing {@link main.domain.Prenotazioni}.
 */
@Service
@Transactional
public class PrenotazioniService {

    private final Logger LOG = LoggerFactory.getLogger(PrenotazioniService.class);

    private final PrenotazioniRepository prenotazioniRepository;
<<<<<<< Updated upstream
=======
    private final StatiPrenotazioneRepository statiPrenotazioneRepository;
    private final UtentiRepository utentiRepository;
    private final SaleRepository saleRepository;
>>>>>>> Stashed changes
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

    /**
<<<<<<< Updated upstream
     * Save a prenotazioni.
     * US4: Imposta automaticamente lo stato CONFIRMED recuperandolo dal DB.
=======
     * Salva una prenotazione.
     * Logica US4: se tipoEvento = PRIVATO, prezzo viene impostato a null.
>>>>>>> Stashed changes
     */
    public PrenotazioniDTO save(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to save Prenotazioni : {}", prenotazioniDTO);

        if ("PRIVATO".equalsIgnoreCase(prenotazioniDTO.getTipoEvento())) {
            prenotazioniDTO.setPrezzo(null);
            LOG.debug("Evento PRIVATO rilevato: prezzo impostato a null come da specifica US4.");
        }

        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);

        // Cerchiamo l'entità Stato che corrisponde al codice CONFIRMED
        statiPrenotazioneRepository
            .findAll()
            .stream()
            .filter(s -> s.getCodice() == StatoCodice.CONFIRMED)
            .findFirst()
            .ifPresent(prenotazioni::setStato);

        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

<<<<<<< Updated upstream
    /**
     * Update a prenotazioni.
     */
=======
>>>>>>> Stashed changes
    public PrenotazioniDTO update(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to update Prenotazioni : {}", prenotazioniDTO);
        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);
        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

<<<<<<< Updated upstream
    /**
     * Partially update a prenotazioni.
     */
=======
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    /**
     * Get all the prenotazionis.
     */
=======
>>>>>>> Stashed changes
    @Transactional(readOnly = true)
    public Page<PrenotazioniDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Prenotazionis");
        return prenotazioniRepository.findAll(pageable).map(prenotazioniMapper::toDto);
    }

    /**
<<<<<<< Updated upstream
     * Get one prenotazioni by id.
     */
=======
     * Recupera le prenotazioni filtrate per Sala (US2).
     */
    @Transactional(readOnly = true)
    public Page<PrenotazioniDTO> findAllBySala(UUID salaId, Pageable pageable) {
        LOG.debug("Request to get all Prenotazionis by Sala : {}", salaId);
        return prenotazioniRepository.findBySalaId(salaId, pageable).map(prenotazioniMapper::toDto);
    }

    public Page<PrenotazioniDTO> findAllWithEagerRelationships(Pageable pageable) {
        return prenotazioniRepository.findAllWithEagerRelationships(pageable).map(prenotazioniMapper::toDto);
    }

>>>>>>> Stashed changes
    @Transactional(readOnly = true)
    public Optional<PrenotazioniDTO> findOne(UUID id) {
        LOG.debug("Request to get Prenotazioni : {}", id);
        return prenotazioniRepository.findOneWithEagerRelationships(id).map(prenotazioniMapper::toDto);
    }

    /**
<<<<<<< Updated upstream
     * Delete the prenotazioni by id.
     * US6: Soft Delete - Cambia lo stato in CANCELLED invece di eliminare.
     */
    public void delete(UUID id) {
        LOG.debug("Request to cancel Prenotazioni : {}", id);
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
                        LOG.debug("Prenotazione {} annullata correttamente", id);
                    });
            });
=======
     * Delete richiesto dalla Resource.
     * Implementa la cancellazione logica per US6.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete (cancel) Prenotazioni : {}", id);
        try {
            // Cerchiamo di ottenere l'ID utente corrente per la cancellazione logica
            // In un contesto reale useresti il SecurityContextHolder
            String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
            this.cancellaPrenotazione(id, currentUserId);
        } catch (Exception e) {
            LOG.warn("Cancellazione logica fallita, procedo con eliminazione fisica per sicurezza: {}", e.getMessage());
            prenotazioniRepository.deleteById(id);
        }
    }

    /**
     * Cancellazione logica della prenotazione (US6)
     */
    public PrenotazioniDTO cancellaPrenotazione(UUID prenotazioneId, String utenteId) throws AccessDeniedException {
        LOG.debug("Request to cancel Prenotazione : {}", prenotazioneId);

        Prenotazioni pren = prenotazioniRepository.findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata"));

        // Se non è admin e non è il proprietario, nega l'accesso
        if (!tipoUtente() && (pren.getUtente() == null || !pren.getUtente().getId().toString().equals(utenteId))) {
            throw new AccessDeniedException("Utente non ha i permessi per cancellare questa prenotazione");
        }

        if (pren.getStato().getCodice() != StatoCodice.CONFIRMED &&
            pren.getStato().getCodice() != StatoCodice.WAITING) {
            throw new IllegalStateException("Solo prenotazioni CONFIRMED o WAITING possono essere cancellate");
        }

        StatiPrenotazione statoC = statiPrenotazioneRepository.findByCodice(StatoCodice.CANCELLED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CANCELLED non trovato"));

        pren.setStato(statoC);
        pren = prenotazioniRepository.save(pren);
        return prenotazioniMapper.toDto(pren);
    }

    // ... [Metodi creaPrenotazione, creaEventoPrivato, confermaPrenotazione e validaPrenotazione rimangono invariati] ...

    public PrenotazioniDTO creaPrenotazione(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to create Prenotazioni : {}", prenotazioniDTO);
        Prenotazioni prenotazioni = prenotazioniMapper.toEntity(prenotazioniDTO);
        Utenti ut = utentiRepository.findById(prenotazioniDTO.getUtenteId()).orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));
        Sale sa = saleRepository.findById(prenotazioniDTO.getSalaId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));
        prenotazioni.setUtente(ut);
        prenotazioni.setSala(sa);
        validaPrenotazione(prenotazioni);
        StatiPrenotazione statoW = statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING).orElseThrow(() -> new EntityNotFoundException("Stato WAITING non trovato"));
        prenotazioni.setStato(statoW);
        prenotazioni = prenotazioniRepository.save(prenotazioni);
        return prenotazioniMapper.toDto(prenotazioni);
    }

    public PrenotazioniDTO creaEventoPrivato(PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("Request to create PRIVATE event : {}", prenotazioniDTO);
        Prenotazioni pren = prenotazioniMapper.toEntity(prenotazioniDTO);
        Utenti ut = utentiRepository.findById(prenotazioniDTO.getUtenteId()).orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));
        Sale sa = saleRepository.findById(prenotazioniDTO.getSalaId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));
        pren.setUtente(ut);
        pren.setSala(sa);
        pren.setTipoEvento("PRIVATO");
        pren.setPrezzo(null);
        validaPrenotazione(pren);
        StatiPrenotazione statoC = statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow(() -> new EntityNotFoundException("Stato CONFIRMED non trovato"));
        pren.setStato(statoC);
        pren = prenotazioniRepository.save(pren);
        return prenotazioniMapper.toDto(pren);
    }

    public void validaPrenotazione(Prenotazioni prenotazioni) {
        if (prenotazioni.getOraInizio().isAfter(prenotazioni.getOraFine())) {
            throw new IllegalArgumentException("L'ora di inizio deve essere inferiore all'ora di fine");
        }
        if (prenotazioni.getData().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La data della prenotazione non deve essere nel passato");
        }
    }

    private boolean tipoUtente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthoritiesConstants.ADMIN));
>>>>>>> Stashed changes
    }
}
