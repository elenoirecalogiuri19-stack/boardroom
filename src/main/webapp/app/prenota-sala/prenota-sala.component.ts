import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'jhi-prenota-sala',
  templateUrl: './prenota-sala.component.html',
  styleUrls: ['./prenota-sala.component.scss'],
  imports: [CommonModule, FormsModule], //
})
export default class PrenotaSalaComponent implements OnInit {
  orari: string[] = [];
  capienza = 50;
  dataSelezionata: string = '';
  oraSelezionata: string = '';

  ngOnInit(): void {
    this.generaOrari();
  }

  generaOrari(): void {
    for (let i = 8; i < 20; i++) {
      const fascia = `${i}:00 - ${i + 1}:00`;
      this.orari.push(fascia);
    }
  }

  confermaPrenotazione(): void {
    console.log('Dati prenotazione:', {
      data: this.dataSelezionata,
      ora: this.oraSelezionata,
      persone: this.capienza,
    });
  }
}
