import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IEventi } from '../eventi.model';

@Component({
  selector: 'jhi-eventi-detail',
  templateUrl: './eventi-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class EventiDetailComponent {
  eventi = input<IEventi | null>(null);

  previousState(): void {
    window.history.back();
  }
}
