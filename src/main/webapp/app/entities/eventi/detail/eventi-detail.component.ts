import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { IEventi } from '../eventi.model';
import SharedModule from 'app/shared/shared.module';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'jhi-eventi-detail',
  templateUrl: './eventi-detail.component.html',
  styleUrl: './eventi-detail.component.scss',
  imports: [SharedModule, RouterModule, CommonModule, FormsModule],
})
export class EventiDetailComponent implements OnInit {
  eventi = signal<IEventi | null>(null);
  isLoading = false;
  showModal = false;

  prenotazione = {
    nome: '',
    cognome: '',
    email: '',
  };

  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ eventi }) => {
      this.eventi.set(eventi);
    });
  }

  previousState(): void {
    window.history.back();
  }

  apriModal(): void {
    this.showModal = true;
  }

  chiudiModal(): void {
    this.showModal = false;
  }

  confermaPrenotazione(): void {
    this.isLoading = true;

    setTimeout(() => {
      this.isLoading = false;
      this.showModal = false;
      this.router.navigate(['/']);
    }, 2000);
  }
}
