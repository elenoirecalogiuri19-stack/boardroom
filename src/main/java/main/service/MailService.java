package main.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.User;
import main.service.dto.PrenotazioniEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/**
 * Service for sending emails asynchronously.
 */
@Service
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;
    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;

    public MailService(
        JHipsterProperties jHipsterProperties,
        JavaMailSender javaMailSender,
        MessageSource messageSource,
        SpringTemplateEngine templateEngine
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        sendEmailSync(to, subject, content, isMultipart, isHtml);
    }

    private void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        LOG.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}'", isMultipart, isHtml, to, subject);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            LOG.debug("Sent email to '{}'", to);
        } catch (MailException | MessagingException e) {
            LOG.warn("Email could not be sent to '{}'", to, e);
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        sendEmailFromTemplateSync(user, templateName, titleKey);
    }

    private void sendEmailFromTemplateSync(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            LOG.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        try {
            Locale locale = Locale.forLanguageTag(user.getLangKey());
            Context context = new Context(locale);
            context.setVariable(USER, user);
            context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());

            String content = templateEngine.process(templateName, context);
            String subject = messageSource.getMessage(titleKey, null, locale);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(jHipsterProperties.getMail().getFrom());
            helper.setSubject(subject);
            helper.setText(content, true);

            ClassPathResource logo = new ClassPathResource("imags/logo-jhipster.png");
            helper.addInline("logoimg", logo, "image/png");
            javaMailSender.send(mimeMessage);
            LOG.debug("Sent email to '{}'", user.getEmail());
        } catch (MessagingException e) {
            LOG.error("Errore durante l'invio dell'email da template {}", templateName, e);
        }
    }

    @Async
    public void sendActivationEmail(User user) {
        LOG.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        LOG.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        LOG.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/passwordResetEmail", "email.reset.title");
    }

    @Async
    public void sendPrenotazioneEventoPublico(Eventi evento, PrenotazioniEmailDTO dto, String codicePre, String qrCod) {
        try {
            Context context = new Context();
            context.setVariable("titoloEvento", evento.getTitolo());
            context.setVariable("descrizioneEvento", evento.getDescrizione());
            context.setVariable("tipoEvento", evento.getTipo().name());
            context.setVariable("prezzoEvento", evento.getPrezzo());

            Prenotazioni p = evento.getPrenotazione();
            context.setVariable("dataPrenotazione", p.getData());
            context.setVariable("oraInizio", p.getOraInizio());
            context.setVariable("oraFine", p.getOraFine());
            context.setVariable("salaNome", p.getSala().getNome());
            context.setVariable("salaCapienza", p.getSala().getCapienza());
            context.setVariable("nome", dto.getNome());
            context.setVariable("cognome", dto.getCognome());
            context.setVariable("email", dto.getEmail());
            context.setVariable("codicePre", codicePre);

            String content = templateEngine.process("mail/eventoPrenotazioneEmail", context);
            byte[] qrBytes = Base64.getDecoder().decode(qrCod);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(dto.getEmail());
            helper.setFrom(jHipsterProperties.getMail().getFrom());
            helper.setSubject("Conferma Prenotazione Evento");
            helper.setText(content, true);
            helper.addInline("qrcode", new ByteArrayResource(qrBytes), "image/png");

            ClassPathResource logo = new ClassPathResource("imags/logo-jhipster.png");
            helper.addInline("logoimg", logo, "image/png");
            javaMailSender.send(mimeMessage);
            LOG.debug("Sent prenotazione email to '{}'", dto.getEmail());
        } catch (MessagingException e) {
            LOG.error("Errore durante l'invio dell'email di prenotazione", e);
        }
    }

    // ── PROMEMORIA ────────────────────────────────────────────────────────────

    /**
     * Invia l'email di promemoria per una prenotazione.
     * Usa il template mail/promemoriaEmail.html.
     *
     * @param email         indirizzo destinatario
     * @param nomeUtente    nome dell'utente per la personalizzazione
     * @param titoloEvento  titolo dell'evento/prenotazione
     * @param data          data formattata in italiano (es: "lunedì 7 aprile 2026")
     * @param oraInizio     ora di inizio (es: "09:00")
     * @param oraFine       ora di fine (es: "11:00")
     * @param nomeSala      nome della sala
     * @param etichettaTipo etichetta del tipo promemoria (es: "1 giorno prima")
     */
    @Async
    public void sendPromemoriaPrenotazione(
        String email,
        String nomeUtente,
        String titoloEvento,
        String data,
        String oraInizio,
        String oraFine,
        String nomeSala,
        String etichettaTipo
    ) {
        try {
            Context context = new Context();
            context.setVariable("nomeUtente", nomeUtente);
            context.setVariable("titoloEvento", titoloEvento);
            context.setVariable("dataPrenotazione", data);
            context.setVariable("oraInizio", oraInizio);
            context.setVariable("oraFine", oraFine);
            context.setVariable("nomeSala", nomeSala);
            context.setVariable("etichettaTipo", etichettaTipo);

            String content = templateEngine.process("mail/promemoriaEmail", context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setFrom(jHipsterProperties.getMail().getFrom());
            helper.setSubject("Promemoria: " + titoloEvento + " — " + etichettaTipo);
            helper.setText(content, true);

            ClassPathResource logo = new ClassPathResource("imags/logo-jhipster.png");
            helper.addInline("logoimg", logo, "image/png");

            javaMailSender.send(mimeMessage);
            LOG.debug("Promemoria inviato a '{}' — tipo: {}", email, etichettaTipo);
        } catch (MessagingException e) {
            LOG.error("Errore invio promemoria a '{}'", email, e);
            // Rilancia per permettere allo scheduler di loggare l'errore
            throw new RuntimeException("Invio promemoria fallito: " + e.getMessage(), e);
        }
    }
}
