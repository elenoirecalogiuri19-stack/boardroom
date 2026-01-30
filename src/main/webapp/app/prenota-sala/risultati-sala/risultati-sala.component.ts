import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RicercaService } from 'app/services/ricerca.service';
import { EventiService } from 'app/entities/eventi/service/eventi.service';
import { IEventi } from 'app/entities/eventi/eventi.model';

@Component({
  selector: 'jhi-risultati-sala',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, FontAwesomeModule],
  templateUrl: './risultati-sala.component.html',
  styleUrl: './risultati-sala.component.scss',
})
export class RisultatiSalaComponent implements OnInit {
  dataRicerca: string = '';
  oraRicerca: string = '';
  capienzaRicerca: number = 0;

  eventi: IEventi[] = [];
  caricamento: boolean = true;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private ricercaService: RicercaService,
    private eventiService: EventiService,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.dataRicerca = params['data'] || '';
      this.oraRicerca = params['ora'] || '';
      this.capienzaRicerca = +params['persone'] || 0;

      this.caricaEFilterEventi();
    });
  }

  caricaEFilterEventi(): void {
    this.caricamento = true;
    this.eventiService.query().subscribe({
      next: res => {
        const tuttiGliEventi = res.body ?? [];

        this.eventi = tuttiGliEventi.filter(ev => {
          const isValido = !!ev.titolo && !!ev.data;

          const matchData = ev.data === this.dataRicerca;

          const matchOra = this.oraRicerca ? ev.oraInizio?.toString().includes(this.oraRicerca) : true;

          return isValido && matchData && matchOra;
        });

        this.caricamento = false;
        console.log('Risultati filtrati:', this.eventi);
      },
      error: err => {
        console.error('Errore:', err);
        this.caricamento = false;
      },
    });
  }

  tornaIndietro(): void {
    this.router.navigate(['/prenota-sala']);
  }

  selezionaSala(evento: IEventi): void {
    console.log('Sala selezionata:', evento.salaNome);
    // Qui andrai alla pagina di conferma o pagamento
  }
}
