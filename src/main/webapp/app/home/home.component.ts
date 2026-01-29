import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  standalone: true,
  imports: [SharedModule, RouterModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);

  // LA TUA LISTA EVENTI (Dati che vedrai nelle card)
  eventiFinti = [
    { titolo: 'Workshop Strategie', sala: 'Sala A - Piano 1', ora: '10:00 - 12:00', prezzo: '25,00' },
    { titolo: 'Cyber Security Talk', sala: 'Sala BoardRoom', ora: '15:00 - 17:00', prezzo: '15,00' },
    { titolo: 'Meeting Startup', sala: 'Open Space', ora: '18:00 - 19:30', prezzo: 'Gratis' },
    { titolo: 'Blockchain Day', sala: 'Laboratorio Sud', ora: '09:30 - 13:30', prezzo: '40,00' },
    { titolo: 'Workshop AI', sala: 'Sala B - Piano 2', ora: '18:00 - 20:00', prezzo: '20,00' },
    { titolo: 'Tech Talk JS', sala: 'Sala Conferenze', ora: '11:00 - 14:00', prezzo: '10,00' },
    { titolo: 'UI/UX Masterclass', sala: 'Aula Design', ora: '14:00 - 18:00', prezzo: '35,00' },
    { titolo: 'Cloud Computing', sala: 'Settore Server', ora: '10:00 - 12:30', prezzo: 'Gratis' },
  ];

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => this.account.set(account));
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
