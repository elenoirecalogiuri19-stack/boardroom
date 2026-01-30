import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RicercaService } from 'app/services/ricerca.service';
import { SaleApiService, ISalaDTO } from 'app/services/sale-api.service';

export interface Sala {
  id: string;
  nome: string;
  capienza: number;
}

@Component({
  selector: 'jhi-risultati-sala',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, FontAwesomeModule],
  templateUrl: './risultati-sala.component.html',
  styleUrl: './risultati-sala.component.scss',
})
export class RisultatiSalaComponent implements OnInit {
  dataRicerca = '';
  oraRicerca = '';
  capienzaRicerca = 0;

  sale: Sala[] = [];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private ricercaService: RicercaService,
    private saleApiService: SaleApiService,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.dataRicerca = (params['data'] as string | undefined) ?? '';
      this.oraRicerca = (params['ora'] as string | undefined) ?? '';
      this.capienzaRicerca = params['persone'] ? Number(params['persone']) : 0;

      this.caricaSaleDisponibili();
    });
  }

  // ✔️ Metodi pubblici prima dei private
  tornaIndietro(): void {
    this.router.navigate(['/prenota-sala']);
  }

  selezionaSala(sala: Sala): void {
    console.warn('Hai scelto per il tuo evento:', sala.nome);
    this.ricercaService.resetRicerca();
  }

  // ✔️ Metodi privati dopo
  private caricaSaleDisponibili(): void {
    if (!this.dataRicerca || !this.oraRicerca) {
      return;
    }

    const parts = this.oraRicerca.split('-');
    if (parts.length !== 2) {
      console.error('Formato ora non valido:', this.oraRicerca);
      return;
    }

    const inizioRaw = parts[0].trim();
    const fineRaw = parts[1].trim();

    const inizio = this.normalizzaOra(inizioRaw);
    const fine = this.normalizzaOra(fineRaw);

    this.saleApiService.getSaleDisponibili(this.dataRicerca, inizio, fine, this.capienzaRicerca).subscribe({
      next: (saleDto: ISalaDTO[]) => {
        this.sale = saleDto.map(s => ({
          id: s.id,
          nome: s.nome,
          capienza: s.capienza,
        }));
      },
      error(err) {
        console.error('Errore nel caricamento delle sale disponibili', err);
      },
    });
  }

  private normalizzaOra(ora: string): string {
    const [h, m] = ora.split(':');

    const hh = h.padStart(2, '0');
    const mm = (m as string | undefined) ?? '00';

    return `${hh}:${mm}`;
  }
}
