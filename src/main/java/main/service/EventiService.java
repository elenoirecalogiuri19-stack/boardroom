package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.repository.PrenotazioniRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.EventiDTO;
import main.service.dto.PrenotazioniEmailDTO;
import main.service.mapper.EventiMapper;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventiService {

    private static final Logger LOG = LoggerFactory.getLogger(EventiService.class);

    private final EventiRepository eventiRepository;
    private final EventiMapper eventiMapper;
    private final PrenotazioniRepository prenotazioniRepository;
    private final StatiPrenotazioneRepository statiPrenotazioneRepository;
    private final MailService mailService;
    private final qrCodeGenerator qrCodeGenerator;

    public EventiService(
        EventiRepository eventiRepository,
        EventiMapper eventiMapper,
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        MailService mailService,
        qrCodeGenerator qrCodeGenerator
    ) {
        this.eventiRepository = eventiRepository;
        this.eventiMapper = eventiMapper;
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.mailService = mailService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @Transactional(readOnly = true)
    public List<EventiDTO> findPublicEventi() {
        LOG.debug("Request to get all public Eventi");
        List<Eventi> eventi = eventiRepository.findPublicConfirmed(TipoEvento.PUBBLICO, StatoCodice.CONFIRMED);
        return eventiMapper.toDto(eventi);
    }

    public EventiDTO createEvento(EventiDTO dto) {
        LOG.debug("REST request to save Eventi : {}", dto);

        Prenotazioni pren = null;

        if (dto.getPrenotazioneId() != null) {
            pren = prenotazioniRepository
                .findById(dto.getPrenotazioneId())
                .orElseThrow(() -> new BadRequestAlertException("Prenotazione non trovata", "eventi", "prenotazioneNotFound"));
        }

        Eventi eventi = buildEventoFromDto(dto, pren);
        setPrezzoInBaseAlTipo(dto, eventi);

        eventi = eventiRepository.save(eventi);

        if (pren != null) {
            aggiornaStatoPrenotazioneConfermata(pren);
        }

        return eventiMapper.toDto(eventi);
    }

    @Transactional(readOnly = true)
    public Page<EventiDTO> findAll(Pageable pageable) {
        return eventiRepository.findAll(pageable).map(eventiMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<EventiDTO> findOne(UUID id) {
        return eventiRepository.findById(id).map(eventiMapper::toDto);
    }

    public EventiDTO update(EventiDTO eventiDTO) {
        Eventi existing = eventiRepository.findById(eventiDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Evento non trovato"));

        existing.setTitolo(eventiDTO.getTitolo());
        existing.setDescrizione(eventiDTO.getDescrizione());
        existing.setTipo(eventiDTO.getTipo());

        setPrezzoInBaseAlTipo(eventiDTO, existing);

        return eventiMapper.toDto(eventiRepository.save(existing));
    }

    public Optional<EventiDTO> partialUpdate(EventiDTO eventiDTO) {
        return eventiRepository
            .findById(eventiDTO.getId())
            .map(existing -> {
                if (eventiDTO.getTitolo() != null) existing.setTitolo(eventiDTO.getTitolo());
                if (eventiDTO.getDescrizione() != null) existing.setDescrizione(eventiDTO.getDescrizione());
                if (existing.getTipo() == TipoEvento.PUBBLICO && eventiDTO.getPrezzo() != null) {
                    existing.setPrezzo(eventiDTO.getPrezzo());
                }
                return eventiRepository.save(existing);
            })
            .map(eventiMapper::toDto);
    }

    public void delete(UUID id) {
        eventiRepository.deleteById(id);
    }

    private Eventi buildEventoFromDto(EventiDTO dto, Prenotazioni prenotazione) {
        Eventi evento = new Eventi();
        evento.setTitolo(dto.getTitolo());
        evento.setDescrizione(dto.getDescrizione());
        evento.setTipo(dto.getTipo());
        evento.setPrenotazione(prenotazione);
        return evento;
    }

    private void setPrezzoInBaseAlTipo(EventiDTO dto, Eventi evento) {
        if (dto.getTipo() == TipoEvento.PUBBLICO) {
            evento.setPrezzo(dto.getPrezzo());
        } else {
            evento.setPrezzo(BigDecimal.ZERO);
        }
    }

    private void aggiornaStatoPrenotazioneConfermata(Prenotazioni prenotazione) {
        statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).ifPresent(prenotazione::setStato);

        prenotazioniRepository.save(prenotazione);
    }

    public void inviaEmailPrenotazione(UUID Id, PrenotazioniEmailDTO dto) {
        Eventi evento = eventiRepository
            .findByIdWithPrenotazioneAndSala(Id)
            .orElseThrow(() -> new EntityNotFoundException("Evento non trovato"));

        Prenotazioni prenotazione = evento.getPrenotazione();

        String codicePre = prenotazione.getCodiceQr();
        if (codicePre == null) {
            codicePre = UUID.randomUUID().toString().substring(0, 8);
            prenotazione.setCodiceQr(codicePre);
            prenotazioniRepository.save(prenotazione);
        }

        String qrCod = qrCodeGenerator.generateQRCodeBase64(codicePre);

        mailService.sendPrenotazioneEventoPublico(evento, dto, codicePre, qrCod);
    }
}
