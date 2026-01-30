package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import main.domain.enumeration.TipoEvento;

public class PrenotazioniDTO implements Serializable {

    private UUID id;

    @NotNull
    private LocalDate data;

    @NotNull
    private LocalTime oraInizio;

    @NotNull
    private LocalTime oraFine;

    private Integer numPersone;

    private TipoEvento tipoEvento;
    private BigDecimal prezzo;
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

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
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
}
