package main.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import main.IntegrationTest;
import main.domain.Authority;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.User;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UserRepository;
import main.repository.UtentiRepository;
import main.service.dto.PrenotazioniDTO;
import main.service.dto.SaleDTO;
import main.service.dto.UtentiDTO;
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
@WithMockUser(username = "testuser")
class PrenotazioniResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PrenotazioniMapper prenotazioniMapper;

    @Autowired
    private EntityManager em;

    private Utenti utente;
    private Sale sala;

    @BeforeEach
    @Transactional
    void setup() {
        initStati();

        String validPassword = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5R1sK2JwIu1ZyWfZPpQhZrYyW5F1C";

        User user = new User();
        user.setLogin("testuser");
        user.setPassword(validPassword);
        user.setActivated(true);

        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        Set<Authority> authorities = new HashSet<>();
        authorities.add(authority);
        user.setAuthorities(authorities);

        user = userRepository.saveAndFlush(user);

        utente = new Utenti();
        utente.setNome("Mario");
        utente.setNumeroDiTelefono("3331234567");
        utente.setUser(user);
        utente = utentiRepository.saveAndFlush(utente);

        sala = new Sale();
        sala.setNome("Sala Test");
        sala.setCapienza(10);
        sala = saleRepository.saveAndFlush(sala);
    }

    private void initStati() {
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

    @Test
    @Transactional
    void creaPrenotazione_shouldCreateWaitingPrenotazione() throws Exception {
        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));

        UtentiDTO uDto = new UtentiDTO();
        uDto.setId(utente.getId());
        dto.setUtente(uDto);

        SaleDTO sDto = new SaleDTO();
        sDto.setId(sala.getId());
        dto.setSala(sDto);

        String response = mockMvc
            .perform(post("/api/prenotazionis/crea").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(dto.getData().toString()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        PrenotazioniDTO created = objectMapper.readValue(response, PrenotazioniDTO.class);

        Prenotazioni saved = prenotazioniRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getStato().getCodice()).isEqualTo(StatoCodice.WAITING);
    }

    @Test
    @Transactional
    void confermaPrenotazione_shouldSetConfirmed() throws Exception {
        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setUtente(utente);
        pren.setSala(sala);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING).orElseThrow());
        pren = prenotazioniRepository.saveAndFlush(pren);

        mockMvc.perform(post("/api/prenotazionis/" + pren.getId() + "/conferma")).andExpect(status().isOk());

        Prenotazioni updated = prenotazioniRepository.findById(pren.getId()).orElseThrow();
        assertThat(updated.getStato().getCodice()).isEqualTo(StatoCodice.CONFIRMED);
    }

    @Test
    @Transactional
    void deletePrenotazione_shouldCancelIfOwner() throws Exception {
        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setUtente(utente);
        pren.setSala(sala);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());
        pren = prenotazioniRepository.saveAndFlush(pren);

        mockMvc.perform(delete("/api/prenotazionis/cancella/" + pren.getId())).andExpect(status().isNoContent());

        Prenotazioni updated = prenotazioniRepository.findById(pren.getId()).orElseThrow();
        assertThat(updated.getStato().getCodice()).isEqualTo(StatoCodice.CANCELLED);
    }

    @Test
    @Transactional
    void getAll_withSalaFilter_shouldReturnOnlyMatching() throws Exception {
        Prenotazioni p1 = new Prenotazioni();
        p1.setData(LocalDate.now().plusDays(1));
        p1.setOraInizio(LocalTime.of(10, 0));
        p1.setOraFine(LocalTime.of(11, 0));
        p1.setSala(sala);
        p1 = prenotazioniRepository.saveAndFlush(p1);

        Sale sala2 = new Sale();
        sala2.setNome("Sala 2");
        sala2.setCapienza(20);
        sala2 = saleRepository.saveAndFlush(sala2);

        Prenotazioni p2 = new Prenotazioni();
        p2.setData(LocalDate.now().plusDays(1));
        p2.setOraInizio(LocalTime.of(10, 0));
        p2.setOraFine(LocalTime.of(11, 0));
        p2.setSala(sala2);
        p2 = prenotazioniRepository.saveAndFlush(p2);

        mockMvc
            .perform(get("/api/prenotazionis?salaId=" + sala.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(org.hamcrest.Matchers.hasItem(p1.getId().toString())))
            .andExpect(jsonPath("$.[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(p2.getId().toString()))));
    }
}
