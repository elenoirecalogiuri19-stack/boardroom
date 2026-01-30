import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { EventiService } from 'app/entities/eventi/service/eventi.service';
import { IEventi } from 'app/entities/eventi/eventi.model';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { NotificationService } from 'app/shared/notification/notification.service';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  standalone: true,
  imports: [SharedModule, RouterModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);
  eventi = signal<IEventi[]>([]);

  caricamento = false;

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private readonly eventiService = inject(EventiService);
  private readonly notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => this.account.set(account));

    this.caricaEventi();
  }

  caricaEventi(): void {
    this.eventiService.getEventiPubblici().subscribe({
      next: data => {
        this.eventi.set(data);
      },
      error: () => {
        this.notificationService.show('Errore nel caricamento degli eventi pubblici', 'error');
      },
    });
  }

  vaiADettagli(evento: IEventi): void {
    this.caricamento = true;
    this.router.navigate(['/eventi', evento.id, 'view']).then(() => {
      this.caricamento = false;
    });
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
