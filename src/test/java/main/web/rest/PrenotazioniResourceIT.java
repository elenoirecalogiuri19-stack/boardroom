package main.web.rest;

import static main.domain.PrenotazioniAsserts.*;
import static main.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import main.service.PrenotazioniService;
import main.service.dto.PrenotazioniDTO;
import main.service.mapper.PrenotazioniMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PrenotazioniResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PrenotazioniResourceIT {

    private static final LocalDate DEFAULT_DATA = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATA = LocalDate.now(ZoneId.systemDefault());

    private static final LocalTime DEFAULT_ORA_INIZIO = LocalTime.NOON;
    private static final LocalTime UPDATED_ORA_INIZIO = LocalTime.MAX.withNano(0);

    private static final LocalTime DEFAULT_ORA_FINE = LocalTime.NOON;
    private static final LocalTime UPDATED_ORA_FINE = LocalTime.MAX.withNano(0);

    private static final String ENTITY_API_URL = "/api/prenotazionis";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Mock
    private PrenotazioniRepository prenotazioniRepositoryMock;

    @Autowired
    private PrenotazioniMapper prenotazioniMapper;

    @Mock
    private PrenotazioniService prenotazioniServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrenotazioniMockMvc;

    private Prenotazioni prenotazioni;

    private Prenotazioni insertedPrenotazioni;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private SaleRepository saleRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prenotazioni createEntity() {
        return new Prenotazioni().data(DEFAULT_DATA).oraInizio(DEFAULT_ORA_INIZIO).oraFine(DEFAULT_ORA_FINE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prenotazioni createUpdatedEntity() {
        return new Prenotazioni().data(UPDATED_DATA).oraInizio(UPDATED_ORA_INIZIO).oraFine(UPDATED_ORA_FINE);
    }

    @BeforeEach
    void initTest() {
        prenotazioni = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPrenotazioni != null) {
            prenotazioniRepository.delete(insertedPrenotazioni);
            insertedPrenotazioni = null;
        }
    }

    @Test
    @Transactional
    void createPrenotazioni() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Prenotazioni
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);
        var returnedPrenotazioniDTO = om.readValue(
            restPrenotazioniMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prenotazioniDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PrenotazioniDTO.class
        );

        // Validate the Prenotazioni in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPrenotazioni = prenotazioniMapper.toEntity(returnedPrenotazioniDTO);
        assertPrenotazioniUpdatableFieldsEquals(returnedPrenotazioni, getPersistedPrenotazioni(returnedPrenotazioni));

        insertedPrenotazioni = returnedPrenotazioni;
    }

    @Test
    @Transactional
    void createPrenotazioniWithExistingId() throws Exception {
        // Create the Prenotazioni with an existing ID
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrenotazioniMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDataIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prenotazioni.setData(null);

        // Create the Prenotazioni, which fails.
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        restPrenotazioniMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOraInizioIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prenotazioni.setOraInizio(null);

        // Create the Prenotazioni, which fails.
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        restPrenotazioniMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOraFineIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prenotazioni.setOraFine(null);

        // Create the Prenotazioni, which fails.
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        restPrenotazioniMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPrenotazionis() throws Exception {
        // Initialize the database
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        // Get all the prenotazioniList
        restPrenotazioniMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prenotazioni.getId().toString())))
            .andExpect(jsonPath("$.[*].data").value(hasItem(DEFAULT_DATA.toString())))
            .andExpect(jsonPath("$.[*].oraInizio").value(hasItem(DEFAULT_ORA_INIZIO.toString())))
            .andExpect(jsonPath("$.[*].oraFine").value(hasItem(DEFAULT_ORA_FINE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPrenotazionisWithEagerRelationshipsIsEnabled() throws Exception {
        when(prenotazioniServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPrenotazioniMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(prenotazioniServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPrenotazionisWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(prenotazioniServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPrenotazioniMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(prenotazioniRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPrenotazioni() throws Exception {
        // Initialize the database
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        // Get the prenotazioni
        restPrenotazioniMockMvc
            .perform(get(ENTITY_API_URL_ID, prenotazioni.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prenotazioni.getId().toString()))
            .andExpect(jsonPath("$.data").value(DEFAULT_DATA.toString()))
            .andExpect(jsonPath("$.oraInizio").value(DEFAULT_ORA_INIZIO.toString()))
            .andExpect(jsonPath("$.oraFine").value(DEFAULT_ORA_FINE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPrenotazioni() throws Exception {
        // Get the prenotazioni
        restPrenotazioniMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPrenotazioni() throws Exception {
        // Initialize the database
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prenotazioni
        Prenotazioni updatedPrenotazioni = prenotazioniRepository.findById(prenotazioni.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPrenotazioni are not directly saved in db
        em.detach(updatedPrenotazioni);
        updatedPrenotazioni.data(UPDATED_DATA).oraInizio(UPDATED_ORA_INIZIO).oraFine(UPDATED_ORA_FINE);
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(updatedPrenotazioni);

        restPrenotazioniMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prenotazioniDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isOk());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPrenotazioniToMatchAllProperties(updatedPrenotazioni);
    }

    @Test
    @Transactional
    void putNonExistingPrenotazioni() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        // Create the Prenotazioni
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrenotazioniMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prenotazioniDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPrenotazioni() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        // Create the Prenotazioni
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrenotazioniMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPrenotazioni() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        // Create the Prenotazioni
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrenotazioniMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePrenotazioniWithPatch() throws Exception {
        // Initialize the database
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prenotazioni using partial update
        Prenotazioni partialUpdatedPrenotazioni = new Prenotazioni();
        partialUpdatedPrenotazioni.setId(prenotazioni.getId());

        partialUpdatedPrenotazioni.oraInizio(UPDATED_ORA_INIZIO).oraFine(UPDATED_ORA_FINE);

        restPrenotazioniMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrenotazioni.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrenotazioni))
            )
            .andExpect(status().isOk());

        // Validate the Prenotazioni in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrenotazioniUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPrenotazioni, prenotazioni),
            getPersistedPrenotazioni(prenotazioni)
        );
    }

    @Test
    @Transactional
    void fullUpdatePrenotazioniWithPatch() throws Exception {
        // Initialize the database
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prenotazioni using partial update
        Prenotazioni partialUpdatedPrenotazioni = new Prenotazioni();
        partialUpdatedPrenotazioni.setId(prenotazioni.getId());

        partialUpdatedPrenotazioni.data(UPDATED_DATA).oraInizio(UPDATED_ORA_INIZIO).oraFine(UPDATED_ORA_FINE);

        restPrenotazioniMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrenotazioni.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrenotazioni))
            )
            .andExpect(status().isOk());

        // Validate the Prenotazioni in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrenotazioniUpdatableFieldsEquals(partialUpdatedPrenotazioni, getPersistedPrenotazioni(partialUpdatedPrenotazioni));
    }

    @Test
    @Transactional
    void patchNonExistingPrenotazioni() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        // Create the Prenotazioni
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrenotazioniMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, prenotazioniDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPrenotazioni() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        // Create the Prenotazioni
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrenotazioniMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPrenotazioni() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        // Create the Prenotazioni
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrenotazioniMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prenotazioni in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePrenotazioni() throws Exception {
        // Initialize the database
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the prenotazioni
        restPrenotazioniMockMvc
            .perform(delete(ENTITY_API_URL_ID, prenotazioni.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    void testCreaPrenotazioniCustom() throws Exception {
        initStatiPrenotazione();

        Sale sala = new Sale();
        sala.setNome("Sala Test");
        sala.setCapienza(10);
        sala = saleRepository.saveAndFlush(sala);

        Utenti utente = new Utenti();
        utente.setNome("Utenti Test");
        utente.setNumeroDiTelefono("12345678");
        utente = utentiRepository.saveAndFlush(utente);

        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));
        dto.setSalaId(sala.getId());
        dto.setUtenteId(utente.getId());

        restPrenotazioniMockMvc
            .perform(post("/api/prenotazionis/crea").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.stato.codice").value("WAITING"))
            .andExpect(jsonPath("$.salaId").value(sala.getId().toString()))
            .andExpect(jsonPath("$.utenteId").value(utente.getId().toString()));
    }

    @Test
    @Transactional
    void testConfermaPrenotazioniCustom() throws Exception {
        initStatiPrenotazione();

        Sale sala = new Sale();
        sala.setNome("Sala Test");
        sala.setCapienza(10);
        sala = saleRepository.saveAndFlush(sala);

        Utenti utente = new Utenti();
        utente.setNome("Mario Rossi");
        utente.setNumeroDiTelefono("12345678");
        utente = utentiRepository.saveAndFlush(utente);

        Prenotazioni p = new Prenotazioni();
        p.setData(LocalDate.now().plusDays(1));
        p.setOraInizio(LocalTime.of(10, 0));
        p.setOraFine(LocalTime.of(11, 0));
        p.setSala(sala);
        p.setUtente(utente);
        p.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING).get());
        p = prenotazioniRepository.saveAndFlush(p);

        restPrenotazioniMockMvc
            .perform(post("/api/prenotazionis/" + p.getId() + "/conferma"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stato.codice").value("CONFIRMED"));
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
        return prenotazioniRepository.count();
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

    protected Prenotazioni getPersistedPrenotazioni(Prenotazioni prenotazioni) {
        return prenotazioniRepository.findById(prenotazioni.getId()).orElseThrow();
    }

    protected void assertPersistedPrenotazioniToMatchAllProperties(Prenotazioni expectedPrenotazioni) {
        assertPrenotazioniAllPropertiesEquals(expectedPrenotazioni, getPersistedPrenotazioni(expectedPrenotazioni));
    }

    protected void assertPersistedPrenotazioniToMatchUpdatableProperties(Prenotazioni expectedPrenotazioni) {
        assertPrenotazioniAllUpdatablePropertiesEquals(expectedPrenotazioni, getPersistedPrenotazioni(expectedPrenotazioni));
    }
}
