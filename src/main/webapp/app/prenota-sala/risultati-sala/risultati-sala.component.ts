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

  isLoading = false;
  showPrivacyModal = false;
  salaSelezionata: Sala | null = null;

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.dataRicerca = (params['data'] as string | undefined) ?? '';
      this.oraRicerca = (params['ora'] as string | undefined) ?? '';
      this.capienzaRicerca = params['persone'] ? Number(params['persone']) : 0;

      this.caricaSaleDisponibili();

      if (params['apriModal'] === 'true' && params['salaId']) {
        this.gestisciRiaperturaModal(params['salaId']);
      }
    });
  }

  tornaIndietro(): void {
    this.isLoading = true;
    this.router.navigate(['/prenota-sala']).then(() => {
      this.isLoading = false;
    });
  }

  apriSceltaPrivacy(sala: Sala): void {
    this.salaSelezionata = sala;
    this.showPrivacyModal = true;
  }

  confermaEProcedi(isPubblico: boolean): void {
    this.isLoading = true;

    setTimeout(() => {
      this.showPrivacyModal = false;
      this.router
        .navigate(['/prenota-sala/crea-evento'], {
          queryParams: {
            salaId: this.salaSelezionata?.id,
            nomeSala: this.salaSelezionata?.nome,
            data: this.dataRicerca,
            ora: this.oraRicerca,
            pubblico: isPubblico,
          },
        })
        .then(() => {
          this.isLoading = false;
        });
    }, 800);
  }

  private gestisciRiaperturaModal(idSala: string): void {
    const checkSale = setInterval(() => {
      if (!this.isLoading && this.sale.length > 0) {
        const sala = this.sale.find(s => s.id === idSala);
        if (sala) {
          this.apriSceltaPrivacy(sala);
        }
        clearInterval(checkSale);
      }
    }, 100);

    setTimeout(() => clearInterval(checkSale), 3000);
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
        console.error('Errore nel caricamento sale:', err);
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
