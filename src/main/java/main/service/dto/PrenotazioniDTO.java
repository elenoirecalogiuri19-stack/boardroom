package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link main.domain.Prenotazioni} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PrenotazioniDTO implements Serializable {

    private UUID id;

    @NotNull
    private LocalDate data;

    @NotNull
    private LocalTime oraInizio;

    @NotNull
    private LocalTime oraFine;

    private String tipoEvento;
    private Double prezzo;
    private StatiPrenotazioneDTO stato;

    private UtentiDTO utente;

    private SaleDTO sala;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }

    public void setOraFine(LocalTime oraFine) {
        this.oraFine = oraFine;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(Double prezzo) {
        this.prezzo = prezzo;
    }

    public StatiPrenotazioneDTO getStato() {
        return stato;
    }

    public void setStato(StatiPrenotazioneDTO stato) {
        this.stato = stato;
    }

    public UtentiDTO getUtente() {
        return utente;
    }

    public void setUtente(UtentiDTO utente) {
        this.utente = utente;
    }

    public SaleDTO getSala() {
        return sala;
    }

    public void setSala(SaleDTO sala) {
        this.sala = sala;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrenotazioniDTO)) {
            return false;
        }

        PrenotazioniDTO prenotazioniDTO = (PrenotazioniDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, prenotazioniDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PrenotazioniDTO{id='" + id + "', data='" + data + "', tipoEvento='" + tipoEvento + "'}";
    }

    public void setSalaId(UUID id) {}

    public void setUtenteId(UUID id) {}
}
