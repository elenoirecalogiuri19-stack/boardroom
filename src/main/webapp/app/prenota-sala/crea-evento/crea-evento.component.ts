import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from 'app/shared/notification/notification.service';

@Component({
  selector: 'jhi-crea-evento',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crea-evento.component.html',
  styleUrl: './crea-evento.component.scss',
})
export class CreaEventoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private notificationService = inject(NotificationService);

  isLoading = false;
  isPubblico = false;
  capienzaRicerca = 0;

  evento = {
    titolo: '',
    descrizione: '',
    prezzo: null as number | null,
    salaId: '',
    nomeSala: '',
    data: '',
    ora: '',
  };

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.isPubblico = params['pubblico'] === 'true';
      this.evento.salaId = params['salaId'];
      this.evento.nomeSala = params['nomeSala'];
      this.evento.data = params['data'];
      this.evento.ora = params['ora'];
      this.capienzaRicerca = params['persone'] ? Number(params['persone']) : 0;

      if (!this.evento.salaId) {
        this.router.navigate(['/prenota-sala']);
      }
    });
  }

  conferma(): void {
    if (!this.evento.titolo) {
      this.notificationService.show("Inserire un titolo per l'evento", 'error');
      return;
    }

    this.isLoading = true;

    setTimeout(() => {
      this.isLoading = false;
      this.notificationService.show('Evento creato con successo!', 'success');
      this.router.navigate(['/']);
    }, 2000);
  }

  annulla(): void {
    this.router.navigate(['/risultati-sala'], {
      queryParams: {
        data: this.evento.data,
        ora: this.evento.ora,
        persone: this.capienzaRicerca,
        salaId: this.evento.salaId,
        apriModal: 'true',
      },
    });
  }
}
