import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoaderService } from './loader.service';

@Component({
  selector: 'jhi-global-loader',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (loaderService.isLoading()) {
      <div class="loader-overlay">
        <div class="custom-spinner"></div>
      </div>
    }
  `,
  styles: [
    `
      .loader-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, 0.7); /* Sfondo leggermente velato */
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
      }
      .custom-spinner {
        width: 55px;
        height: 55px;
        border: 6px solid #f3f3f3;
        border-top: 6px solid #4a8c40; /* Il tuo verde */
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }
      @keyframes spin {
        0% {
          transform: rotate(0deg);
        }
        100% {
          transform: rotate(360deg);
        }
      }
    `,
  ],
})
export class LoaderComponent {
  loaderService = inject(LoaderService);
}
