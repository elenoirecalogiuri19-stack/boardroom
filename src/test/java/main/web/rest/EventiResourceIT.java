package main.web.rest;

import static main.domain.EventiAsserts.*;
import static main.web.rest.TestUtil.createUpdateProxyForBean;
import static main.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import main.service.MailService;
import main.service.dto.EventiDTO;
import main.service.dto.PrenotazioniEmailDTO;
import main.service.mapper.EventiMapper;
import main.service.qrCodeGenerator;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
@Import(EventiResourceIT.MockConfig.class)
class EventiResourceIT {

    @TestConfiguration
    static class MockConfig {

        @Bean
        public MailService mailService() {
            return Mockito.mock(MailService.class);
        }

        @Bean
        public qrCodeGenerator qrCodeGenerator() {
            return Mockito.mock(qrCodeGenerator.class);
        }
    }

    private static final String ENTITY_API_URL = "/api/eventis";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final String DEFAULT_TITOLO = "AAAAAAAAAA";
    private static final String UPDATED_TITOLO = "BBBBBBBBBB";

    private static final TipoEvento DEFAULT_TIPO = TipoEvento.PRIVATO;
    private static final TipoEvento UPDATED_TIPO = TipoEvento.PUBBLICO;

    private static final BigDecimal DEFAULT_PREZZO = BigDecimal.ONE;
    private static final BigDecimal UPDATED_PREZZO = BigDecimal.valueOf(2);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventiRepository eventiRepository;

    @Autowired
    private EventiMapper eventiMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private qrCodeGenerator qrCodeGenerator;

    private Eventi eventi;
    private Eventi insertedEventi;

    @BeforeEach
    void initTest() {
        eventi = new Eventi().titolo(DEFAULT_TITOLO).tipo(DEFAULT_TIPO).prezzo(DEFAULT_PREZZO);
    }

    @AfterEach
    void cleanup() {
        if (insertedEventi != null) {
            eventiRepository.delete(insertedEventi);
            insertedEventi = null;
        }
    }

    @Test
    @Transactional
    void createEventi_shouldPersistEntity() throws Exception {
        long countBefore = getRepositoryCount();

        EventiDTO dto = eventiMapper.toDto(eventi);

        EventiDTO returned = objectMapper.readValue(
            mockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EventiDTO.class
        );

        assertIncrementedRepositoryCount(countBefore);

        insertedEventi = eventiMapper.toEntity(returned);
        assertEventiUpdatableFieldsEquals(insertedEventi, getPersistedEventi(insertedEventi));
    }

    @Test
    @Transactional
    void createEventiWithExistingId_shouldReturnBadRequest() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        EventiDTO dto = eventiMapper.toDto(eventi);

