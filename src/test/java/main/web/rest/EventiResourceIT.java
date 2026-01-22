package main.web.rest;

import static main.domain.EventiAsserts.*;
import static main.domain.enumeration.TipoEvento.PUBBLICO;
import static main.web.rest.TestUtil.createUpdateProxyForBean;
import static main.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.*;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.*;
import main.service.dto.EventiDTO;
import main.service.mapper.EventiMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link EventiResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventiResourceIT {

    private static final String DEFAULT_TITOLO = "AAAAAAAAAA";
    private static final String UPDATED_TITOLO = "BBBBBBBBBB";

    private static final TipoEvento DEFAULT_TIPO = TipoEvento.PRIVATO;
    private static final TipoEvento UPDATED_TIPO = PUBBLICO;

    private static final BigDecimal DEFAULT_PREZZO = new BigDecimal(1);
    private static final BigDecimal UPDATED_PREZZO = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/eventis";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EventiRepository eventiRepository;

    @Autowired
    private EventiMapper eventiMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEventiMockMvc;

    private Eventi eventi;

    private Eventi insertedEventi;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Eventi createEntity() {
        return new Eventi().titolo(DEFAULT_TITOLO).tipo(DEFAULT_TIPO).prezzo(DEFAULT_PREZZO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Eventi createUpdatedEntity() {
        return new Eventi().titolo(UPDATED_TITOLO).tipo(UPDATED_TIPO).prezzo(UPDATED_PREZZO);
    }

    @BeforeEach
    void initTest() {
        eventi = createEntity();
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
    void createEventi() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Eventi
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);
        var returnedEventiDTO = om.readValue(
            restEventiMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EventiDTO.class
        );

        // Validate the Eventi in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEventi = eventiMapper.toEntity(returnedEventiDTO);
        assertEventiUpdatableFieldsEquals(returnedEventi, getPersistedEventi(returnedEventi));

        insertedEventi = returnedEventi;
    }

    @Test
    @Transactional
    void createEventiWithExistingId() throws Exception {
        // Create the Eventi with an existing ID
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventiMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitoloIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventi.setTitolo(null);

        // Create the Eventi, which fails.
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        restEventiMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTipoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventi.setTipo(null);

        // Create the Eventi, which fails.
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        restEventiMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEventis() throws Exception {
        // Initialize the database
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        // Get all the eventiList
        restEventiMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventi.getId().toString())))
            .andExpect(jsonPath("$.[*].titolo").value(hasItem(DEFAULT_TITOLO)))
            .andExpect(jsonPath("$.[*].tipo").value(hasItem(DEFAULT_TIPO.toString())))
            .andExpect(jsonPath("$.[*].prezzo").value(hasItem(sameNumber(DEFAULT_PREZZO))));
    }

    @Test
    @Transactional
    void getEventi() throws Exception {
        // Initialize the database
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        // Get the eventi
        restEventiMockMvc
            .perform(get(ENTITY_API_URL_ID, eventi.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(eventi.getId().toString()))
            .andExpect(jsonPath("$.titolo").value(DEFAULT_TITOLO))
            .andExpect(jsonPath("$.tipo").value(DEFAULT_TIPO.toString()))
            .andExpect(jsonPath("$.prezzo").value(sameNumber(DEFAULT_PREZZO)));
    }

    @Test
    @Transactional
    void getNonExistingEventi() throws Exception {
        // Get the eventi
        restEventiMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEventi() throws Exception {
        // Initialize the database
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventi
        Eventi updatedEventi = eventiRepository.findById(eventi.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEventi are not directly saved in db
        em.detach(updatedEventi);
        updatedEventi.titolo(UPDATED_TITOLO).tipo(UPDATED_TIPO).prezzo(UPDATED_PREZZO);
        EventiDTO eventiDTO = eventiMapper.toDto(updatedEventi);

        restEventiMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventiDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isOk());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEventiToMatchAllProperties(updatedEventi);
    }

    @Test
    @Transactional
    void putNonExistingEventi() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        // Create the Eventi
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventiMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventiDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEventi() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        // Create the Eventi
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventiMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEventi() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        // Create the Eventi
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventiMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEventiWithPatch() throws Exception {
        // Initialize the database
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventi using partial update
        Eventi partialUpdatedEventi = new Eventi();
        partialUpdatedEventi.setId(eventi.getId());

        partialUpdatedEventi.titolo(UPDATED_TITOLO);

        restEventiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventi.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventi))
            )
            .andExpect(status().isOk());

        // Validate the Eventi in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventiUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedEventi, eventi), getPersistedEventi(eventi));
    }

    @Test
    @Transactional
    void fullUpdateEventiWithPatch() throws Exception {
        // Initialize the database
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventi using partial update
        Eventi partialUpdatedEventi = new Eventi();
        partialUpdatedEventi.setId(eventi.getId());

        partialUpdatedEventi.titolo(UPDATED_TITOLO).tipo(UPDATED_TIPO).prezzo(UPDATED_PREZZO);

        restEventiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventi.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventi))
            )
            .andExpect(status().isOk());

        // Validate the Eventi in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventiUpdatableFieldsEquals(partialUpdatedEventi, getPersistedEventi(partialUpdatedEventi));
    }

    @Test
    @Transactional
    void patchNonExistingEventi() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        // Create the Eventi
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, eventiDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEventi() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        // Create the Eventi
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEventi() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        // Create the Eventi
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventiMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Eventi in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEventi() throws Exception {
        // Initialize the database
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the eventi
        restEventiMockMvc
            .perform(delete(ENTITY_API_URL_ID, eventi.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    void testCreaEventoPubblico() throws Exception {
        initStatiPrenotazione();

        Sale sala = new Sale();
        sala.setNome("Sala Test");
        sala.setCapienza(10);
        sala = saleRepository.saveAndFlush(sala);

        Utenti utente = new Utenti();
        utente.setNome("Mario");
        utente.setNumeroDiTelefono("3331234567");
        utente = utentiRepository.saveAndFlush(utente);

        Prenotazioni p = new Prenotazioni();
        p.setData(LocalDate.now().plusDays(1));
        p.setOraInizio(LocalTime.of(10, 0));
        p.setOraFine(LocalTime.of(11, 0));
        p.setSala(sala);
        p.setUtente(utente);
        p.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).get());
        p = prenotazioniRepository.saveAndFlush(p);

        EventiDTO dto = new EventiDTO();
        dto.setPrenotazioneId(p.getId());
        dto.setTitolo("Concerto");
        dto.setTipo(PUBBLICO);
        dto.setPrezzo(BigDecimal.valueOf(20));

        restEventiMockMvc
            .perform(post("/api/eventis/crea-pubblico").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo").value("PUBBLICO"))
            .andExpect(jsonPath("$.prezzo").value(20));
    }

    @Test
    @Transactional
    void testEventoPubblicoPrenotazioneNonConfermata() throws Exception {
        initStatiPrenotazione();

        Prenotazioni p = new Prenotazioni();
        p.setData(LocalDate.now().plusDays(1));
        p.setOraInizio(LocalTime.of(10, 0));
        p.setOraFine(LocalTime.of(11, 0));
        p.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING).get());
        p = prenotazioniRepository.saveAndFlush(p);

        EventiDTO dto = new EventiDTO();
        dto.setPrenotazioneId(p.getId());
        dto.setTitolo("Evento");
        dto.setTipo(PUBBLICO);
        dto.setPrezzo(BigDecimal.valueOf(10));

        restEventiMockMvc
            .perform(post("/api/eventis/crea-pubblico").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void testGetEventiPubblici() throws Exception {
        Eventi e1 = new Eventi();
        e1.setTitolo("Pubblico");
        e1.setTipo(PUBBLICO);
        eventiRepository.saveAndFlush(e1);

        Eventi e2 = new Eventi();
        e2.setTitolo("Privato");
        e2.setTipo(TipoEvento.PRIVATO);
        eventiRepository.saveAndFlush(e2);

        restEventiMockMvc
            .perform(get("/api/eventis/pubblici"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].titolo").value(hasItem("Pubblico")))
            .andExpect(jsonPath("$.[*].titolo").value(not(hasItem("Privato"))));
    }

    private void initStatiPrenotazione() {
        if (statiPrenotazioneRepository.count() == 0) {
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.WAITING).descrizione("In attesa").ordineAzione(1)
            );
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.CONFIRMED).descrizione("Confermata").ordineAzione(2)
            );
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.REJECTED).descrizione("Rifiutata").ordineAzione(3)
            );
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.CANCELLED).descrizione("Annullata").ordineAzione(4)
            );
        }
    }

    protected long getRepositoryCount() {
        return eventiRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Eventi getPersistedEventi(Eventi eventi) {
        return eventiRepository.findById(eventi.getId()).orElseThrow();
    }

    protected void assertPersistedEventiToMatchAllProperties(Eventi expectedEventi) {
        assertEventiAllPropertiesEquals(expectedEventi, getPersistedEventi(expectedEventi));
    }

    protected void assertPersistedEventiToMatchUpdatableProperties(Eventi expectedEventi) {
        assertEventiAllUpdatablePropertiesEquals(expectedEventi, getPersistedEventi(expectedEventi));
    }
}
