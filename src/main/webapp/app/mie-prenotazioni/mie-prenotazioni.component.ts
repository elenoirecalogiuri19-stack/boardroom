import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { IEventi } from 'app/entities/eventi/eventi.model';
import { EventiService } from 'app/entities/eventi/service/eventi.service';

@Component({
  standalone: true,
  selector: 'jhi-mie-prenotazioni',
  templateUrl: './mie-prenotazioni.component.html',
  styleUrl: './mie-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule],
})
export class MiePrenotazioniComponent implements OnInit {
  eventi = signal<IEventi[]>([]);
  isLoading = false;
  loadingId: string | null = null; // Cambiato da number a string

  private eventiService = inject(EventiService);

  ngOnInit(): void {
    this.caricaLeMiePrenotazioni();
  }

  caricaLeMiePrenotazioni(): void {
    this.isLoading = true;
    this.eventiService.query().subscribe({
      next: res => {
        this.eventi.set(res.body ?? []);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  eliminaEvento(id: string | undefined): void {
    if (id !== undefined) {
      if (confirm('Sei sicuro di voler eliminare questa prenotazione?')) {
        this.loadingId = id;
        this.eventiService.delete(id).subscribe({
          next: () => {
            this.loadingId = null;
            this.caricaLeMiePrenotazioni();
          },
          error: () => (this.loadingId = null),
        });
      }
    }
  }
}
