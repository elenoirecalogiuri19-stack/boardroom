package main.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrenotazioniDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PrenotazioniDTO.class);
        PrenotazioniDTO prenotazioniDTO1 = new PrenotazioniDTO();
        prenotazioniDTO1.setId(UUID.randomUUID());
        PrenotazioniDTO prenotazioniDTO2 = new PrenotazioniDTO();
        assertThat(prenotazioniDTO1).isNotEqualTo(prenotazioniDTO2);
        prenotazioniDTO2.setId(prenotazioniDTO1.getId());
        assertThat(prenotazioniDTO1).isEqualTo(prenotazioniDTO2);
        prenotazioniDTO2.setId(UUID.randomUUID());
        assertThat(prenotazioniDTO1).isNotEqualTo(prenotazioniDTO2);
        prenotazioniDTO1.setId(null);
        assertThat(prenotazioniDTO1).isNotEqualTo(prenotazioniDTO2);
    }
}
