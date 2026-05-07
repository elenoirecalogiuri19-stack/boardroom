package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.PrenotazioniEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WaitlistService {

    private static final Logger LOG = LoggerFactory.getLogger(WaitlistService.class);

    private static final int MINUTI_CONFERMA = 15;

    private final PrenotazioniRepository prenotazioniRepository;
    private final StatiPrenotazioneRepository statiPrenotazioneRepository;
    private final SaleRepository saleRepository;
    private final MailService mailService;
    private final QrCodeGenerator qrCodeGenerator;

    public WaitlistService(
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        SaleRepository saleRepository,
        MailService mailService,
        QrCodeGenerator qrCodeGenerator
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.saleRepository = saleRepository;
        this.mailService = mailService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    public Prenotazioni aggiungiAWaitlist(Prenotazioni prenotazione) {
        Sale sala = prenotazione.getSala();
        if (sala == null) throw new IllegalArgumentException("Sala obbligatoria");

        boolean giaInCoda = prenotazioniRepository.existsWaitlistPerStessoSlot(
            sala,
            prenotazione.getData(),
            prenotazione.getOraInizio(),
            prenotazione.getOraFine(),
            prenotazione.getUtente()
        );
        if (giaInCoda) {
            throw new IllegalStateException("Sei già in lista di attesa per questo slot.");
        }

        int prossimaPos =
            prenotazioniRepository
                .maxPosizioneWaitlist(sala, prenotazione.getData(), prenotazione.getOraInizio(), prenotazione.getOraFine())
                .orElse(0) +
            1;

        StatiPrenotazione statoWaitlisted = getStato(StatoCodice.WAITLISTED);
        prenotazione.setStato(statoWaitlisted);
        prenotazione.setPosizioneWaitlist(prossimaPos);

        Prenotazioni salvata = prenotazioniRepository.save(prenotazione);

        inviaEmailWaitlist(salvata, prossimaPos);

        LOG.info(
            "Prenotazione {} aggiunta in waitlist pos {} per sala {} slot {}-{}",
            salvata.getId(),
            prossimaPos,
            sala.getNome(),
            prenotazione.getOraInizio(),
            prenotazione.getOraFine()
        );

        return salvata;
    }

    public void promuoviDaWaitlist(Prenotazioni prenotazioneCancellata) {
        Sale sala = prenotazioneCancellata.getSala();

        saleRepository.findByIdWithLock(sala.getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));

        List<Prenotazioni> waitlist = prenotazioniRepository.findWaitlistOrdinata(
            sala,
            prenotazioneCancellata.getData(),
            prenotazioneCancellata.getOraInizio(),
            prenotazioneCancellata.getOraFine()
        );

        if (waitlist.isEmpty()) {
            LOG.debug("Nessun utente in waitlist per slot liberato di sala {}", sala.getNome());
            return;
        }

        Prenotazioni candidato = waitlist.get(0);
        promuoviCandidato(candidato);

        for (int i = 1; i < waitlist.size(); i++) {
            Prenotazioni p = waitlist.get(i);
            p.setPosizioneWaitlist(i);
            prenotazioniRepository.save(p);
        }
    }

    private void promuoviCandidato(Prenotazioni candidato) {
        if (candidato.getCodiceQr() == null || candidato.getCodiceQr().isBlank()) {
            String codice = "SALA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            candidato.setCodiceQr(codice);
        }

        StatiPrenotazione statoPromoted = getStato(StatoCodice.PROMOTED);
        candidato.setStato(statoPromoted);
        candidato.setPosizioneWaitlist(null);
        candidato.setPromossaAt(LocalDateTime.now());

        prenotazioniRepository.save(candidato);

        inviaEmailPromozione(candidato);

        LOG.info(
            "Prenotazione {} promossa da waitlist a PROMOTED — scade alle {}",
            candidato.getId(),
            candidato.getPromossaAt().plusMinutes(MINUTI_CONFERMA)
        );
    }

    public Prenotazioni confermaPromozione(UUID prenotazioneId) {
        Prenotazioni pren = prenotazioniRepository
            .findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata: " + prenotazioneId));

        if (pren.getStato().getCodice() != StatoCodice.PROMOTED) {
            throw new IllegalStateException("La prenotazione non è in stato PROMOTED.");
        }

        LocalDateTime scadenza = pren.getPromossaAt().plusMinutes(MINUTI_CONFERMA);
        if (LocalDateTime.now().isAfter(scadenza)) {
            scadiEPromuoviSuccessivo(pren);
            throw new IllegalStateException("Il tempo per confermare è scaduto. Il posto è stato assegnato al prossimo in lista.");
        }

        saleRepository.findByIdWithLock(pren.getSala().getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));

        boolean conflitto = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
            pren.getSala(),
            pren.getData(),
            pren.getOraInizio(),
            pren.getOraFine()
        );
        if (conflitto) {
            scadiEPromuoviSuccessivo(pren);
            throw new IllegalStateException("Lo slot non è più disponibile.");
        }

        pren.setStato(getStato(StatoCodice.CONFIRMED));
        pren.setPromossaAt(null);
        prenotazioniRepository.save(pren);

        LOG.info("Prenotazione {} confermata dall'utente dopo promozione da waitlist", pren.getId());
        return pren;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void scadiPromozioniScadute() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTI_CONFERMA);

        List<Prenotazioni> scadute = prenotazioniRepository.findPromotedScadute(limite);

        for (Prenotazioni pren : scadute) {
            try {
                LOG.info("Promozione scaduta per prenotazione {} — passa al prossimo in waitlist", pren.getId());
                scadiEPromuoviSuccessivo(pren);
            } catch (Exception e) {
                LOG.error("Errore nella scadenza promozione {}: {}", pren.getId(), e.getMessage());
            }
        }
    }

    private void scadiEPromuoviSuccessivo(Prenotazioni pren) {
        pren.setStato(getStato(StatoCodice.EXPIRED));
        pren.setPromossaAt(null);
        prenotazioniRepository.save(pren);

        promuoviDaWaitlist(pren);
    }

    private void inviaEmailWaitlist(Prenotazioni pren, int posizione) {
        try {
            if (pren.getUtente() == null || pren.getUtente().getUser() == null) return;
            String email = pren.getUtente().getUser().getEmail();
            String nome = pren.getUtente().getNome() != null ? pren.getUtente().getNome() : "";

            mailService.sendWaitlistNotifica(
                email,
                nome,
                pren.getSala().getNome(),
                pren.getData().toString(),
                pren.getOraInizio().toString(),
                pren.getOraFine().toString(),
                posizione
            );
        } catch (Exception e) {
            LOG.warn("Errore invio email waitlist per prenotazione {}: {}", pren.getId(), e.getMessage());
        }
    }

    private void inviaEmailPromozione(Prenotazioni pren) {
        try {
            if (pren.getUtente() == null || pren.getUtente().getUser() == null) return;
            String email = pren.getUtente().getUser().getEmail();
            String nome = pren.getUtente().getNome() != null ? pren.getUtente().getNome() : "";

            mailService.sendWaitlistPromossa(
                email,
                nome,
                pren.getSala().getNome(),
                pren.getData().toString(),
                pren.getOraInizio().toString(),
                pren.getOraFine().toString(),
                pren.getId().toString(),
                MINUTI_CONFERMA
            );
        } catch (Exception e) {
            LOG.warn("Errore invio email promozione per prenotazione {}: {}", pren.getId(), e.getMessage());
        }
    }

    private StatiPrenotazione getStato(StatoCodice codice) {
        return statiPrenotazioneRepository
            .findByCodice(codice)
            .orElseThrow(() -> new EntityNotFoundException("Stato " + codice + " non trovato"));
    }
}
