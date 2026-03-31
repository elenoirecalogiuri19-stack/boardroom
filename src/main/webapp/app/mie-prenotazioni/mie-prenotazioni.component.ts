import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { PrenotazioneDTO, PrenotazioniApiService } from '../services/prenotazioni-api.service';
import { NotificationService } from 'app/shared/notification/notification.service';
import { RicorrenzaFormComponent } from '../entities/ricorrenti/ricorrenza-form.component';
import { RicorrenzaService } from '../entities/ricorrenti/ricorrenza.service';
import { IRicorrenza } from '../entities/ricorrenti/ricorrenza.model';

@Component({
  standalone: true,
  selector: 'jhi-mie-prenotazioni',
  templateUrl: './mie-prenotazioni.component.html',
  styleUrl: './mie-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule, RicorrenzaFormComponent],
})
export class MiePrenotazioniComponent implements OnInit {
  prenotazioni = signal<PrenotazioneDTO[]>([]);
  expandedRows = signal<Set<string>>(new Set());
  showDeleteModal = signal(false);
  prenotazioneIdDaEliminare = signal<string | undefined>(undefined);

  // ── Ricorrenze ────────────────────────────────────────
  mostraFormRicorrenza = signal(false);
  ricorrenze = signal<IRicorrenza[]>([]);
  showCancellaSerieModal = signal(false);
  ricorrenzaIdDaCancellare = signal<string | undefined>(undefined);
  cancellaSoloFuture = signal(true);
  // ─────────────────────────────────────────────────────

  private prenotazioniApi = inject(PrenotazioniApiService);
  private notificationService = inject(NotificationService);
  private ricorrenzaService = inject(RicorrenzaService);

  ngOnInit(): void {
    this.caricaLeMiePrenotazioni();
    this.caricaRicorrenze();
  }

  caricaLeMiePrenotazioni(): void {
    this.prenotazioniApi.getMiePrenotazioni().subscribe({
      next: (res: PrenotazioneDTO[]) => this.prenotazioni.set(res),
      error: () => this.notificationService.show('Errore nel caricamento delle prenotazioni', 'error'),
    });
  }

  // ── Ricorrenze ────────────────────────────────────────

  caricaRicorrenze(): void {
    this.ricorrenzaService.getMie().subscribe({
      next: list => this.ricorrenze.set(list),
      error: () => {},
    });
  }

  onRicorrenzaCreata(res: IRicorrenza): void {
    this.mostraFormRicorrenza.set(false);
    this.caricaLeMiePrenotazioni();
    this.caricaRicorrenze();
  }

  onRicorrenzaAnnullata(): void {
    this.mostraFormRicorrenza.set(false);
  }

  chiediCancellazioneSerie(id: string | undefined, soloFuture: boolean): void {
    if (!id) return;
    this.ricorrenzaIdDaCancellare.set(id);
    this.cancellaSoloFuture.set(soloFuture);
    this.showCancellaSerieModal.set(true);
  }

  annullaCancellazioneSerie(): void {
    this.showCancellaSerieModal.set(false);
    this.ricorrenzaIdDaCancellare.set(undefined);
  }

  confermaCancellazioneSerie(): void {
    const id = this.ricorrenzaIdDaCancellare();
    if (!id) return;
    this.showCancellaSerieModal.set(false);

    const op$ = this.cancellaSoloFuture() ? this.ricorrenzaService.cancellaFuture(id) : this.ricorrenzaService.cancellaTutta(id);

    op$.subscribe({
      next: () => {
        this.notificationService.show('Serie cancellata con successo', 'success');
        this.caricaLeMiePrenotazioni();
        this.caricaRicorrenze();
      },
      error: () => this.notificationService.show('Errore nella cancellazione della serie', 'error'),
    });

    this.ricorrenzaIdDaCancellare.set(undefined);
  }

  getFrequenzaLabel(f: string): string {
    const map: Record<string, string> = {
      WEEKLY: 'Settimanale',
      BIWEEKLY: 'Bisettimanale',
      MONTHLY: 'Mensile',
    };
    return map[f] ?? f;
  }

  getGiorniLabel(giorni: string[] | undefined): string {
    if (!giorni || giorni.length === 0) return '';
    const nomi: Record<string, string> = {
      MONDAY: 'Lun',
      TUESDAY: 'Mar',
      WEDNESDAY: 'Mer',
      THURSDAY: 'Gio',
      FRIDAY: 'Ven',
      SATURDAY: 'Sab',
      SUNDAY: 'Dom',
    };
    return giorni.map(g => nomi[g] ?? g).join(' · ');
  }

  // ── Prenotazioni singole ──────────────────────────────

  toggleDetails(id: string | undefined): void {
    if (!id) return;
    const newSet = new Set(this.expandedRows());
    newSet.has(id) ? newSet.delete(id) : newSet.add(id);
    this.expandedRows.set(newSet);
  }

  isExpanded(id: string | undefined): boolean {
    return !!id && this.expandedRows().has(id);
  }

  chiediConfermaEliminazione(id: string | undefined): void {
    if (!id) return;
    this.prenotazioneIdDaEliminare.set(id);
    this.showDeleteModal.set(true);
  }

  annullaEliminazione(): void {
    this.showDeleteModal.set(false);
    this.prenotazioneIdDaEliminare.set(undefined);
  }

  confermaEliminazione(): void {
    const id = this.prenotazioneIdDaEliminare();
    if (!id) return;
    this.showDeleteModal.set(false);

    this.prenotazioniApi.cancellaPrenotazione(id).subscribe({
      next: () => {
        this.notificationService.show('Prenotazione eliminata con successo', 'success');
        this.caricaLeMiePrenotazioni();
      },
      error: () => this.notificationService.show("Errore durante l'eliminazione", 'error'),
    });

    this.prenotazioneIdDaEliminare.set(undefined);
  }
}
