package main.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import main.IntegrationTest;
import main.domain.Eventi;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.service.mapper.EventiMapper;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
public class EventiResourceTest {

    private static final String PUBLIC_API_URL = "/api/eventis/pubblici";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventiRepository eventiRepository;

    @Autowired
    private EventiMapper eventiMapper;

    private Eventi eventoPubb;

    @BeforeEach
    void initTest() {
        eventoPubb = new Eventi().titolo("Evento pubblici").tipo(TipoEvento.PUBBLICO).prezzo(BigDecimal.valueOf(10));
    }

    @Test
    @Transactional
    public void getPublicEventi_ShouldReturnOnlyPublicEvents() throws Exception {
        eventiRepository.saveAndFlush(eventoPubb);

        mockMvc
            .perform(get(PUBLIC_API_URL).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titolo").value("Evento pubblico"))
            .andExpect(jsonPath("$[0].tipo").value("PUBBLICO"));
    }
}
