import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';

import { IPrenotazioni } from 'app/entities/prenotazioni/prenotazioni.model';
import { PrenotazioniService } from 'app/entities/prenotazioni/service/prenotazioni.service';

import dayjs from 'dayjs/esm';
import { PrenotazioneDTO } from '../services/prenotazioni-api.service';

@Component({
  standalone: true,
  selector: 'jhi-storico-prenotazioni',
  templateUrl: './storico-prenotazioni.component.html',
  styleUrl: './storico-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule],
})
export class StoricoPrenotazioniComponent implements OnInit {
  prenotazioniPassate = signal<IPrenotazioni[]>([]);
  tutteLePrenotazioni: IPrenotazioni[] = []; // Backup per i filtri
  isLoading = signal(false);
  filtroAttivo = signal('tutte');

  private prenotazioniService = inject(PrenotazioniService);

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading.set(true);

    this.prenotazioniService.getStorico().subscribe({
      next: res => {
        this.isLoading.set(false);

        const body = res.body ?? [];

        const concluse = body.sort((a, b) => dayjs(b.data).diff(dayjs(a.data)) || dayjs(b.oraInizio).diff(dayjs(a.oraInizio)));

        this.tutteLePrenotazioni = concluse;
        this.prenotazioniPassate.set(concluse);
      },
      error: () => this.isLoading.set(false),
    });
  }

  applicaFiltro(tipo: string): void {
    this.filtroAttivo.set(tipo);
    const ora = dayjs();

    if (tipo === 'tutte') {
      this.prenotazioniPassate.set(this.tutteLePrenotazioni);
    } else if (tipo === '30giorni') {
      const trentaGiorniFa = ora.subtract(30, 'days');
      this.prenotazioniPassate.set(this.tutteLePrenotazioni.filter((p: any) => dayjs(p.oraInizio).isAfter(trentaGiorniFa)));
    } else if (tipo === '2025') {
      this.prenotazioniPassate.set(this.tutteLePrenotazioni.filter((p: any) => dayjs(p.oraInizio).year() === 2025));
    }
  }
}
