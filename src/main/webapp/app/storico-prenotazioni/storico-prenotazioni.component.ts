import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { IPrenotazioni } from 'app/entities/prenotazioni/prenotazioni.model';
import { PrenotazioniService } from 'app/entities/prenotazioni/service/prenotazioni.service';
import dayjs from 'dayjs/esm';

@Component({
  standalone: true,
  selector: 'jhi-storico-prenotazioni',
  templateUrl: './storico-prenotazioni.component.html',
  styleUrl: './storico-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule],
})
export class StoricoPrenotazioniComponent implements OnInit {
  prenotazioniMostrate = signal<IPrenotazioni[]>([]);
  tutteLePrenotazioni: IPrenotazioni[] = [];
  isLoading = signal(false);
  filtroAttivo = signal('tutte');

  private prenotazioniService = inject(PrenotazioniService);

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);
    this.prenotazioniService.getStorico().subscribe({
      next: res => {
        const dati = res.body ?? [];
        this.tutteLePrenotazioni = dati.sort((a, b) => dayjs(b.data).valueOf() - dayjs(a.data).valueOf());
        this.prenotazioniMostrate.set(this.tutteLePrenotazioni);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  applicaFiltro(tipo: string): void {
    this.filtroAttivo.set(tipo);
    const oggi = dayjs();

    if (tipo === 'tutte') {
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni);
    } else if (tipo === '30giorni') {
      const limite = oggi.subtract(30, 'days');
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni.filter(p => p.data && dayjs(p.data).isAfter(limite)));
    } else if (tipo === '2025') {
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni.filter(p => p.data && dayjs(p.data).year() === 2025));
    }
  }

  convertToDate(d: any): Date | null {
    return d ? dayjs(d).toDate() : null;
  }
}
