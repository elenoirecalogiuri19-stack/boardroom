package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;
import main.domain.enumeration.TipoEvento;

/**
 * A DTO for the {@link main.domain.Eventi} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventiDTO implements Serializable {

    private UUID id;

    @NotNull
    private String titolo;

    @NotNull
    private TipoEvento tipo;

    private BigDecimal prezzo;

    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private String salaNome;

    private UUID prenotazioneId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public UUID getPrenotazioneId() {
        return prenotazioneId;
    }

    public void setPrenotazioneId(UUID prenotazioneId) {
        this.prenotazioneId = prenotazioneId;
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

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventiDTO)) {
            return false;
        }

        EventiDTO eventiDTO = (EventiDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, eventiDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventiDTO{" +
            "id='" + getId() + "'" +
            ", titolo='" + getTitolo() + "'" +
            ", tipo='" + getTipo() + "'" +
            ", prezzo=" + getPrezzo() +
            ", prenotazione=" + getPrenotazioneId() +
            "}";
    }
}
