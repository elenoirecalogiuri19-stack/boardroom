package main.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import main.domain.enumeration.TipoEvento;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity class for Prenotazioni
 */
@Entity
@Table(name = "prenotazioni")
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

    @Column(name = "tipo_evento")
    private TipoEvento tipoEvento;

    @Column(name = "prezzo")
    private BigDecimal prezzo;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "prenotazione")
    @JsonIgnoreProperties(value = { "prenotazione" }, allowSetters = true)
    private Set<Eventi> eventis = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private StatiPrenotazione stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private Utenti utente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private Sale sala;

    // --- GETTER E SETTER ---
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

    public Set<Eventi> getEventis() {
        return eventis;
    }

    public void setEventis(Set<Eventi> eventis) {
        this.eventis = eventis;
    }

    public StatiPrenotazione getStato() {
        return stato;
    }

    public void setStato(StatiPrenotazione stato) {
        this.stato = stato;
    }

    public Utenti getUtente() {
        return utente;
    }

    public void setUtente(Utenti utente) {
        this.utente = utente;
    }

    public Sale getSala() {
        return sala;
    }

    public void setSala(Sale sala) {
        this.sala = sala;
    }

    // --- METODI FLUENTI ---
    public Prenotazioni id(UUID id) {
        this.setId(id);
        return this;
    }

    public Prenotazioni data(LocalDate data) {
        this.setData(data);
        return this;
    }

    public Prenotazioni oraInizio(LocalTime oraInizio) {
        this.setOraInizio(oraInizio);
        return this;
    }

    public Prenotazioni oraFine(LocalTime oraFine) {
        this.setOraFine(oraFine);
        return this;
    }

    public Prenotazioni tipoEvento(TipoEvento tipoEvento) {
        this.setTipoEvento(tipoEvento);
        return this;
    }

    public Prenotazioni prezzo(BigDecimal prezzo) {
        this.setPrezzo(prezzo);
        return this;
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

    public Prenotazioni stato(StatiPrenotazione stato) {
        this.setStato(stato);
        return this;
    }

    public Prenotazioni utente(Utenti utente) {
        this.setUtente(utente);
        return this;
    }

    public Prenotazioni sala(Sale sala) {
        this.setSala(sala);
        return this;
    }

    // --- equals & hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prenotazioni)) return false;
        return id != null && id.equals(((Prenotazioni) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
