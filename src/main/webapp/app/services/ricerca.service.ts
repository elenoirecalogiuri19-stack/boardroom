import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class RicercaService {
  private datiRicerca = {
    data: '',
    ora: '',
    capienza: 1,
  };

  constructor() {}

  salvaRicerca(nuoviDati: any) {
    console.log('Service: Salvataggio dati...', nuoviDati);
    this.datiRicerca = nuoviDati;
  }

  recuperaRicerca() {
    return this.datiRicerca;
  }

  resetRicerca() {
    console.log('Service: Reset dati effettuato.');
    this.datiRicerca = {
      data: '',
      ora: '',
      capienza: 1,
    };
  }
}
