import { Injectable } from '@angular/core';

export interface IRicercaSale {
  data: string;
  ora: string;
  capienzaMax: number;
}

@Injectable({
  providedIn: 'root',
})
export class RicercaService {
  private datiRicerca: IRicercaSale = {
    data: '',
    ora: '',
    capienzaMax: 0,
  };

  salvaRicerca(nuoviDati: IRicercaSale): void {
    console.warn('Service: Salvataggio dati...', nuoviDati);
    this.datiRicerca = { ...nuoviDati };
  }

  recuperaRicerca(): IRicercaSale {
    return { ...this.datiRicerca };
  }

  resetRicerca(): void {
    console.warn('Service: Reset dati effettuato.');
    this.datiRicerca = {
      data: '',
      ora: '',
      capienzaMax: 0,
    };
  }
}
