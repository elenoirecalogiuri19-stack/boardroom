package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import main.security.AuthoritiesConstants;
import main.service.QrCodeGenerator;
import main.service.dto.PrenotazioniDTO;
import main.service.dto.PrenotazioniEmailDTO;
import main.service.mapper.PrenotazioniMapper;
import main.web.rest.errors.UtenteNonAutenticatoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrenotazioniService {

    private static final Logger LOG = LoggerFactory.getLogger(PrenotazioniService.class);

    private final PrenotazioniRepository prenotazioniRepository;

    private final StatiPrenotazioneRepository statiPrenotazioneRepository;

    private final UtentiRepository utentiRepository;

    private final SaleRepository saleRepository;

    private final PrenotazioniMapper prenotazioniMapper;

    private final QrCodeGenerator qrCodeGenerator;

    private final MailService mailService;
    private final WaitlistService waitlistService;

    public PrenotazioniService(
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        UtentiRepository utentiRepository,
        SaleRepository saleRepository,
        PrenotazioniMapper prenotazioniMapper,
        QrCodeGenerator qrCodeGenerator,
        MailService mailService,
        WaitlistService waitlistService
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.utentiRepository = utentiRepository;
        this.saleRepository = saleRepository;
        this.prenotazioniMapper = prenotazioniMapper;
        this.qrCodeGenerator = qrCodeGenerator;
        this.mailService = mailService;
        this.waitlistService = waitlistService;
    }

    public PrenotazioniDTO save(PrenotazioniDTO dto) {
        LOG.debug("Request to save Prenotazioni (admin) : {}", dto);

        Prenotazioni entity = prenotazioniMapper.toEntity(dto);

        // Carica la sala con lock per evitare race condition concorrenti
        if (entity.getSala() != null && entity.getSala().getId() != null) {
            UUID salaId = entity.getSala().getId();
            Sale sala = saleRepository
                .findByIdWithLock(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + salaId));
            entity.setSala(sala);
        }

        validaPrenotazione(entity);

        applyDefaultConfirmedState(entity);

        if (entity.getSala() != null && entity.getData() != null && entity.getOraInizio() != null && entity.getOraFine() != null) {
            boolean conflitto = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
                entity.getSala(),
                entity.getData(),
                entity.getOraInizio(),
                entity.getOraFine()
            );
            if (conflitto) {
                throw new IllegalStateException(
                    "Impossibile creare la prenotazione: esiste già una prenotazione confermata per la sala '" +
                    entity.getSala().getNome() +
                    "' in questo orario."
                );
            }
        }

        if (entity.getCodiceQr() == null || entity.getCodiceQr().isBlank()) {
            String codice = "SALA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            entity.setCodiceQr(codice);
            LOG.debug("Codice QR generato per prenotazione admin: {}", codice);
        }

        entity = prenotazioniRepository.save(entity);
        return prenotazioniMapper.toDto(entity);
    }

    public PrenotazioniDTO update(PrenotazioniDTO dto) {
        LOG.debug("Request to update Prenotazioni : {}", dto);

        prenotazioniRepository
            .findById(dto.getId())
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata: " + dto.getId()));

        Prenotazioni entity = prenotazioniMapper.toEntity(dto);

        if (entity.getSala() != null && entity.getSala().getId() != null) {
            UUID salaId = entity.getSala().getId();
            Sale sala = saleRepository
                .findByIdWithLock(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + salaId));
            entity.setSala(sala);
        }

        validaPrenotazione(entity);

        if (entity.getSala() != null && entity.getData() != null && entity.getOraInizio() != null && entity.getOraFine() != null) {
            boolean conflitto = prenotazioniRepository.existsOverlappingConfirmedExcluding(
                entity.getSala(),
                entity.getData(),
                entity.getOraInizio(),
                entity.getOraFine(),
                dto.getId()
            );
            if (conflitto) {
                throw new IllegalStateException(
                    "Impossibile aggiornare la prenotazione: esiste già una prenotazione confermata per la sala '" +
                    entity.getSala().getNome() +
                    "' in questo orario."
                );
            }
        }

        entity = prenotazioniRepository.save(entity);
        return prenotazioniMapper.toDto(entity);
    }

    public Optional<PrenotazioniDTO> partialUpdate(PrenotazioniDTO dto) {
        LOG.debug("Request to partially update Prenotazioni : {}", dto);

        return prenotazioniRepository
            .findById(dto.getId())
            .map(existing -> {
                prenotazioniMapper.partialUpdate(existing, dto);
                return existing;
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

    @Transactional(readOnly = true)
    public Optional<PrenotazioniDTO> findByCodiceQr(String codice) {
        LOG.debug("Request to find Prenotazioni by QR code : {}", codice);
        return prenotazioniRepository.findByCodiceQr(codice).map(prenotazioniMapper::toDto);
    }

    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void deleteAsAdmin(UUID id) {
        LOG.debug("Request to hard-delete Prenotazioni (admin) : {}", id);
        if (!prenotazioniRepository.existsById(id)) {
            throw new EntityNotFoundException("Prenotazione non trovata: " + id);
        }
        prenotazioniRepository.deleteById(id);
        LOG.debug("Prenotazione {} eliminata definitivamente dall'amministratore", id);
    }

    public void deletePrenotazione(UUID id) {
        LOG.debug("Request to cancel Prenotazioni : {}", id);

        String username = getAuthenticatedUsername();

        Prenotazioni pren = prenotazioniRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata"));
        verificaPermessiCancellazione(pren, username);

        boolean wasConfirmed = pren.getStato() != null && pren.getStato().getCodice() == StatoCodice.CONFIRMED;

        StatiPrenotazione statoCancelled = statiPrenotazioneRepository
            .findByCodice(StatoCodice.CANCELLED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CANCELLED non trovato"));

        pren.setStato(statoCancelled);
        prenotazioniRepository.save(pren);

        if (wasConfirmed) {
            waitlistService.promuoviDaWaitlist(pren);
        }

        LOG.debug("Prenotazione {} annullata con successo dall'utente {}", id, username);
    }

    public PrenotazioniDTO confermaPrenotazione(UUID prenotazioneId) {
        LOG.debug("Request to create Prenotazioni : {}", prenotazioneId);

        Prenotazioni pren = prenotazioniRepository
            .findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovato"));

        String username = getAuthenticatedUsername();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.ADMIN.equals(a.getAuthority()));
        if (!isAdmin) {
            boolean isOwner =
                pren.getUtente() != null && pren.getUtente().getUser() != null && username.equals(pren.getUtente().getUser().getLogin());
            if (!isOwner) {
                throw new AccessDeniedException("Non autorizzato a confermare questa prenotazione");
            }
        }

        verificaStatoWaiting(pren);
        saleRepository.findByIdWithLock(pren.getSala().getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));
        verificaAssenzaConflitti(pren);

        StatiPrenotazione statoConfirmed = statiPrenotazioneRepository
            .findByCodice(StatoCodice.CONFIRMED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CONFIRMED non trovato"));
        pren.setStato(statoConfirmed);

        if (pren.getCodiceQr() == null || pren.getCodiceQr().isBlank()) {
            String codice = "SALA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            pren.setCodiceQr(codice);
            LOG.debug("Codice QR generato per prenotazione {}: {}", prenotazioneId, codice);
        }

        pren = prenotazioniRepository.save(pren);

        try {
            String qrBase64 = qrCodeGenerator.generateQRCodeBase64(pren.getCodiceQr());

            PrenotazioniEmailDTO emailDto = new PrenotazioniEmailDTO();
            if (pren.getUtente() != null) {
                emailDto.setNome(pren.getUtente().getNome() != null ? pren.getUtente().getNome() : "");
                emailDto.setCognome("");
                if (pren.getUtente().getUser() != null) {
                    emailDto.setEmail(pren.getUtente().getUser().getEmail());
                }
            }

            mailService.sendConfermaPrenotazione(pren, emailDto, pren.getCodiceQr(), qrBase64);
            LOG.debug("Email conferma accodata per prenotazione {}", pren.getId());
        } catch (Exception e) {
            LOG.warn("Errore invio email conferma per prenotazione {}: {}", pren.getId(), e.getMessage());
        }

        return prenotazioniMapper.toDto(pren);
    }

    @Transactional(readOnly = true)
    public List<PrenotazioniDTO> getStoricoPrenotazioni() {
        LocalDate oggi = LocalDate.now();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.ADMIN.equals(a.getAuthority()));

        if (isAdmin) {
            return prenotazioniRepository.findStorico(oggi).stream().map(prenotazioniMapper::toDto).toList();
        }

        String username = getAuthenticatedUsername();
        return prenotazioniRepository.findStoricoByLogin(username, oggi).stream().map(prenotazioniMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PrenotazioniDTO> getPrenotazioniOdierne() {
        LocalDate oggi = LocalDate.now();
        String username = getAuthenticatedUsername();
        return prenotazioniRepository
            .findByUtente_User_LoginAndDataGreaterThanEqualOrderByDataAscOraInizioAsc(username, oggi)
            .stream()
            .map(prenotazioniMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PrenotazioniDTO> findByDataBetween(LocalDate dataInizio, LocalDate dataFine) {
        LOG.debug("Request to get prenotazioni calendario da {} a {}", dataInizio, dataFine);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.ADMIN.equals(a.getAuthority()));

        List<Prenotazioni> risultati = prenotazioniRepository.findByDataBetween(dataInizio, dataFine);

        if (!isAdmin) {
            String username = getAuthenticatedUsername();
            risultati = risultati
                .stream()
                .filter(
                    p -> p.getUtente() != null && p.getUtente().getUser() != null && username.equals(p.getUtente().getUser().getLogin())
                )
                .toList();
        }

        return risultati.stream().map(prenotazioniMapper::toDto).toList();
    }

    public PrenotazioniDTO nuovoPrenotazioni(PrenotazioniDTO dto) {
        LOG.debug("Request to nuovo Prenotazioni : {}", dto);

        validaInputRicerca(dto);
        LOG.debug(
            "Input ricerca valido - salaId={}, data={}, oraInizio={}, oraFine={}",
            dto.getSalaId(),
            dto.getData(),
            dto.getOraInizio(),
            dto.getOraFine()
        );

        Sale sala = caricaSala(dto.getSalaId());
        LOG.debug("Sala caricata: {}", sala.getId());

        Utenti utente = caricaUtenteAutenticato();
        LOG.debug("Utente autenticato: {}", utente.getId());

        Prenotazioni pren = costruisciPrenotazioneDaRicerca(dto, sala, utente);

        validaPrenotazione(pren);

        impostaStatoIniziale(pren);

        gestisciSovrapposizioni(pren);

        Prenotazioni salvata = prenotazioniRepository.save(pren);
        LOG.debug("Prenotazione salvata: {}", salvata.getId());

        return prenotazioniMapper.toDto(salvata);
    }

    private void validaPrenotazione(Prenotazioni prenotazioni) {
        if (!prenotazioni.getOraInizio().isBefore(prenotazioni.getOraFine())) {
            throw new IllegalArgumentException(
                "L'ora di inizio deve essere strettamente inferiore all'ora di fine (durata minima: 1 minuto)"
            );
        }
        if (prenotazioni.getData().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La data della prenotazione non deve essere nel passato");
        }
        if (prenotazioni.getNumPersone() != null && prenotazioni.getSala() != null) {
            if (prenotazioni.getNumPersone() <= 0) {
                throw new IllegalArgumentException("Il numero di persone deve essere almeno 1.");
            }
            int capienza = prenotazioni.getSala().getCapienza();
            if (prenotazioni.getNumPersone() > capienza) {
                throw new IllegalArgumentException(
                    "Il numero di persone (" +
                    prenotazioni.getNumPersone() +
                    ") supera la capienza della sala '" +
                    prenotazioni.getSala().getNome() +
                    "' (" +
                    capienza +
                    " posti)."
                );
            }
        }
    }

    private void applyDefaultConfirmedState(Prenotazioni prenotazioni) {
        statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).ifPresent(prenotazioni::setStato);
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UtenteNonAutenticatoException("Utente non autenticato");
        }
        return auth.getName();
    }

    private void verificaPermessiCancellazione(Prenotazioni pren, String username) {
        boolean isOwner =
            pren.getUtente() != null && pren.getUtente().getUser() != null && username.equals(pren.getUtente().getUser().getLogin());
        if (!isOwner) {
            throw new AccessDeniedException("Utente non ha i permessi per cancellare questa prenotazione");
        }
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
            // Slot occupato → entra in waitlist invece di essere rifiutato
            waitlistService.aggiungiAWaitlist(pren);
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

    private void validaInputRicerca(PrenotazioniDTO dto) {
        if (dto.getSalaId() == null) {
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
        return saleRepository.findByIdWithLock(salaId).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));
    }

    private Utenti caricaUtenteAutenticato() {
        String username = getAuthenticatedUsername();
        return utentiRepository
            .findByUser_Login(username)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Profilo utente non trovato per l'account '" +
                    username +
                    "': " +
                    "completare la registrazione prima di effettuare una prenotazione"
                )
            );
    }

    private Prenotazioni costruisciPrenotazioneDaRicerca(PrenotazioniDTO dto, Sale sala, Utenti utente) {
        Prenotazioni pren = new Prenotazioni();
        pren.setSala(sala);
        pren.setUtente(utente);
        pren.setData(dto.getData());
        pren.setOraInizio(dto.getOraInizio());
        pren.setOraFine(dto.getOraFine());
        pren.setNumPersone(dto.getNumPersone());
        return pren;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void aggiornaPrenotazioniScadute() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(5);

        StatiPrenotazione rejected = statiPrenotazioneRepository
            .findByCodice(StatoCodice.REJECTED)
            .orElseThrow(() -> new EntityNotFoundException("Stato REJECTED non trovato"));

        int aggiornate = prenotazioniRepository.aggiornaScadute(rejected, limite);

        if (aggiornate > 0) {
            LOG.debug("Aggiornate {} prenotazioni da WAITING a REJECTED", aggiornate);
        }
    }
}
