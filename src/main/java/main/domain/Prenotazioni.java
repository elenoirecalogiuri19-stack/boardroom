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

    @Transient
    private String tipoEvento;

    @Transient
    private Double prezzo;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "prenotazione")
    @JsonIgnoreProperties(value = { "prenotazione" }, allowSetters = true)
    private Set<Eventi> eventis = new HashSet<>();

    // AGGIUNTO @Transient per evitare l'errore 500 sulla colonna mancante
    @Transient
    private StatiPrenotazione stato;

    @Transient
    private Utenti utente;

    @Transient
    private Sale sala;

    // --- GETTER E SETTER ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getOraInizio() { return oraInizio; }
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }

    public LocalTime getOraFine() { return oraFine; }
    public void setOraFine(LocalTime oraFine) { this.oraFine = oraFine; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public Double getPrezzo() { return prezzo; }
    public void setPrezzo(Double prezzo) { this.prezzo = prezzo; }

    public StatiPrenotazione getStato() { return stato; }
    public void setStato(StatiPrenotazione stato) { this.stato = stato; }

    public Utenti getUtente() { return utente; }
    public void setUtente(Utenti utente) { this.utente = utente; }

    public Sale getSala() { return sala; }
    public void setSala(Sale sala) { this.sala = sala; }

    public Set<Eventi> getEventis() { return eventis; }
    public void setEventis(Set<Eventi> eventis) { this.eventis = eventis; }

    public Prenotazioni data(LocalDate data) { this.setData(data); return this; }
    public Prenotazioni oraInizio(LocalTime oraInizio) { this.setOraInizio(oraInizio); return this; }
    public Prenotazioni oraFine(LocalTime oraFine) { this.setOraFine(oraFine); return this; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prenotazioni)) return false;
        return id != null && id.equals(((Prenotazioni) o).id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Prenotazioni{id=" + id + ", data='" + data + "'}";
    }
}
