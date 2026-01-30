import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RicercaService } from '../services/ricerca.service';

@Component({
  standalone: true,
  selector: 'jhi-prenota-sala',
  templateUrl: './prenota-sala.component.html',
  styleUrls: ['./prenota-sala.component.scss'],
  imports: [CommonModule, FormsModule],
})
export default class PrenotaSalaComponent implements OnInit {
  orari: string[] = [];
  capienza = 50;
  dataSelezionata: string = '';
  oraSelezionata: string = '';

  caricamento = false;

  constructor(
    private router: Router,
    private ricercaService: RicercaService,
  ) {}

  ngOnInit(): void {
    this.generaOrari();

    const salvati = this.ricercaService.recuperaRicerca();
    if (salvati.data) {
      this.dataSelezionata = salvati.data;
      this.oraSelezionata = salvati.ora;
      this.capienza = salvati.capienza;
    }
  }

  generaOrari(): void {
    this.orari = [];
    for (let i = 8; i < 20; i++) {
      const fascia = `${i}:00 - ${i + 1}:00`;
      this.orari.push(fascia);
    }
  }

  confermaPrenotazione(): void {
    this.caricamento = true;

    this.ricercaService.salvaRicerca({
      data: this.dataSelezionata,
      ora: this.oraSelezionata,
      capienza: this.capienza,
    });

    this.router
      .navigate(['/risultati-sala'], {
        queryParams: {
          data: this.dataSelezionata,
          ora: this.oraSelezionata,
          persone: this.capienza,
        },
      })
      .then(() => {
        this.caricamento = false;
      });
  }
}
