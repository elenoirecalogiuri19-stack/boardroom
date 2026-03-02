package main.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.Sale;
import main.repository.SaleRepository;
import main.service.dto.SaleDTO;
import main.service.mapper.SaleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SaleResourceIT {

    private static final String ENTITY_API_URL = "/api/sales";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleMapper saleMapper;

    private Sale sale;

    @BeforeEach
    void initTest() {
        sale = new Sale().nome("Sala Test").capienza(10).descrizione("Descrizione test");
    }

    @Test
    @Transactional
    void createSale_shouldCreate() throws Exception {
        long countBefore = saleRepository.count();

        SaleDTO dto = saleMapper.toDto(sale);

        String response = mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        SaleDTO created = objectMapper.readValue(response, SaleDTO.class);

        assertThat(saleRepository.count()).isEqualTo(countBefore + 1);
        Sale saved = saleRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getNome()).isEqualTo("Sala Test");
        assertThat(saved.getCapienza()).isEqualTo(10);
    }

    @Test
    @Transactional
    void updateSale_shouldUpdate() throws Exception {
        sale = saleRepository.saveAndFlush(sale);

        SaleDTO dto = saleMapper.toDto(sale);
        dto.setNome("Aggiornata");
        dto.setCapienza(50);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, sale.getId()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto))
            )
            .andExpect(status().isOk());

        Sale updated = saleRepository.findById(sale.getId()).orElseThrow();
        assertThat(updated.getNome()).isEqualTo("Aggiornata");
        assertThat(updated.getCapienza()).isEqualTo(50);
    }

    @Test
    @Transactional
    void getAllSales_shouldReturnList() throws Exception {
        saleRepository.saveAndFlush(sale);

        mockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk()).andExpect(jsonPath("$.[*].id").exists());
    }

    @Test
    @Transactional
    void getAllFreeSales_shouldReturnList() throws Exception {
        saleRepository.saveAndFlush(sale);

        mockMvc
            .perform(
                get(ENTITY_API_URL + "/disponibili")
                    .param("data", LocalDate.now().toString())
                    .param("inizio", LocalTime.of(10, 0).toString())
                    .param("fine", LocalTime.of(11, 0).toString())
            )
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void deleteSale_shouldDelete() throws Exception {
        sale = saleRepository.saveAndFlush(sale);
        long countBefore = saleRepository.count();

        mockMvc.perform(delete(ENTITY_API_URL_ID, sale.getId())).andExpect(status().isNoContent());

        assertThat(saleRepository.count()).isEqualTo(countBefore - 1);
    }
}
