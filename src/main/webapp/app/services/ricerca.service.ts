import { Injectable } from '@angular/core';

export interface IRicercaSale {
  data: string;
  ora: string;
  capienza: number | null;
}

@Injectable({
  providedIn: 'root',
})
export class RicercaService {
  private datiRicerca: IRicercaSale = {
    data: '',
    ora: '',
    capienza: null,
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
      capienza: null,
    };
  }
}
