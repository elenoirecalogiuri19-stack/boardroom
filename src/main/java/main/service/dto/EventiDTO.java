package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
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

    private PrenotazioniDTO prenotazione;

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

    public PrenotazioniDTO getPrenotazione() {
        return prenotazione;
    }

    public void setPrenotazione(PrenotazioniDTO prenotazione) {
        this.prenotazione = prenotazione;
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
            ", prenotazione=" + getPrenotazione() +
            "}";
    }
}