        long countBefore = getRepositoryCount();

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(countBefore);
    }

    @Test
    @Transactional
    void getAllEventis_shouldReturnList() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        mockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventi.getId().toString())))
            .andExpect(jsonPath("$.[*].titolo").value(hasItem(DEFAULT_TITOLO)))
            .andExpect(jsonPath("$.[*].tipo").value(hasItem(DEFAULT_TIPO.toString())))
            .andExpect(jsonPath("$.[*].prezzo").value(hasItem(sameNumber(DEFAULT_PREZZO))));
    }

    @Test
    @Transactional
    void getEventi_shouldReturnSingleEntity() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        mockMvc
            .perform(get(ENTITY_API_URL_ID, eventi.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(eventi.getId().toString()))
            .andExpect(jsonPath("$.titolo").value(DEFAULT_TITOLO))
            .andExpect(jsonPath("$.tipo").value(DEFAULT_TIPO.toString()))
            .andExpect(jsonPath("$.prezzo").value(sameNumber(DEFAULT_PREZZO)));
    }

    @Test
    @Transactional
    void getNonExistingEventi_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEventi_shouldUpdateEntity() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        long countBefore = getRepositoryCount();

        Eventi updated = eventiRepository.findById(eventi.getId()).orElseThrow();
        entityManager.detach(updated);

        updated.titolo(UPDATED_TITOLO).tipo(UPDATED_TIPO).prezzo(UPDATED_PREZZO);

        EventiDTO dto = eventiMapper.toDto(updated);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, dto.getId()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(countBefore);
        assertPersistedEventiToMatchAllProperties(updated);
    }

    @Test
    @Transactional
    void partialUpdateEventiWithPatch_shouldUpdateSelectedFields() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        long countBefore = getRepositoryCount();

        Eventi partial = new Eventi();
        partial.setId(eventi.getId());
        partial.titolo(UPDATED_TITOLO);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partial.getId())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(partial))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(countBefore);
        assertEventiUpdatableFieldsEquals(createUpdateProxyForBean(partial, eventi), getPersistedEventi(eventi));
    }

    @Test
    @Transactional
    void deleteEventi_shouldRemoveEntity() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        long countBefore = getRepositoryCount();

        mockMvc.perform(delete(ENTITY_API_URL_ID, eventi.getId())).andExpect(status().isNoContent());

        assertDecrementedRepositoryCount(countBefore);
    }

    @Test
    @Transactional
    void getPublicEvents_shouldReturnOnlyPublicEvents() throws Exception {
        initStatiPrenotazione();

        Prenotazioni pren = createConfirmedPrenotazione();

        Eventi pub = new Eventi();
        pub.setTitolo("Pubblico");
        pub.setTipo(TipoEvento.PUBBLICO);
        pub.setPrenotazione(pren);
        eventiRepository.saveAndFlush(pub);

        Eventi priv = new Eventi();
        priv.setTitolo("Privato");
        priv.setTipo(TipoEvento.PRIVATO);
        priv.setPrenotazione(pren);
        eventiRepository.saveAndFlush(priv);

        mockMvc
            .perform(get("/api/eventis/pubblici"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].titolo").value(hasItem("Pubblico")))
            .andExpect(jsonPath("$.[*].titolo").value(not(hasItem("Privato"))));
    }

    @Test
    @Transactional
    void prenotazioneEmail_shouldReturnOk() throws Exception {
        initStatiPrenotazione();

        Prenotazioni pren = createConfirmedPrenotazione();

        eventi.setPrenotazione(pren);
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        doNothing().when(mailService).sendPrenotazioneEventoPublico(any(), any(), any(), any());

        when(qrCodeGenerator.generateQRCodeBase64(any())).thenReturn("FAKE_QR_CODE");

        PrenotazioniEmailDTO dto = new PrenotazioniEmailDTO();
        dto.setNome("Mario");
        dto.setCognome("Rossi");
        dto.setEmail("test@example.com");

        mockMvc
            .perform(
                post(ENTITY_API_URL_ID + "/prenotazione-email", insertedEventi.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto))
            )
            .andExpect(status().isOk());
    }

    private void initStatiPrenotazione() {
        if (statiPrenotazioneRepository.count() == 0) {
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.WAITING).descrizione("In attesa").ordineAzione(1)
            );
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.CONFIRMED).descrizione("Confermata").ordineAzione(2)
            );
        }
    }

    private Prenotazioni createConfirmedPrenotazione() {
        Sale sala = new Sale();
        sala.setNome("Sala Test");
        sala.setCapienza(10);
        sala = saleRepository.saveAndFlush(sala);

        Utenti utente = new Utenti();
        utente.setNome("Mario");
        utente.setNumeroDiTelefono("3331234567");
        utente = utentiRepository.saveAndFlush(utente);

        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setSala(sala);
        pren.setUtente(utente);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());

        return prenotazioniRepository.saveAndFlush(pren);
    }

    private long getRepositoryCount() {
        return eventiRepository.count();
    }

    private void assertIncrementedRepositoryCount(long before) {
        assertThat(before + 1).isEqualTo(getRepositoryCount());
    }

    private void assertDecrementedRepositoryCount(long before) {
        assertThat(before - 1).isEqualTo(getRepositoryCount());
    }

    private void assertSameRepositoryCount(long before) {
        assertThat(before).isEqualTo(getRepositoryCount());
    }

    private Eventi getPersistedEventi(Eventi eventi) {
        return eventiRepository.findById(eventi.getId()).orElseThrow();
    }

    private void assertPersistedEventiToMatchAllProperties(Eventi expected) {
        assertEventiAllPropertiesEquals(expected, getPersistedEventi(expected));
    }
}
