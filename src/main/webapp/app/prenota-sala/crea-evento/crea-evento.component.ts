import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NotificationService } from 'app/shared/notification/notification.service';
import { EventiApiService } from 'app/services/eventi-api.service';
import { RicorrenzaService } from 'app/entities/ricorrenti/ricorrenza.service';
import { IRicorrenza, GiornoSettimana, GIORNI_OPTIONS, FREQUENZA_OPTIONS, Frequenza } from 'app/entities/ricorrenti/ricorrenza.model';
import { ISalaDTO } from 'app/services/sale-api.service';

const GIORNI_JS_TO_API: Record<number, GiornoSettimana> = {
  0: 'SUNDAY',
  1: 'MONDAY',
  2: 'TUESDAY',
  3: 'WEDNESDAY',
  4: 'THURSDAY',
  5: 'FRIDAY',
  6: 'SATURDAY',
};

// ── Tipi per gestione conflitti ───────────────────────────────

/** Una singola opzione selezionabile per risolvere un conflitto */
export interface OpzioneConflitto {
  tipo: 'cambio_giorno' | 'cambio_orario' | 'cambio_sala';
  data: string;
  oraInizio: string;
  oraFine: string;
  salaId: string;
  salaNome: string;
  etichetta: string; // testo breve per il bottone
}

/** Stato di un conflitto su una data specifica */
export interface ConflittoData {
  data: string; // data originale in conflitto
  opzioniGiorno: string[]; // date alternative disponibili
  opzioniOrario: { oraInizio: string; oraFine: string }[]; // fasce libere
  opzioniSala: ISalaDTO[]; // sale disponibili in quello slot
  // Selezioni correnti dell'utente (null = non ancora scelto)
  giornoScelto: string | null;
  oraScelta: { oraInizio: string; oraFine: string } | null;
  salaScelta: ISalaDTO | null;
  // Quale "tipo" di risoluzione ha scelto
  tipoRisoluzione: 'cambio_giorno' | 'cambio_orario' | 'cambio_sala' | null;
  caricamento: boolean;
}

@Component({
  selector: 'jhi-crea-evento',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crea-evento.component.html',
  styleUrl: './crea-evento.component.scss',
})
export class CreaEventoComponent implements OnInit {
  isLoading = false;
  isPubblico = false;
  prenotazioneId = '';
  capienzaRicerca = 0;

  evento = {
    titolo: '',
    descrizione: '',
    prezzo: null as number | null,
    salaId: '',
    nomeSala: '',
    data: '',
    ora: '',
    oraInizio: '',
    oraFine: '',
  };

  // ── Ricorrenza ───────────────────────────────────────
  mostraRicorrenza = false;
  readonly frequenzaOptions = FREQUENZA_OPTIONS;
  readonly giorniOptions = GIORNI_OPTIONS;

  ricorrenza = {
    frequenza: 'WEEKLY' as Frequenza,
    giorniSelezionati: [] as GiornoSettimana[],
    usaDataFine: true,
    dataFine: '',
    numOccorrenze: null as number | null,
  };

  // ── Fasi ─────────────────────────────────────────────
  fase: 'form' | 'verifica' | 'riepilogo' = 'form';
  isVerificando = false;
  conflitti = signal<ConflittoData[]>([]);
  dateLibere: string[] = [];

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private notificationService = inject(NotificationService);
  private eventiApi = inject(EventiApiService);
  private ricorrenzaService = inject(RicorrenzaService);

  readonly oggi = new Date().toISOString().split('T')[0];

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.isPubblico = params['pubblico'] === 'true';
      this.evento.salaId = params['salaId'];
      this.evento.nomeSala = params['nomeSala'];
      this.evento.data = params['data'];
      this.evento.ora = params['ora'];
      this.prenotazioneId = params['prenotazioneId'];
      this.capienzaRicerca = params['numPersone'] ? Number(params['numPersone']) : 0;

      if (this.evento.ora) {
        const parts = this.evento.ora.split('-').map((s: string) => s.trim());
        this.evento.oraInizio = this.normalizzaOra(parts[0] ?? '');
        this.evento.oraFine = this.normalizzaOra(parts[1] ?? '');
      }

      if (this.evento.data) {
        const d = new Date(this.evento.data + 'T00:00:00');
        const g = GIORNI_JS_TO_API[d.getDay()];
        if (g) this.ricorrenza.giorniSelezionati = [g];
      }

