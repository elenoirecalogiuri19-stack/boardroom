package main.domain;

import static main.domain.EventiTestSamples.*;
import static main.domain.PrenotazioniTestSamples.*;
import static main.domain.SaleTestSamples.*;
import static main.domain.StatiPrenotazioneTestSamples.*;
import static main.domain.UtentiTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrenotazioniTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prenotazioni.class);
        Prenotazioni prenotazioni1 = getPrenotazioniSample1();
        Prenotazioni prenotazioni2 = new Prenotazioni();
        assertThat(prenotazioni1).isNotEqualTo(prenotazioni2);

        prenotazioni2.setId(prenotazioni1.getId());
        assertThat(prenotazioni1).isEqualTo(prenotazioni2);

        prenotazioni2 = getPrenotazioniSample2();
        assertThat(prenotazioni1).isNotEqualTo(prenotazioni2);
    }

    @Test
    void eventiTest() {
        Prenotazioni prenotazioni = getPrenotazioniRandomSampleGenerator();
        Eventi eventiBack = getEventiRandomSampleGenerator();

        prenotazioni.addEventi(eventiBack);
        assertThat(prenotazioni.getEventis()).containsOnly(eventiBack);
        assertThat(eventiBack.getPrenotazione()).isEqualTo(prenotazioni);

        prenotazioni.removeEventi(eventiBack);
        assertThat(prenotazioni.getEventis()).doesNotContain(eventiBack);
        assertThat(eventiBack.getPrenotazione()).isNull();

        prenotazioni.eventis(new HashSet<>(Set.of(eventiBack)));
        assertThat(prenotazioni.getEventis()).containsOnly(eventiBack);
        assertThat(eventiBack.getPrenotazione()).isEqualTo(prenotazioni);

        prenotazioni.setEventis(new HashSet<>());
        assertThat(prenotazioni.getEventis()).doesNotContain(eventiBack);
        assertThat(eventiBack.getPrenotazione()).isNull();
    }

    @Test
    void statoTest() {
        Prenotazioni prenotazioni = getPrenotazioniRandomSampleGenerator();
        StatiPrenotazione statiPrenotazioneBack = getStatiPrenotazioneRandomSampleGenerator();

        prenotazioni.setStato(statiPrenotazioneBack);
        assertThat(prenotazioni.getStato()).isEqualTo(statiPrenotazioneBack);

        prenotazioni.stato(null);
        assertThat(prenotazioni.getStato()).isNull();
    }

    @Test
    void utenteTest() {
        Prenotazioni prenotazioni = getPrenotazioniRandomSampleGenerator();
        Utenti utentiBack = getUtentiRandomSampleGenerator();

        prenotazioni.setUtente(utentiBack);
        assertThat(prenotazioni.getUtente()).isEqualTo(utentiBack);

        prenotazioni.utente(null);
        assertThat(prenotazioni.getUtente()).isNull();
    }

    @Test
    void salaTest() {
        Prenotazioni prenotazioni = getPrenotazioniRandomSampleGenerator();
        Sale saleBack = getSaleRandomSampleGenerator();

        prenotazioni.setSala(saleBack);
        assertThat(prenotazioni.getSala()).isEqualTo(saleBack);

        prenotazioni.sala(null);
        assertThat(prenotazioni.getSala()).isNull();
    }
}
