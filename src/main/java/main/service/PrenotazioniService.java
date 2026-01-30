package main.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import main.service.dto.PrenotazioniDTO;
import main.service.mapper.PrenotazioniMapper;
import main.web.rest.errors.UtenteNonAutenticatoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final UtentiRepository utentiRepository;

    private final SaleRepository saleRepository;

    private final PrenotazioniMapper prenotazioniMapper;

    public PrenotazioniService(
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        UtentiRepository utentiRepository,
        SaleRepository saleRepository,
        PrenotazioniMapper prenotazioniMapper
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.utentiRepository = utentiRepository;
        this.saleRepository = saleRepository;
        this.prenotazioniMapper = prenotazioniMapper;
    }

    /**
     * Save a prenotazioni.
     *
     * @param dto the entity to save.
     * @return the persisted entity.
     */
    public PrenotazioniDTO save(PrenotazioniDTO dto) {
        LOG.debug("Request to save Prenotazioni : {}", dto);

        applyPrivateEventRules(dto);

        Prenotazioni entity = prenotazioniMapper.toEntity(dto);
        applyDefaultConfirmedState(entity);

        entity = prenotazioniRepository.save(entity);
        return prenotazioniMapper.toDto(entity);
    }

    /**
     * Update a prenotazioni.
     *
     * @param dto the entity to save.
     * @return the persisted entity.
     */
    public PrenotazioniDTO update(PrenotazioniDTO dto) {
        LOG.debug("Request to update Prenotazioni : {}", dto);
        applyPrivateEventRules(dto);
        Prenotazioni entity = prenotazioniMapper.toEntity(dto);
        entity = prenotazioniRepository.save(entity);
        return prenotazioniMapper.toDto(entity);
    }

    /**
     * Partially update a prenotazioni.
     *
     * @param dto the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PrenotazioniDTO> partialUpdate(PrenotazioniDTO dto) {
        LOG.debug("Request to partially update Prenotazioni : {}", dto);

        return prenotazioniRepository
            .findById(dto.getId())
            .map(existing -> {
                prenotazioniMapper.partialUpdate(existing, dto);
                applyPrivateEventRulesEntity(existing);
                return existing;
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

    @Transactional(readOnly = true)
    public Page<PrenotazioniDTO> getAll(Pageable pageable, boolean eagerload, UUID salaId) {
        if (salaId != null) {
            return prenotazioniRepository.findBySalaId(salaId, pageable).map(prenotazioniMapper::toDto);
        }
        if (eagerload) {
            return findAllWithEagerRelationships(pageable);
        }
        return findAll(pageable);
    }

    /**
     * Delete the prenotazioni by id.
     *
     * @param id the id of the entity.
     *
     * gestita permessi per eliminazione prenotazione
     *
     */
    public void delete(UUID id) throws AccessDeniedException {
        LOG.debug("Request to delete Prenotazioni : {}", id);

        String username = getAuthenticatedUsername();

        Prenotazioni pren = prenotazioniRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata"));
        verificaPermessiCancellazione(pren, username);

        StatiPrenotazione statoCancelled = statiPrenotazioneRepository
            .findByCodice(StatoCodice.CANCELLED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CANCELLED non trovato"));

        pren.setStato(statoCancelled);
        prenotazioniRepository.save(pren);

        LOG.debug("Prenotazione {} annulaa con sucesso");
    }

    /**
     *
     * Metodo nuova prenotazione che
     * ne verifica la disponibilita
     * e gestisce lo stato
     *
     */
    public PrenotazioniDTO creaPrenotazione(PrenotazioniDTO dto) {
        LOG.debug("Request to create Prenotazioni : {}", dto);

        Prenotazioni pren = prenotazioniMapper.toEntity(dto);

        validaRiferimenti(dto);
        collegaUtenteESala(pren);
        validaPrenotazione(pren);
        impostaStatoIniziale(pren);
        gestisciSovrapposizioni(pren);

        pren = prenotazioniRepository.save(pren);

        return prenotazioniMapper.toDto(pren);
    }

    /**
     *
     * Metodo per confermare la prenotazione
     *
     */

    public PrenotazioniDTO confermaPrenotazione(UUID prenotazioneId) {
        LOG.debug("Request to create Prenotazioni : {}", prenotazioneId);

        Prenotazioni pren = prenotazioniRepository
            .findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovato"));

        verificaStatoWaiting(pren);
        verificaAssenzaConflitti(pren);

        StatiPrenotazione statoConfirmed = statiPrenotazioneRepository
            .findByCodice(StatoCodice.CONFIRMED)
            .orElseThrow(() -> new EntityNotFoundException("Stato COFERMED non trovato"));
        pren.setStato(statoConfirmed);

        pren = prenotazioniRepository.save(pren);

        return prenotazioniMapper.toDto(pren);
    }

    @Transactional(readOnly = true)
    public List<PrenotazioniDTO> getStoricoPrenotazioni() {
        LocalDate oggi = LocalDate.now();
        return prenotazioniRepository.findStorico(oggi).stream().map(prenotazioniMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PrenotazioniDTO> getPrenotazioniOdierne() {
        LocalDate oggi = LocalDate.now();
        return prenotazioniRepository.findOggiEFutre(oggi).stream().map(prenotazioniMapper::toDto).toList();
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

    private void applyPrivateEventRules(PrenotazioniDTO dto) {
        if (TipoEvento.PRIVATO.equals(dto.getTipoEvento())) {
            dto.setPrezzo(null);
        }
    }

    private void applyDefaultConfirmedState(Prenotazioni prenotazioni) {
        statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).ifPresent(prenotazioni::setStato);
    }

    private void applyPrivateEventRulesEntity(Prenotazioni entity) {
        if ("PRIVATO".equals(entity.getTipoEvento())) {
            entity.setPrezzo(null);
        }
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UtenteNonAutenticatoException("Utente non autenticato");
        }
        return auth.getName();
    }

    private void verificaPermessiCancellazione(Prenotazioni pren, String username) throws AccessDeniedException {
        boolean isOwner =
            pren.getUtente() != null && pren.getUtente().getUser() != null && username.equals(pren.getUtente().getUser().getLogin());
        if (!isOwner) {
            throw new AccessDeniedException("Utente non ha i permessi per cancellare questa prenotazione");
        }
    }

    private void validaRiferimenti(PrenotazioniDTO dto) {
        if (dto.getUtente() == null || dto.getUtente().getId() == null) {
            throw new IllegalArgumentException("Utente non è valido: ID mancante");
        }
        if (dto.getSala() == null || dto.getSala().getId() == null) {
            throw new IllegalArgumentException("Sala non è valida: ID mancante");
        }
    }

    private void collegaUtenteESala(Prenotazioni pren) {
        UUID utenteId = pren.getUtente().getId();
        UUID salaId = pren.getSala().getId();

        Utenti ut = utentiRepository.findById(utenteId).orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));
        Sale sa = saleRepository.findById(salaId).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));
        pren.setUtente(ut);
        pren.setSala(sa);
    }

    private void impostaStatoIniziale(Prenotazioni pren) {
        StatiPrenotazione statoWaiting = statiPrenotazioneRepository
            .findByCodice(StatoCodice.WAITING)
            .orElseThrow(() -> new EntityNotFoundException("Stato WAITING non trovato"));
        pren.setStato(statoWaiting);
    }

    private void gestisciSovrapposizioni(Prenotazioni pren) {
        boolean sovrapposizione = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
            pren.getSala(),
            pren.getData(),
            pren.getOraInizio(),
            pren.getOraFine()
        );

        if (sovrapposizione) {
            StatiPrenotazione statoRejected = statiPrenotazioneRepository
                .findByCodice(StatoCodice.REJECTED)
                .orElseThrow(() -> new EntityNotFoundException("Stato REJECTED non trovato"));
            pren.setStato(statoRejected);
        }
    }

    private void verificaStatoWaiting(Prenotazioni pren) {
        if (pren.getStato().getCodice() != StatoCodice.WAITING) {
            throw new IllegalStateException("La prenotazione non è in stato WAITING");
        }
    }

    private void verificaAssenzaConflitti(Prenotazioni pren) {
        boolean sovrapposizione = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
            pren.getSala(),
            pren.getData(),
            pren.getOraInizio(),
            pren.getOraFine()
        );
        if (sovrapposizione) {
            throw new IllegalStateException("Non è possibile confermare la prenotazione: conflitto con prenotazione esistente");
        }
    }

    public PrenotazioniDTO nuovoPrenotazioni(PrenotazioniDTO dto) {
        LOG.debug("Request to nuovo Prenotazioni : {}", dto);

        validaInputRicerca(dto);

        Sale sala = caricaSala(dto.getSala().getId());
        Utenti utente = caricaUtenteAutenticato();

        Prenotazioni pren = costruisciPrenotazioneDaRicerca(dto, sala, utente);

        validaPrenotazione(pren);
        impostaStatoIniziale(pren);

        Prenotazioni salvata = prenotazioniRepository.save(pren);
        return prenotazioniMapper.toDto(salvata);
    }

    private void validaInputRicerca(PrenotazioniDTO dto) {
        if (dto.getSala() == null || dto.getSala().getId() == null) {
            throw new IllegalArgumentException("Sala non valida: ID mancante");
        }
        if (dto.getData() == null) {
            throw new IllegalArgumentException("La data è obbligatoria");
        }
        if (dto.getOraInizio() == null || dto.getOraFine() == null) {
            throw new IllegalArgumentException("Orario di inizio e fine sono obbligatori");
        }
    }

    private Sale caricaSala(UUID salaId) {
        return saleRepository.findById(salaId).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));
    }

    private Utenti caricaUtenteAutenticato() {
        String username = getAuthenticatedUsername();
        return utentiRepository.findByUser_Login(username).orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));
    }

    private Prenotazioni costruisciPrenotazioneDaRicerca(PrenotazioniDTO dto, Sale sala, Utenti utente) {
        Prenotazioni pren = new Prenotazioni();
        pren.setSala(sala);
        pren.setUtente(utente);
        pren.setData(dto.getData());
        pren.setOraInizio(dto.getOraInizio());
        pren.setOraFine(dto.getOraFine());
        return pren;
    }
}
