package main.service.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.domain.enumeration.Frequenza;

/**
 * DTO per la creazione e lettura di una regola di ricorrenza.
 *
 * Esempio payload creazione:
 * {
 *   "salaId": "uuid-sala",
 *   "frequenza": "WEEKLY",
 *   "giorniSettimana": ["MONDAY", "WEDNESDAY"],
 *   "dataInizio": "2026-04-07",
 *   "dataFine": "2026-06-30",
 *   "oraInizio": "09:00",
 *   "oraFine": "10:00",
 *   "numPersone": 5
 * }
 */
public class RicorrenzaDTO implements Serializable {

    private UUID id;

    @NotNull
    private UUID salaId;

    private String salaNome;

    @NotNull
    private Frequenza frequenza;

    /**
     * Giorni selezionati per WEEKLY/BIWEEKLY.
     * Valori: "MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"
     */
    private List<String> giorniSettimana;

    @NotNull
    private LocalDate dataInizio;

    /** Data fine serie (obbligatoria se numOccorrenze è null) */
    private LocalDate dataFine;

    /** Numero occorrenze (obbligatorio se dataFine è null) */
    private Integer numOccorrenze;

    @NotNull
    private LocalTime oraInizio;

    @NotNull
    private LocalTime oraFine;

    private Integer numPersone;

    /** Numero di istanze effettivamente generate (popolato in risposta) */
    private int istanzeCreate;

    /** Istanze con conflitti non create (popolato in risposta) */
    private int istanzeConflitto;

    /** Date skippate per conflitto (popolato in risposta, max 10 per leggibilità) */
    private List<LocalDate> dateConflitto;

    // ── Getters & Setters ─────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSalaId() {
        return salaId;
    }

    public void setSalaId(UUID salaId) {
        this.salaId = salaId;
    }

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    public Frequenza getFrequenza() {
        return frequenza;
    }

    public void setFrequenza(Frequenza frequenza) {
        this.frequenza = frequenza;
    }

    public List<String> getGiorniSettimana() {
        return giorniSettimana;
    }

    public void setGiorniSettimana(List<String> giorniSettimana) {
        this.giorniSettimana = giorniSettimana;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDataFine(LocalDate dataFine) {
        this.dataFine = dataFine;
    }

    public Integer getNumOccorrenze() {
        return numOccorrenze;
    }

    public void setNumOccorrenze(Integer numOccorrenze) {
        this.numOccorrenze = numOccorrenze;
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

    public Integer getNumPersone() {
        return numPersone;
    }

    public void setNumPersone(Integer numPersone) {
        this.numPersone = numPersone;
    }

    public int getIstanzeCreate() {
        return istanzeCreate;
    }

    public void setIstanzeCreate(int istanzeCreate) {
        this.istanzeCreate = istanzeCreate;
    }

    public int getIstanzeConflitto() {
        return istanzeConflitto;
    }

    public void setIstanzeConflitto(int istanzeConflitto) {
        this.istanzeConflitto = istanzeConflitto;
    }

    public List<LocalDate> getDateConflitto() {
        return dateConflitto;
    }

    public void setDateConflitto(List<LocalDate> dateConflitto) {
        this.dateConflitto = dateConflitto;
    }
}
