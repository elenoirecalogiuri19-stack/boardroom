package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link main.domain.Prenotazioni} entity.
 * US4: Aggiunto tipoEvento e prezzo per la gestione degli eventi privati.
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

    private String tipoEvento; // Per US4 (es. "PRIVATO", "PUBBLICO")

    private Double prezzo;      // Per US4 (opzionale se privato)

    private StatiPrenotazioneDTO stato;

    private UtentiDTO utente;

<<<<<<< Updated upstream
    private SaleDTO sala;
=======
    private UUID salaId;

    private String salaNome;

    // --- Getter e Setter ---
>>>>>>> Stashed changes

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

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    // --- Standard Methods ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrenotazioniDTO)) return false;
        PrenotazioniDTO that = (PrenotazioniDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PrenotazioniDTO{" +
            "id='" + getId() + "'" +
            ", data='" + getData() + "'" +
            ", oraInizio='" + getOraInizio() + "'" +
            ", oraFine='" + getOraFine() + "'" +
            ", tipoEvento='" + getTipoEvento() + "'" +
            ", prezzo=" + getPrezzo() +
            ", stato=" + getStato() +
<<<<<<< Updated upstream
            ", utente=" + getUtente() +
            ", sala=" + getSala() +
=======
            ", utente=" + getUtenteId() +
            ", sala=" + getSalaId() +
            ", salaNome='" + getSalaNome() + "'" +
>>>>>>> Stashed changes
            "}";
    }
}
