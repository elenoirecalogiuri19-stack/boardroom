import { Route } from '@angular/router';

export const MIE_PRENOTAZIONI_ROUTE: Route = {
  path: 'mie-prenotazioni',
  loadComponent: () => import('./mie-prenotazioni.component').then(m => m.MiePrenotazioniComponent),
  data: {
    pageTitle: 'Le mie prenotazioni',
  },
};
