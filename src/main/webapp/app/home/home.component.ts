import { Component, OnDestroy, OnInit, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';

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
  @ViewChild('carousel') carousel!: ElementRef;

  account = signal<Account | null>(null);
  eventi = signal<IEventi[]>([]);
  isLoading = signal<boolean>(true);

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private readonly eventiService = inject(EventiService);
  private readonly notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.caricaEventi();

    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => this.account.set(account));
  }

  scroll(offset: number): void {
    if (this.carousel) {
      this.carousel.nativeElement.scrollBy({ left: offset, behavior: 'smooth' });
    }
  }

  caricaEventi(): void {
    this.isLoading.set(true);
    this.eventiService
      .getEventiPubblici()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: data => {
          this.eventi.set(data);
        },
        error: () => {
          this.notificationService.show('Errore nel caricamento degli eventi pubblici', 'error');
        },
      });
  }

  vaiAlleMiePrenotazioni(): void {
    this.gestisciNavigazioneProtetta('/mie-prenotazioni');
  }

  private gestisciNavigazioneProtetta(destinazione: string): void {
    if (this.account() === null) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: destinazione } });
    } else {
      this.router.navigate([destinazione]);
    }
  }

  vaiADettagli(evento: IEventi): void {
    this.router.navigate(['/eventi', evento.id, 'view']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
