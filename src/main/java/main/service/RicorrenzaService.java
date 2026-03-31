package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.Prenotazioni;
import main.domain.Ricorrenza;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.domain.enumeration.Frequenza;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.RicorrenzaRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import main.security.AuthoritiesConstants;
import main.service.dto.RicorrenzaDTO;
import main.web.rest.errors.UtenteNonAutenticatoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RicorrenzaService {

    private static final Logger LOG = LoggerFactory.getLogger(RicorrenzaService.class);

    /** Limite di sicurezza: max occorrenze generabili per serie */
    private static final int MAX_OCCORRENZE = 365;

    private final RicorrenzaRepository ricorrenzaRepository;
    private final PrenotazioniRepository prenotazioniRepository;
    private final SaleRepository saleRepository;
    private final UtentiRepository utentiRepository;
    private final StatiPrenotazioneRepository statiRepository;

    public RicorrenzaService(
        RicorrenzaRepository ricorrenzaRepository,
        PrenotazioniRepository prenotazioniRepository,
        SaleRepository saleRepository,
        UtentiRepository utentiRepository,
        StatiPrenotazioneRepository statiRepository
    ) {
        this.ricorrenzaRepository = ricorrenzaRepository;
        this.prenotazioniRepository = prenotazioniRepository;
        this.saleRepository = saleRepository;
        this.utentiRepository = utentiRepository;
        this.statiRepository = statiRepository;
    }

    // ── CREAZIONE ─────────────────────────────────────────────────────────────

    /**
     * Crea una nuova regola di ricorrenza e genera tutte le occorrenze (strategia EAGER).
     *
     * Flusso:
     *  1. Valida input
     *  2. Calcola tutte le date candidate
     *  3. Per ogni data, verifica conflitti con prenotazioni CONFIRMED esistenti
     *  4. Crea la prenotazione in stato WAITING se non c'è conflitto, la salta altrimenti
     *  5. Ritorna il DTO con conteggio create/skippate e date conflitto
     */
    public RicorrenzaDTO creaRicorrenza(RicorrenzaDTO dto) {
        LOG.debug("Request to create Ricorrenza: frequenza={}, sala={}", dto.getFrequenza(), dto.getSalaId());

        validaInput(dto);

        Sale sala = saleRepository
            .findByIdWithLock(dto.getSalaId())
            .orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + dto.getSalaId()));

        Utenti utente = caricaUtenteAutenticato();

        StatiPrenotazione statoWaiting = statiRepository
            .findByCodice(StatoCodice.WAITING)
            .orElseThrow(() -> new EntityNotFoundException("Stato WAITING non trovato"));

        StatiPrenotazione statoConfirmed = statiRepository
            .findByCodice(StatoCodice.CONFIRMED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CONFIRMED non trovato"));

        // Salva la regola
        Ricorrenza ricorrenza = new Ricorrenza();
        ricorrenza.setFrequenza(dto.getFrequenza());
        ricorrenza.setDataInizio(dto.getDataInizio());
        ricorrenza.setDataFine(dto.getDataFine());
        ricorrenza.setNumOccorrenze(dto.getNumOccorrenze());
        ricorrenza.setOraInizio(dto.getOraInizio());
        ricorrenza.setOraFine(dto.getOraFine());
        ricorrenza.setNumPersone(dto.getNumPersone());
        ricorrenza.setSala(sala);
        ricorrenza.setUtente(utente);
        if (dto.getGiorniSettimana() != null && !dto.getGiorniSettimana().isEmpty()) {
            ricorrenza.setGiorniSettimana(String.join(",", dto.getGiorniSettimana()));
        }
        ricorrenza = ricorrenzaRepository.save(ricorrenza);

        // Calcola le date candidate
        List<LocalDate> dateCandidate = calcolaDate(dto);
        LOG.debug("Date candidate calcolate: {}", dateCandidate.size());

        // Genera le prenotazioni
        int create = 0;
        List<LocalDate> dateConflitto = new ArrayList<>();
        boolean primaIstanza = true;

        for (LocalDate data : dateCandidate) {
            boolean conflitto = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
                sala,
                data,
                dto.getOraInizio(),
                dto.getOraFine()
            );

            if (conflitto) {
                dateConflitto.add(data);
                LOG.debug("Conflitto trovato per data {}, skip", data);
                // La prima data è quella della prenotazione singola già creata — non saltarla
                primaIstanza = false;
                continue;
            }

            Prenotazioni p = new Prenotazioni();
            p.setData(data);
            p.setOraInizio(dto.getOraInizio());
            p.setOraFine(dto.getOraFine());
            p.setSala(sala);
            p.setUtente(utente);
            p.setNumPersone(dto.getNumPersone());
            // Prima occorrenza (= il giorno già prenotato e confermato) → CONFIRMED
            // Tutte le successive → WAITING, in attesa di conferma
            p.setStato(primaIstanza ? statoConfirmed : statoWaiting);
            p.setRicorrenza(ricorrenza);
            prenotazioniRepository.save(p);
            create++;
            primaIstanza = false;
        }

        LOG.debug("Ricorrenza {}: create={}, conflitti={}", ricorrenza.getId(), create, dateConflitto.size());

        // Componi risposta
        dto.setId(ricorrenza.getId());
        dto.setSalaNome(sala.getNome());
        dto.setIstanzeCreate(create);
        dto.setIstanzeConflitto(dateConflitto.size());
        dto.setDateConflitto(dateConflitto.stream().limit(10).collect(Collectors.toList()));
        return dto;
    }

    // ── CANCELLAZIONE ─────────────────────────────────────────────────────────

    /**
     * Cancella tutte le prenotazioni future della serie (da oggi incluso).
     * La regola di ricorrenza rimane per storico.
     */
    public void cancellaSerieDaOggi(UUID ricorrenzaId) {
        Ricorrenza r = caricaRicorrenzaConPermessi(ricorrenzaId);
        LocalDate oggi = LocalDate.now();

        List<Prenotazioni> future = prenotazioniRepository.findByRicorrenzaIdAndDataGreaterThanEqual(ricorrenzaId, oggi);
        StatiPrenotazione cancelled = statiRepository
            .findByCodice(StatoCodice.CANCELLED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CANCELLED non trovato"));

        future.forEach(p -> {
            p.setStato(cancelled);
            prenotazioniRepository.save(p);
        });

        LOG.debug("Cancellate {} prenotazioni future della serie {}", future.size(), ricorrenzaId);
    }

    /**
     * Cancella TUTTA la serie (tutte le istanze, passate e future).
     */
    public void cancellaTuttaSerie(UUID ricorrenzaId) {
        Ricorrenza r = caricaRicorrenzaConPermessi(ricorrenzaId);
        StatiPrenotazione cancelled = statiRepository
            .findByCodice(StatoCodice.CANCELLED)
            .orElseThrow(() -> new EntityNotFoundException("Stato CANCELLED non trovato"));

        List<Prenotazioni> tutte = prenotazioniRepository.findByRicorrenzaId(ricorrenzaId);
        tutte.forEach(p -> {
            p.setStato(cancelled);
            prenotazioniRepository.save(p);
        });

        LOG.debug("Cancellate tutte {} le istanze della serie {}", tutte.size(), ricorrenzaId);
    }

    // ── LETTURA ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RicorrenzaDTO> getMieRicorrenze() {
        String login = getAuthenticatedUsername();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.ADMIN.equals(a.getAuthority()));

        List<Ricorrenza> lista = isAdmin ? ricorrenzaRepository.findAllWithDetails() : ricorrenzaRepository.findByUtente_User_Login(login);

        return lista.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── CALCOLO DATE ─────────────────────────────────────────────────────────

    /**
     * Calcola tutte le date candidate per la serie in base a frequenza e configurazione.
     *
     * Algoritmo:
     *  - WEEKLY / BIWEEKLY: iterazione giorno per giorno con step settimana/bisettimanale,
     *    filtrando sui giorni della settimana selezionati.
     *  - MONTHLY: aggiunge esattamente 1 mese alla data di inizio ad ogni iterazione.
     *
     * Edge case gestiti:
     *  - Fine mese: dayjs/LocalDate gestisce automaticamente (es: 31 gen + 1 mese = 28/29 feb)
     *  - Limite numOccorrenze: si ferma al raggiungimento del contatore
     *  - Limite dataFine: si ferma quando la data supera dataFine
     *  - Limite di sicurezza MAX_OCCORRENZE: evita loop infiniti
     */
    List<LocalDate> calcolaDate(RicorrenzaDTO dto) {
        List<LocalDate> date = new ArrayList<>();

        LocalDate cursore = dto.getDataInizio();
        LocalDate fine = dto.getDataFine();
        int maxOcc = dto.getNumOccorrenze() != null ? dto.getNumOccorrenze() : MAX_OCCORRENZE;
        int contatore = 0;

        List<DayOfWeek> giorniAttivi = parseGiorni(dto.getGiorniSettimana());

        switch (dto.getFrequenza()) {
            case WEEKLY -> {
                // Itera settimana per settimana sui giorni selezionati
                // Trova il lunedì della settimana di inizio
                LocalDate inizioSettimana = cursore.with(DayOfWeek.MONDAY);
                if (inizioSettimana.isAfter(cursore)) {
                    inizioSettimana = inizioSettimana.minusWeeks(1);
                }

                LocalDate settimana = inizioSettimana;
                while (contatore < maxOcc && (fine == null || settimana.isBefore(fine) || settimana.isEqual(fine))) {
                    for (DayOfWeek giorno : giorniAttivi) {
                        LocalDate data = settimana.with(giorno);
                        if (!data.isBefore(dto.getDataInizio()) && (fine == null || !data.isAfter(fine))) {
                            date.add(data);
                            contatore++;
                            if (contatore >= maxOcc) break;
                        }
                    }
                    settimana = settimana.plusWeeks(1);
                    if (date.size() >= MAX_OCCORRENZE) break;
                }
            }
            case BIWEEKLY -> {
                LocalDate inizioSettimana = cursore.with(DayOfWeek.MONDAY);
                if (inizioSettimana.isAfter(cursore)) {
                    inizioSettimana = inizioSettimana.minusWeeks(1);
                }

                LocalDate settimana = inizioSettimana;
                while (contatore < maxOcc && (fine == null || settimana.isBefore(fine) || settimana.isEqual(fine))) {
                    for (DayOfWeek giorno : giorniAttivi) {
                        LocalDate data = settimana.with(giorno);
                        if (!data.isBefore(dto.getDataInizio()) && (fine == null || !data.isAfter(fine))) {
                            date.add(data);
                            contatore++;
                            if (contatore >= maxOcc) break;
                        }
                    }
                    settimana = settimana.plusWeeks(2); // ogni 2 settimane
                    if (date.size() >= MAX_OCCORRENZE) break;
                }
            }
            case MONTHLY -> {
                // Stesso giorno del mese, mese successivo
                // LocalDate gestisce automaticamente i fine mese (es: 31 gen → 28 feb)
                LocalDate data = dto.getDataInizio();
                while (contatore < maxOcc && (fine == null || !data.isAfter(fine))) {
                    date.add(data);
                    contatore++;
                    data = data.plusMonths(1);
                    if (date.size() >= MAX_OCCORRENZE) break;
                }
            }
        }

        return date;
    }

    // ── Helpers privati ───────────────────────────────────────────────────────

    private List<DayOfWeek> parseGiorni(List<String> giorni) {
        if (giorni == null || giorni.isEmpty()) {
            // Default: stesso giorno della settimana di dataInizio
            return List.of();
        }
        return giorni.stream().map(g -> DayOfWeek.valueOf(g.toUpperCase())).sorted().collect(Collectors.toList());
    }

    private void validaInput(RicorrenzaDTO dto) {
        if (dto.getSalaId() == null) throw new IllegalArgumentException("Sala obbligatoria");
        if (dto.getFrequenza() == null) throw new IllegalArgumentException("Frequenza obbligatoria");
        if (dto.getDataInizio() == null) throw new IllegalArgumentException("Data inizio obbligatoria");
        if (dto.getDataFine() == null && dto.getNumOccorrenze() == null) throw new IllegalArgumentException(
            "Specificare dataFine oppure numOccorrenze"
        );
        if (dto.getDataFine() != null && dto.getDataFine().isBefore(dto.getDataInizio())) throw new IllegalArgumentException(
            "dataFine deve essere successiva a dataInizio"
        );
        if (dto.getOraInizio() == null || dto.getOraFine() == null) throw new IllegalArgumentException("Orari obbligatori");
        if (!dto.getOraInizio().isBefore(dto.getOraFine())) throw new IllegalArgumentException("oraInizio deve essere prima di oraFine");
        if (dto.getDataInizio().isBefore(LocalDate.now())) throw new IllegalArgumentException("dataInizio non può essere nel passato");
        if (
            dto.getFrequenza() != Frequenza.MONTHLY && (dto.getGiorniSettimana() == null || dto.getGiorniSettimana().isEmpty())
        ) throw new IllegalArgumentException("Selezionare almeno un giorno della settimana");
    }

    private Ricorrenza caricaRicorrenzaConPermessi(UUID id) {
        Ricorrenza r = ricorrenzaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ricorrenza non trovata: " + id));

        String username = getAuthenticatedUsername();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.ADMIN.equals(a.getAuthority()));

        if (!isAdmin) {
            boolean isOwner =
                r.getUtente() != null && r.getUtente().getUser() != null && username.equals(r.getUtente().getUser().getLogin());
            if (!isOwner) throw new AccessDeniedException("Non autorizzato su questa serie");
        }
        return r;
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new UtenteNonAutenticatoException("Utente non autenticato");
        return auth.getName();
    }

    private Utenti caricaUtenteAutenticato() {
        String username = getAuthenticatedUsername();
        return utentiRepository
            .findByUser_Login(username)
            .orElseThrow(() -> new EntityNotFoundException("Profilo utente non trovato per: " + username));
    }

    private RicorrenzaDTO toDto(Ricorrenza r) {
        RicorrenzaDTO dto = new RicorrenzaDTO();
        dto.setId(r.getId());
        dto.setFrequenza(r.getFrequenza());
        dto.setDataInizio(r.getDataInizio());
        dto.setDataFine(r.getDataFine());
        dto.setNumOccorrenze(r.getNumOccorrenze());
        dto.setOraInizio(r.getOraInizio());
        dto.setOraFine(r.getOraFine());
        dto.setNumPersone(r.getNumPersone());
        if (r.getSala() != null) {
            dto.setSalaId(r.getSala().getId());
            dto.setSalaNome(r.getSala().getNome());
        }
        if (r.getGiorniSettimana() != null && !r.getGiorniSettimana().isBlank()) {
            dto.setGiorniSettimana(Arrays.asList(r.getGiorniSettimana().split(",")));
        }
        return dto;
    }
}
