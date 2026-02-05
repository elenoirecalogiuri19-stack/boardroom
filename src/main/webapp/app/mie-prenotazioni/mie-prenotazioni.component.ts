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
  expandedRows = signal<Set<string>>(new Set());

  private prenotazioniApi = inject(PrenotazioniApiService);

  ngOnInit(): void {
    this.caricaLeMiePrenotazioni();
  }

  caricaLeMiePrenotazioni(): void {
    this.isLoading = true;
    this.prenotazioniApi.getMiePrenotazioni().subscribe({
      next: (res: PrenotazioneDTO[]) => {
        this.prenotazioni.set(res);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
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

  eliminaEvento(id: string | undefined): void {
    if (!id) return;
    if (confirm('Sei sicuro di voler eliminare questa prenotazione?')) {
      this.prenotazioniApi.cancellaPrenotazione(id).subscribe({
        next: () => this.caricaLeMiePrenotazioni(),
      });
    }
  }
}
