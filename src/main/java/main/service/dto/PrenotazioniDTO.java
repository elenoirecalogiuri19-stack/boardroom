package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

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

    // --- METODI BRIDGE CON UUID ---
    public UUID getUtenteId() {
        return (utente != null) ? utente.getId() : null;
    }

    public void setUtenteId(UUID id) {
        if (this.utente == null) {
            this.utente = new UtentiDTO();
        }
        this.utente.setId(id);
    }

    public UUID getSalaId() {
        return (sala != null) ? sala.getId() : null;
    }

    public void setSalaId(UUID id) {
        if (this.sala == null) {
            this.sala = new SaleDTO();
        }
        this.sala.setId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrenotazioniDTO)) return false;
        return id != null && id.equals(((PrenotazioniDTO) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PrenotazioniDTO{id='" + id + "'}";
    }
}
