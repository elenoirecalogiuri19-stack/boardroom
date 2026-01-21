package main.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A Prenotazioni.
 */
@Entity
@Table(name = "prenotazioni")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Prenotazioni implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @NotNull
    @Column(name = "data", nullable = false)
    private LocalDate data;

    @NotNull
    @Column(name = "ora_inizio", nullable = false)
    private LocalTime oraInizio;

    @NotNull
    @Column(name = "ora_fine", nullable = false)
    private LocalTime oraFine;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "prenotazione")
    @JsonIgnoreProperties(value = { "prenotazione" }, allowSetters = true)
    private Set<Eventi> eventis = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private StatiPrenotazione stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private Utenti utente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private Sale sala;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Prenotazioni id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getData() {
        return this.data;
    }

    public Prenotazioni data(LocalDate data) {
        this.setData(data);
        return this;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOraInizio() {
        return this.oraInizio;
    }

    public Prenotazioni oraInizio(LocalTime oraInizio) {
        this.setOraInizio(oraInizio);
        return this;
    }

    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    public LocalTime getOraFine() {
        return this.oraFine;
    }

    public Prenotazioni oraFine(LocalTime oraFine) {
        this.setOraFine(oraFine);
        return this;
    }

    public void setOraFine(LocalTime oraFine) {
        this.oraFine = oraFine;
    }

    public Set<Eventi> getEventis() {
        return this.eventis;
    }

    public void setEventis(Set<Eventi> eventis) {
        if (this.eventis != null) {
            this.eventis.forEach(i -> i.setPrenotazione(null));
        }
        if (eventis != null) {
            eventis.forEach(i -> i.setPrenotazione(this));
        }
        this.eventis = eventis;
    }

    public Prenotazioni eventis(Set<Eventi> eventis) {
        this.setEventis(eventis);
        return this;
    }

    public Prenotazioni addEventi(Eventi eventi) {
        this.eventis.add(eventi);
        eventi.setPrenotazione(this);
        return this;
    }

    public Prenotazioni removeEventi(Eventi eventi) {
        this.eventis.remove(eventi);
        eventi.setPrenotazione(null);
        return this;
    }

    public StatiPrenotazione getStato() {
        return this.stato;
    }

    public void setStato(StatiPrenotazione statiPrenotazione) {
        this.stato = statiPrenotazione;
    }

    public Prenotazioni stato(StatiPrenotazione statiPrenotazione) {
        this.setStato(statiPrenotazione);
        return this;
    }

    public Utenti getUtente() {
        return this.utente;
    }

    public void setUtente(Utenti utenti) {
        this.utente = utenti;
    }

    public Prenotazioni utente(Utenti utenti) {
        this.setUtente(utenti);
        return this;
    }

    public Sale getSala() {
        return this.sala;
    }

    public void setSala(Sale sale) {
        this.sala = sale;
    }

    public Prenotazioni sala(Sale sale) {
        this.setSala(sale);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Prenotazioni)) {
            return false;
        }
        return getId() != null && getId().equals(((Prenotazioni) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Prenotazioni{" +
            "id=" + getId() +
            ", data='" + getData() + "'" +
            ", oraInizio='" + getOraInizio() + "'" +
            ", oraFine='" + getOraFine() + "'" +
            "}";
    }
}
