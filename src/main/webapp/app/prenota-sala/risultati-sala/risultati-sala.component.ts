import { Component, OnInit, inject } from '@angular/core';
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
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private ricercaService = inject(RicercaService);
  private saleApiService = inject(SaleApiService);

  dataRicerca = '';
  oraRicerca = '';
  capienzaRicerca = 0;
  sale: Sala[] = [];

  // Il tuo loader indispensabile!
  isLoading = false;

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.dataRicerca = (params['data'] as string | undefined) ?? '';
      this.oraRicerca = (params['ora'] as string | undefined) ?? '';
      this.capienzaRicerca = params['persone'] ? Number(params['persone']) : 0;

      this.caricaSaleDisponibili();
    });
  }

  tornaIndietro(): void {
    this.isLoading = true; // Attiva loader
    this.router.navigate(['/prenota-sala']).then(() => {
      this.isLoading = false;
    });
  }

  selezionaSala(sala: Sala): void {
    this.isLoading = true; // Attiva loader
    console.warn('Hai scelto per il tuo evento:', sala.nome);

    // Simulo un'operazione o navigazione
    setTimeout(() => {
      this.ricercaService.resetRicerca();
      this.isLoading = false;
      // Qui aggiungerai la navigazione al form di creazione evento
    }, 1000);
  }

  private caricaSaleDisponibili(): void {
    if (!this.dataRicerca || !this.oraRicerca) return;

    const parts = this.oraRicerca.split('-');
    if (parts.length !== 2) return;

    this.isLoading = true;
    const inizio = this.normalizzaOra(parts[0].trim());
    const fine = this.normalizzaOra(parts[1].trim());

    this.saleApiService.getSaleDisponibili(this.dataRicerca, inizio, fine, this.capienzaRicerca).subscribe({
      next: (saleDto: ISalaDTO[]) => {
        this.sale = saleDto.map(s => ({
          id: s.id,
          nome: s.nome,
          capienza: s.capienza,
        }));
        this.isLoading = false;
      },
      error: err => {
        console.error('Errore nel caricamento sale', err);
        this.isLoading = false;
      },
    });
  }

  private normalizzaOra(ora: string): string {
    const [h, m] = ora.split(':');
    const hh = h.padStart(2, '0');
    const mm = m ?? '00';
    return `${hh}:${mm}`;
  }
}
