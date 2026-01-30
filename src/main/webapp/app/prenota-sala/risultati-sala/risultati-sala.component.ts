import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Sala } from './sala.model';
import { RicercaService } from 'app/services/ricerca.service';

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

  sale: Sala[] = [
    { id: 1, nome: 'Sala Smeraldo', titolo: 'Meeting Room A', descrizione: 'Perfetta per brainstorming e team building.' },
    { id: 2, nome: 'Sala Rubino', titolo: 'Conference Room B', descrizione: 'Attrezzatura video 4K e sistema audio surround.' },
    { id: 3, nome: 'Sala Zaffiro', titolo: 'Workshop Space', descrizione: 'Ampia e luminosa, ideale per corsi di formazione.' },
  ];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private ricercaService: RicercaService,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.dataRicerca = params['data'];
      this.oraRicerca = params['ora'];
      this.capienzaRicerca = +params['persone'];
    });
  }

  tornaIndietro(): void {
    this.router.navigate(['/prenota-sala']);
  }

  selezionaSala(sala: Sala): void {
    console.log('Hai scelto per il tuo evento:', sala.nome);

    this.ricercaService.resetRicerca();
  }
}
