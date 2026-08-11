import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { PrenotazioneDTO, PrenotazioniApiService } from '../services/prenotazioni-api.service';
import { NotificationService } from 'app/shared/notification/notification.service';

@Component({
  standalone: true,
  selector: 'jhi-mie-prenotazioni',
  templateUrl: './mie-prenotazioni.component.html',
  styleUrl: './mie-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule],
})
export class MiePrenotazioniComponent implements OnInit {
  prenotazioni = signal<PrenotazioneDTO[]>([]);
  expandedRows = signal<Set<string>>(new Set());
  showDeleteModal = signal(false);
  prenotazioneIdDaEliminare = signal<string | undefined>(undefined);

  private prenotazioniApi = inject(PrenotazioniApiService);
  private notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.caricaLeMiePrenotazioni();
  }

  caricaLeMiePrenotazioni(): void {
    this.prenotazioniApi.getMiePrenotazioni().subscribe({
      next: (res: PrenotazioneDTO[]) => {
        this.prenotazioni.set(res);
      },
      error: () => {
        this.notificationService.show('Errore nel caricamento delle prenotazioni', 'error');
      },
    });
  }

  toggleDetails(id: string | undefined): void {
    if (!id) return;
    const newSet = new Set(this.expandedRows());
    if (newSet.has(id)) {
      newSet.delete(id);
    } else {
      newSet.add(id);
    }
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
      error: () => {
        this.notificationService.show("Errore durante l'eliminazione", 'error');
      },
    });

    this.prenotazioneIdDaEliminare.set(undefined);
  }
}
