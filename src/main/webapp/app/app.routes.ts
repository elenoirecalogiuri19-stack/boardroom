import { Routes } from '@angular/router';

import { Authority } from 'app/config/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component'),
    title: 'Boardroom',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar.component'),
    outlet: 'navbar',
  },

  {
    path: 'prenota-sala',
    loadComponent: () => import('./prenota-sala/prenota-sala.component'),
    title: 'Prenota Sala',
    canActivate: [UserRouteAccessService],
  },

  {
    path: 'risultati-sala',
    loadComponent: () => import('./prenota-sala/risultati-sala/risultati-sala.component').then(m => m.RisultatiSalaComponent),
    title: 'Sale Disponibili',
    canActivate: [UserRouteAccessService],
  },

  {
    path: 'prenota-sala/crea-evento',
    loadComponent: () => import('./prenota-sala/crea-evento/crea-evento.component').then(m => m.CreaEventoComponent),
    title: 'Dettagli Evento',
    canActivate: [UserRouteAccessService],
  },

  {
    path: 'admin',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component'),
    title: 'Login',
  },
  {
    path: '',
    loadChildren: () => import(`./entities/entity.routes`),
  },
  ...errorRoute,
];

export default routes;
