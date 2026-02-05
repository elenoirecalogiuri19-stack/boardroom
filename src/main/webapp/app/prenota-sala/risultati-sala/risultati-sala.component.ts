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
  private saleApiService = inject(SaleApiService);

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.dataRicerca = params['data'] ?? '';
      this.oraRicerca = params['ora'] ?? '';
      this.capienzaRicerca = Number(params['capienza'] ?? 0);
      this.caricaSaleDisponibili();
    });
  }

  tornaIndietro(): void {
    this.router.navigate(['/prenota-sala']);
  }

  apriSceltaPrivacy(sala: Sala): void {
    this.salaSelezionata = sala;
    this.showPrivacyModal = true;
  }

  confermaEProcedi(isPubblico: boolean): void {
    if (!this.salaSelezionata) return;

    this.showPrivacyModal = false;
    this.isLoading = true;

    this.router
      .navigate(['/prenota-sala/crea-evento'], {
        queryParams: {
          salaId: this.salaSelezionata.id,
          nomeSala: this.salaSelezionata.nome,
          data: this.dataRicerca,
          ora: this.oraRicerca,
          pubblico: isPubblico,
        },
      })
      .then(() => {
        this.isLoading = false;
      });
  }

  private caricaSaleDisponibili(): void {
    if (!this.dataRicerca || !this.oraRicerca) return;

    this.isLoading = true;
    const parts = this.oraRicerca.split('-');
    const inizio = this.normalizzaOra(parts[0].trim());
    const fine = this.normalizzaOra(parts[1].trim());

    this.saleApiService.getSaleDisponibili(this.dataRicerca, inizio, fine, this.capienzaRicerca).subscribe({
      next: (saleDto: ISalaDTO[]) => {
        this.sale = saleDto.map(s => ({
          ...s,
          id: s.id.toString(),
          nome: s.nome || 'Sala Executive',
          capienza: s.capienza || 0,
        }));
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  private normalizzaOra(ora: string): string {
    const [h, m] = ora.split(':');
    return `${h.padStart(2, '0')}:${m ?? '00'}`;
  }
}
