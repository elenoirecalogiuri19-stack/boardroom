import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RicercaService } from 'app/services/ricerca.service';
import { SaleApiService, ISalaDTO } from 'app/services/sale-api.service';
import { PrenotazioniApiService } from 'app/services/prenotazioni-api.service';
import { AccountService } from 'app/core/auth/account.service';

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

  isLoading = false;
  showPrivacyModal = false;
  salaSelezionata: Sala | null = null;

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private ricercaService = inject(RicercaService);
  private saleApiService = inject(SaleApiService);
  private prenotazioniApi = inject(PrenotazioniApiService);
  private accountService = inject(AccountService);
  private account: any;

  ngOnInit(): void {
    this.accountService.identity().subscribe(acc => {
      this.account = acc;
    });

    this.route.queryParams.subscribe(params => {
      this.dataRicerca = params['data'] ?? '';
      this.oraRicerca = params['ora'] ?? '';
      this.capienzaRicerca = Number(params['capienzaMax'] ?? 0);

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
    if (!this.salaSelezionata) return;

    this.isLoading = true;

    const sala = this.salaSelezionata;
    const [oraInizio, oraFine] = this.oraRicerca.split('-').map(o => o.trim());

    const payload = {
      data: this.dataRicerca,
      oraInizio: this.normalizzaOra(oraInizio),
      oraFine: this.normalizzaOra(oraFine),
      tipoEvento: isPubblico ? 'PUBBLICO' : 'PRIVATO',
      prezzo: null,
      salaId: sala.id,
    };

    this.prenotazioniApi.creaPrenotazione(payload).subscribe({
      next: pren => {
        this.showPrivacyModal = false;

        this.router
          .navigate(['/prenota-sala/crea-evento'], {
            queryParams: {
              salaId: sala.id,
              nomeSala: sala.nome,
              data: this.dataRicerca,
              ora: this.oraRicerca,
              pubblico: isPubblico,
              prenotazioneId: pren.id,
            },
          })
          .finally(() => (this.isLoading = false));
      },
      error: err => {
        console.error('Errore creazione prenotazione:', err);
        this.isLoading = false;
      },
    });
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
