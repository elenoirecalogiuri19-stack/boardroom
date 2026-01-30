package main.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import main.IntegrationTest;
import main.domain.Eventi;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Focused tests for the /api/eventis/pubblici endpoint.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventiResourceTest {

    private static final String PUBLIC_API_URL = "/api/eventis/pubblici";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventiRepository eventiRepository;

    private Eventi publicEvent;

    @BeforeEach
    void initTest() {
        publicEvent = new Eventi().titolo("Evento pubblico").tipo(TipoEvento.PUBBLICO).prezzo(BigDecimal.valueOf(10));
    }

    @Test
    @Transactional
    void getPublicEventi_shouldReturnOnlyPublicEvents() throws Exception {
        eventiRepository.saveAndFlush(publicEvent);

        mockMvc
            .perform(get(PUBLIC_API_URL).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titolo").value("Evento pubblico"))
            .andExpect(jsonPath("$[0].tipo").value("PUBBLICO"));
    }
}
