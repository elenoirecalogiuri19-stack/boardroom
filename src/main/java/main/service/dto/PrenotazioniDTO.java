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

    private StatiPrenotazioneDTO stato;

    private UUID utenteId;

    private UUID salaId;

    private String salaNome;

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

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

    public StatiPrenotazioneDTO getStato() {
        return stato;
    }

    public void setStato(StatiPrenotazioneDTO stato) {
        this.stato = stato;
    }

    public UUID getUtenteId() {
        return utenteId;
    }

    public void setUtenteId(UUID utenteId) {
        this.utenteId = utenteId;
    }

    public UUID getSalaId() {
        return salaId;
    }

    public void setSalaId(UUID salaId) {
        this.salaId = salaId;
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
        return "PrenotazioniDTO{" +
            "id='" + getId() + "'" +
            ", data='" + getData() + "'" +
            ", oraInizio='" + getOraInizio() + "'" +
            ", oraFine='" + getOraFine() + "'" +
            ", stato=" + getStato() +
            ", utente=" + getUtenteId() +
            ", sala=" + getSalaId() +
            "}";
    }
}