      if (!this.evento.salaId) this.router.navigate(['/prenota-sala']);
    });
  }

  // ── Ricorrenza helpers ───────────────────────────────

  get mostraGiorni(): boolean {
    return this.ricorrenza.frequenza !== 'MONTHLY';
  }

  get stimaOccorrenze(): string {
    if (!this.evento.data) return '';
    if (this.ricorrenza.usaDataFine && this.ricorrenza.dataFine) {
      const giorni = Math.ceil(
        (new Date(this.ricorrenza.dataFine).getTime() - new Date(this.evento.data).getTime()) / (1000 * 60 * 60 * 24),
      );
      let s = 0;
      if (this.ricorrenza.frequenza === 'WEEKLY') s = Math.ceil(giorni / 7) * this.ricorrenza.giorniSelezionati.length;
      if (this.ricorrenza.frequenza === 'BIWEEKLY') s = Math.ceil(giorni / 14) * this.ricorrenza.giorniSelezionati.length;
      if (this.ricorrenza.frequenza === 'MONTHLY') s = Math.ceil(giorni / 30);
      return `~${s} prenotazioni`;
    }
    return this.ricorrenza.numOccorrenze ? `${this.ricorrenza.numOccorrenze} prenotazioni` : '';
  }

  toggleGiorno(g: GiornoSettimana): void {
    const idx = this.ricorrenza.giorniSelezionati.indexOf(g);
    this.ricorrenza.giorniSelezionati =
      idx >= 0 ? this.ricorrenza.giorniSelezionati.filter(x => x !== g) : [...this.ricorrenza.giorniSelezionati, g];
  }

  isGiornoSelezionato(g: GiornoSettimana): boolean {
    return this.ricorrenza.giorniSelezionati.includes(g);
  }

  onFrequenzaChange(): void {
    if (this.ricorrenza.frequenza === 'MONTHLY') this.ricorrenza.giorniSelezionati = [];
  }

  // ── Verifica conflitti ───────────────────────────────

  async verificaEProcedi(): Promise<void> {
    if (!this.validaRicorrenza()) return;
    this.isVerificando = true;
    this.fase = 'verifica';

    const dateCandidate = this.calcolaDate();
    const risultati: ConflittoData[] = [];
    this.dateLibere = [];

    for (const data of dateCandidate) {
      const libera = await this.verificaDisponibilita(data, this.evento.oraInizio, this.evento.oraFine, this.evento.salaId);
      if (libera) {
        this.dateLibere.push(data);
      } else {
        const conflitto = await this.caricaOpzioniConflitto(data);
        risultati.push(conflitto);
      }
    }

    this.conflitti.set(risultati);
    this.isVerificando = false;

    if (risultati.length === 0) {
      await this.creaRicorrenzaFinale();
    } else {
      this.fase = 'riepilogo';
    }
  }

  /**
   * Carica in parallelo tutte le opzioni disponibili per un conflitto:
   * - Fino a 5 date alternative nei prossimi 14 giorni (stesso orario, stessa sala)
   * - Tutte le fasce orarie libere nella stessa data (stessa sala)
   * - Tutte le sale disponibili nello stesso slot
   */
  private async caricaOpzioniConflitto(data: string): Promise<ConflittoData> {
    const [opzioniGiorno, opzioniOrario, opzioniSala] = await Promise.all([
      this.trovaTuttiGiorniLiberi(data, 5),
      this.trovaTutteFasceLibere(data),
      this.troveTutteleSaleDisponibili(data),
    ]);

    return {
      data,
      opzioniGiorno,
      opzioniOrario,
      opzioniSala,
      giornoScelto: null,
      oraScelta: null,
      salaScelta: null,
      tipoRisoluzione: null,
      caricamento: false,
    };
  }

  /** Trova fino a `max` date libere nei prossimi 14 giorni (stessa sala, stesso orario) */
  private async trovaTuttiGiorniLiberi(dataConflitto: string, max: number): Promise<string[]> {
    const risultati: string[] = [];
    let cursore = new Date(dataConflitto + 'T00:00:00');
    cursore.setDate(cursore.getDate() + 1);
    for (let i = 0; i < 14 && risultati.length < max; i++) {
      const dataStr = cursore.toISOString().split('T')[0];
      const libera = await this.verificaDisponibilita(dataStr, this.evento.oraInizio, this.evento.oraFine, this.evento.salaId);
      if (libera) risultati.push(dataStr);
      cursore.setDate(cursore.getDate() + 1);
    }
    return risultati;
  }

  /** Trova tutte le fasce orarie libere nella stessa data (stessa sala) */
  private async trovaTutteFasceLibere(data: string): Promise<{ oraInizio: string; oraFine: string }[]> {
    const durataOre = this.calcolaDurataOre();
    const risultati: { oraInizio: string; oraFine: string }[] = [];
    for (let h = 8; h <= 19 - durataOre; h++) {
      const oraI = `${String(h).padStart(2, '0')}:00`;
      const oraF = `${String(h + durataOre).padStart(2, '0')}:00`;
      if (oraI === this.evento.oraInizio) continue;
      const libera = await this.verificaDisponibilita(data, oraI, oraF, this.evento.salaId);
      if (libera) risultati.push({ oraInizio: oraI, oraFine: oraF });
    }
    return risultati;
  }

  /** Trova tutte le sale disponibili nello stesso slot (stessa data, stesso orario) */
  private async troveTutteleSaleDisponibili(data: string): Promise<ISalaDTO[]> {
    try {
      const params = new HttpParams()
        .set('data', data)
        .set('inizio', this.evento.oraInizio)
        .set('fine', this.evento.oraFine)
        .set('capienza', this.capienzaRicerca.toString());
      const sale = await this.http.get<ISalaDTO[]>('/api/sales/disponibili', { params }).toPromise();
      // Escludi la sala originale (quella già in conflitto)
      return (sale ?? []).filter(s => s.id !== this.evento.salaId);
    } catch {
      return [];
    }
  }

  // ── Selezione risoluzione ────────────────────────────

  selezionaTipo(c: ConflittoData, tipo: 'cambio_giorno' | 'cambio_orario' | 'cambio_sala'): void {
    // Reset scelte precedenti quando si cambia tipo
    c.tipoRisoluzione = tipo;
    c.giornoScelto = null;
    c.oraScelta = null;
    c.salaScelta = null;
  }

  selezionaGiorno(c: ConflittoData, data: string): void {
    c.giornoScelto = c.giornoScelto === data ? null : data;
  }

  selezionaOrario(c: ConflittoData, slot: { oraInizio: string; oraFine: string }): void {
    c.oraScelta = c.oraScelta?.oraInizio === slot.oraInizio ? null : slot;
  }

  selezionaSala(c: ConflittoData, sala: ISalaDTO): void {
    c.salaScelta = c.salaScelta?.id === sala.id ? null : sala;
  }

  /** Restituisce true se il conflitto ha una scelta completa e valida */
  isConflittoRisolto(c: ConflittoData): boolean {
    if (!c.tipoRisoluzione) return false;
    if (c.tipoRisoluzione === 'cambio_giorno') return !!c.giornoScelto;
    if (c.tipoRisoluzione === 'cambio_orario') return !!c.oraScelta;
    if (c.tipoRisoluzione === 'cambio_sala') return !!c.salaScelta;
    return false;
  }

  saltaConflitto(c: ConflittoData): void {
    c.tipoRisoluzione = null;
    c.giornoScelto = null;
    c.oraScelta = null;
    c.salaScelta = null;
  }

  get conflittiRisoltiCount(): number {
    return this.conflitti().filter(c => this.isConflittoRisolto(c)).length;
  }

  get conflittiSaltatiCount(): number {
    return this.conflitti().filter(c => !this.isConflittoRisolto(c)).length;
  }

  get tuttiGestiti(): boolean {
    return this.conflitti().every(c => this.isConflittoRisolto(c) || this.isConflittoSaltato(c));
  }

  isConflittoSaltato(c: ConflittoData): boolean {
    return !c.tipoRisoluzione && !c.giornoScelto && !c.oraScelta && !c.salaScelta;
  }

  getRiepilogoConflitto(c: ConflittoData): string {
    if (!this.isConflittoRisolto(c)) return '';
    if (c.tipoRisoluzione === 'cambio_giorno' && c.giornoScelto)
      return `${this.formatData(c.giornoScelto)} · ${this.evento.oraInizio}–${this.evento.oraFine} · ${this.evento.nomeSala}`;
    if (c.tipoRisoluzione === 'cambio_orario' && c.oraScelta)
      return `${this.formatData(c.data)} · ${c.oraScelta.oraInizio}–${c.oraScelta.oraFine} · ${this.evento.nomeSala}`;
    if (c.tipoRisoluzione === 'cambio_sala' && c.salaScelta)
      return `${this.formatData(c.data)} · ${this.evento.oraInizio}–${this.evento.oraFine} · ${c.salaScelta.nome}`;
    return '';
  }

  // ── Creazione finale ─────────────────────────────────

  async creaRicorrenzaFinale(): Promise<void> {
    this.isLoading = true;

    // Crea prima l'evento per la prenotazione singola originale
    try {
      await this.eventiApi
        .creaEvento({
          titolo: this.evento.titolo,
          descrizione: this.evento.descrizione,
          prezzo: this.isPubblico ? this.evento.prezzo : null,
          tipo: this.isPubblico ? 'PUBBLICO' : 'PRIVATO',
          prenotazioneId: this.prenotazioneId,
        })
        .toPromise();
    } catch {
      this.isLoading = false;
      this.notificationService.show('Errore nella creazione evento', 'error');
      return;
    }

    if (!this.mostraRicorrenza) {
      this.isLoading = false;
      this.notificationService.show('Evento creato con successo!', 'success');
      this.router.navigate(['/']);
      return;
    }

    // Costruisci il DTO ricorrenza base
    const dto: IRicorrenza = {
      salaId: this.evento.salaId,
      frequenza: this.ricorrenza.frequenza,
      giorniSettimana: this.mostraGiorni ? this.ricorrenza.giorniSelezionati : undefined,
      dataInizio: this.evento.data,
      dataFine: this.ricorrenza.usaDataFine ? this.ricorrenza.dataFine : undefined,
      numOccorrenze: !this.ricorrenza.usaDataFine ? this.ricorrenza.numOccorrenze! : undefined,
      oraInizio: this.evento.oraInizio,
      oraFine: this.evento.oraFine,
      numPersone: this.capienzaRicerca,
    };

    this.ricorrenzaService.crea(dto).subscribe({
      next: async res => {
        // Crea prenotazioni aggiuntive per i conflitti risolti
        const risolti = this.conflitti().filter(c => this.isConflittoRisolto(c));
        for (const c of risolti) {
          await this.creaPrenotazioneConflittoRisolto(c);
        }

        this.isLoading = false;
        const saltati = this.conflitti().filter(c => !this.isConflittoRisolto(c)).length;
        const msg =
          `✅ Serie creata: ${res.istanzeCreate} prenotazioni` +
          (risolti.length > 0 ? ` + ${risolti.length} conflitti risolti` : '') +
          (saltati > 0 ? ` · ${saltati} saltati` : '');
        this.notificationService.show(msg, 'success');
        this.router.navigate(['/mie-prenotazioni']);
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.show('Errore nella creazione della serie', 'error');
      },
    });
  }

  private async creaPrenotazioneConflittoRisolto(c: ConflittoData): Promise<void> {
    let data = c.data;
    let oraInizio = this.evento.oraInizio;
    let oraFine = this.evento.oraFine;
    let salaId = this.evento.salaId;

    if (c.tipoRisoluzione === 'cambio_giorno' && c.giornoScelto) data = c.giornoScelto;
    if (c.tipoRisoluzione === 'cambio_orario' && c.oraScelta) {
      oraInizio = c.oraScelta.oraInizio;
      oraFine = c.oraScelta.oraFine;
    }
    if (c.tipoRisoluzione === 'cambio_sala' && c.salaScelta) salaId = c.salaScelta.id;

    try {
      await this.http
        .post('/api/prenotazionis/prenotta', {
          data,
          oraInizio,
          oraFine,
          salaId,
          numPersone: this.capienzaRicerca,
        })
        .toPromise();
    } catch {
      // Non blocca il flusso — la prenotazione principale è già creata
      console.warn(`Prenotazione conflitto non creata per ${data}`);
    }
  }

  // ── Calcolo date ─────────────────────────────────────

  private calcolaDate(): string[] {
    const date: string[] = [];
    const inizio = new Date(this.evento.data + 'T00:00:00');
    let fine: Date | null = null;
    if (this.ricorrenza.usaDataFine && this.ricorrenza.dataFine) fine = new Date(this.ricorrenza.dataFine + 'T00:00:00');
    const maxOcc = this.ricorrenza.numOccorrenze ?? 365;
    const MAP: Record<string, number> = {
      SUNDAY: 0,
      MONDAY: 1,
      TUESDAY: 2,
      WEDNESDAY: 3,
      THURSDAY: 4,
      FRIDAY: 5,
      SATURDAY: 6,
    };

    if (this.ricorrenza.frequenza === 'MONTHLY') {
      let cur = new Date(inizio);
      while (date.length < maxOcc && (!fine || cur <= fine)) {
        date.push(cur.toISOString().split('T')[0]);
        cur = new Date(cur);
        cur.setMonth(cur.getMonth() + 1);
      }
    } else {
      const step = this.ricorrenza.frequenza === 'BIWEEKLY' ? 14 : 7;
      const giorniTarget = this.ricorrenza.giorniSelezionati.map(g => MAP[g]).sort();
      const lunedi = new Date(inizio);
      const dow = lunedi.getDay() || 7;
      lunedi.setDate(lunedi.getDate() - (dow - 1));
      let settimana = new Date(lunedi);
      while (date.length < maxOcc && (!fine || settimana <= fine)) {
        for (const g of giorniTarget) {
          const d = new Date(settimana);
          d.setDate(settimana.getDate() + ((g + 6) % 7));
          if (d >= inizio && (!fine || d <= fine)) {
            date.push(d.toISOString().split('T')[0]);
            if (date.length >= maxOcc) break;
          }
        }
        settimana.setDate(settimana.getDate() + step);
        if (date.length >= 365) break;
      }
    }
    return date;
  }

  // ── Utils ────────────────────────────────────────────

  private async verificaDisponibilita(data: string, oraInizio: string, oraFine: string, salaId: string): Promise<boolean> {
    try {
      const params = new HttpParams()
        .set('data', data)
        .set('inizio', oraInizio)
        .set('fine', oraFine)
        .set('capienza', this.capienzaRicerca.toString());
      const sale = await this.http.get<ISalaDTO[]>('/api/sales/disponibili', { params }).toPromise();
      return (sale ?? []).some(s => s.id === salaId);
    } catch {
      return false;
    }
  }

  private calcolaDurataOre(): number {
    const [hI, mI] = this.evento.oraInizio.split(':').map(Number);
    const [hF, mF] = this.evento.oraFine.split(':').map(Number);
    return Math.max(1, Math.round((hF * 60 + mF - hI * 60 - mI) / 60));
  }

  private normalizzaOra(ora: string): string {
    if (!ora) return '00:00';
    const [h, m] = ora.split(':');
    return `${h.padStart(2, '0')}:${m ?? '00'}`;
  }

  formatData(data: string): string {
    if (!data) return '';
    return new Date(data + 'T00:00:00').toLocaleDateString('it-IT', { weekday: 'short', day: 'numeric', month: 'short' });
  }

  // ── Validazione e flusso ─────────────────────────────

  conferma(): void {
    if (!this.evento.titolo) {
      this.notificationService.show("Inserire un titolo per l'evento", 'error');
      return;
    }
    if (this.isPubblico && (this.evento.prezzo == null || this.evento.prezzo < 0)) {
      this.notificationService.show('Inserire un prezzo valido', 'error');
      return;
    }
    if (this.mostraRicorrenza) {
      this.verificaEProcedi();
    } else {
      this.isLoading = true;
      this.eventiApi
        .creaEvento({
          titolo: this.evento.titolo,
          descrizione: this.evento.descrizione,
          prezzo: this.isPubblico ? this.evento.prezzo : null,
          tipo: this.isPubblico ? 'PUBBLICO' : 'PRIVATO',
          prenotazioneId: this.prenotazioneId,
        })
        .subscribe({
          next: () => {
            this.isLoading = false;
            this.notificationService.show('Evento creato!', 'success');
            this.router.navigate(['/']);
          },
          error: () => {
            this.isLoading = false;
            this.notificationService.show('Errore creazione evento', 'error');
          },
        });
    }
  }

  private validaRicorrenza(): boolean {
    if (this.mostraGiorni && this.ricorrenza.giorniSelezionati.length === 0) {
      this.notificationService.show('Seleziona almeno un giorno', 'error');
      return false;
    }
    if (this.ricorrenza.usaDataFine && !this.ricorrenza.dataFine) {
      this.notificationService.show('Inserisci la data di fine', 'error');
      return false;
    }
    if (!this.ricorrenza.usaDataFine && (!this.ricorrenza.numOccorrenze || this.ricorrenza.numOccorrenze < 1)) {
      this.notificationService.show('Inserisci il numero di occorrenze', 'error');
      return false;
    }
    return true;
  }

  tornaAlForm(): void {
    this.fase = 'form';
    this.conflitti.set([]);
  }

  annulla(): void {
    this.router.navigate(['/risultati-sala'], {
      queryParams: {
        data: this.evento.data,
        ora: this.evento.ora,
        numPersone: this.capienzaRicerca,
        capienza: this.capienzaRicerca,
        salaId: this.evento.salaId,
        apriModal: 'true',
      },
    });
  }
}
