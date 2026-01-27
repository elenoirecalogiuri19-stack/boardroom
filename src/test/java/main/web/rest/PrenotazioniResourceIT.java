package main.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
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
import main.service.dto.PrenotazioniDTO;
import main.service.dto.SaleDTO; // IMPORT AGGIUNTO
import main.service.dto.UtentiDTO; // IMPORT AGGIUNTO
import main.service.mapper.PrenotazioniMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PrenotazioniResourceIT {

    private static final LocalDate DEFAULT_DATA = LocalDate.ofEpochDay(0L);
    private static final LocalTime DEFAULT_ORA_INIZIO = LocalTime.NOON;
    private static final LocalTime DEFAULT_ORA_FINE = LocalTime.NOON;

    private static final String ENTITY_API_URL = "/api/prenotazionis";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Autowired
    private PrenotazioniMapper prenotazioniMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrenotazioniMockMvc;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private SaleRepository saleRepository;

    private Prenotazioni prenotazioni;

    public static Prenotazioni createEntity() {
        Prenotazioni p = new Prenotazioni();
        p.setData(DEFAULT_DATA);
        p.setOraInizio(DEFAULT_ORA_INIZIO);
        p.setOraFine(DEFAULT_ORA_FINE);
        return p;
    }

    @BeforeEach
    void initTest() {
        prenotazioni = createEntity();
    }

    @Test
    @Transactional
    void createPrenotazioni() throws Exception {
        int databaseSizeBeforeCreate = prenotazioniRepository.findAll().size();
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        restPrenotazioniMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isCreated());

        assertThat(prenotazioniRepository.findAll().size()).isEqualTo(databaseSizeBeforeCreate + 1);
    }

    @Test
    @Transactional
    void getAllPrenotazionis() throws Exception {
        prenotazioniRepository.saveAndFlush(prenotazioni);

        restPrenotazioniMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prenotazioni.getId().toString())));
    }

    @Test
    @Transactional
    void testCreaPrenotazioniCustom() throws Exception {
        initStatiPrenotazione();

        Sale salaEntity = new Sale();
        salaEntity.setNome("Sala Test");
        salaEntity.setCapienza(10);
        salaEntity = saleRepository.saveAndFlush(salaEntity);

        Utenti utenteEntity = new Utenti();
        utenteEntity.setNome("Utenti Test");
        utenteEntity.setNumeroDiTelefono("12345678");
        utenteEntity = utentiRepository.saveAndFlush(utenteEntity);

        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));

        // Riferimento esplicito ai DTO
        SaleDTO sDto = new SaleDTO();
        sDto.setId(salaEntity.getId());
        dto.setSala(sDto);

        UtentiDTO uDto = new UtentiDTO();
        uDto.setId(utenteEntity.getId());
        dto.setUtente(uDto);

        restPrenotazioniMockMvc
            .perform(post("/api/prenotazionis/crea").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.sala.id").value(salaEntity.getId().toString()))
            .andExpect(jsonPath("$.utente.id").value(utenteEntity.getId().toString()));
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
}
