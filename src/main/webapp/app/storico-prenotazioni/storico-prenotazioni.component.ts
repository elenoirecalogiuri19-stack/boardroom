import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import SharedModule from 'app/shared/shared.module';
import { IPrenotazioni } from 'app/entities/prenotazioni/prenotazioni.model';
import { PrenotazioniService } from 'app/entities/prenotazioni/service/prenotazioni.service';
import dayjs from 'dayjs/esm';

type FiltroModalita = 'tutte' | '30giorni' | 'annoCorrente' | 'singola' | 'range';

@Component({
  standalone: true,
  selector: 'jhi-storico-prenotazioni',
  templateUrl: './storico-prenotazioni.component.html',
  styleUrl: './storico-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule, FormsModule],
})
export class StoricoPrenotazioniComponent implements OnInit {
  prenotazioniMostrate = signal<IPrenotazioni[]>([]);
  tutteLePrenotazioni: IPrenotazioni[] = [];
  filtroAttivo = signal<FiltroModalita>('tutte');
  isLoading = signal<boolean>(false);
  readonly annoCorrente = dayjs().year();

  // Campi filtro data
  filtroDataSingola = '';
  filtroDataDal = '';
  filtroDataAl = '';

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
        this.applicaFiltroCorrente();
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  /** Seleziona la modalità filtro rapido e resetta i campi data */
  applicaFiltro(tipo: FiltroModalita): void {
    this.filtroAttivo.set(tipo);
    this.filtroDataSingola = '';
    this.filtroDataDal = '';
    this.filtroDataAl = '';
    this.applicaFiltroCorrente();
  }

  /** Applica i filtri data personalizzati (singola o range) */
  cercaPerData(): void {
    this.applicaFiltroCorrente();
  }

  /** Reset completo — torna a "tutte" */
  resetFiltri(): void {
    this.filtroAttivo.set('tutte');
    this.filtroDataSingola = '';
    this.filtroDataDal = '';
    this.filtroDataAl = '';
    this.prenotazioniMostrate.set(this.tutteLePrenotazioni);
  }

  private applicaFiltroCorrente(): void {
    const oggi = dayjs();
    const tipo = this.filtroAttivo();

    if (tipo === 'tutte') {
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni);
    } else if (tipo === '30giorni') {
      const limite = oggi.subtract(30, 'days');
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni.filter(p => p.data && dayjs(p.data).isAfter(limite)));
    } else if (tipo === 'annoCorrente') {
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni.filter(p => p.data && dayjs(p.data).year() === this.annoCorrente));
    } else if (tipo === 'singola' && this.filtroDataSingola) {
      const target = dayjs(this.filtroDataSingola);
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni.filter(p => p.data && dayjs(p.data).isSame(target, 'day')));
    } else if (tipo === 'range') {
      let risultati = [...this.tutteLePrenotazioni];
      if (this.filtroDataDal) {
        const dal = dayjs(this.filtroDataDal).startOf('day');
        risultati = risultati.filter(p => p.data && (dayjs(p.data).isAfter(dal) || dayjs(p.data).isSame(dal, 'day')));
      }
      if (this.filtroDataAl) {
        const al = dayjs(this.filtroDataAl).endOf('day');
        risultati = risultati.filter(p => p.data && (dayjs(p.data).isBefore(al) || dayjs(p.data).isSame(al, 'day')));
      }
      this.prenotazioniMostrate.set(risultati);
    }
  }

  convertToDate(d: any): Date | null {
    return d ? dayjs(d).toDate() : null;
  }
}
