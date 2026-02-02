import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { PrenotazioneDTO, PrenotazioniApiService } from '../services/prenotazioni-api.service';

@Component({
  standalone: true,
  selector: 'jhi-mie-prenotazioni',
  templateUrl: './mie-prenotazioni.component.html',
  styleUrl: './mie-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule],
})
export class MiePrenotazioniComponent implements OnInit {
  prenotazioni = signal<PrenotazioneDTO[]>([]);
  isLoading = false;
  loadingId: string | null = null; // Cambiato da number a string

  private prenotazioniApi = inject(PrenotazioniApiService);

  ngOnInit(): void {
    this.caricaLeMiePrenotazioni();
  }

  caricaLeMiePrenotazioni(): void {
    this.isLoading = true;
    this.prenotazioniApi.getMiePrenotazioni().subscribe({
      next: res => {
        this.prenotazioni.set(res);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  eliminaEvento(id: string | undefined): void {
    if (!id) return;
    if (confirm('Sei sicuro di voler eliminare questa prenotazione?')) {
      this.loadingId = id;
      this.prenotazioniApi.cancellaPrenotazione(id).subscribe({
        next: () => {
          this.loadingId = null;
          this.caricaLeMiePrenotazioni();
        },
        error: () => (this.loadingId = null),
      });
    }
  }
}
