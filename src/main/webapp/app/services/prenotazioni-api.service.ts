import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface PrenotazioneDTO {
  id: string;
}

@Injectable({ providedIn: 'root' })
export class PrenotazioniApiService {
  private baseUrl = '/api/prenotazionis';

  constructor(private http: HttpClient) {}

  creaPrenotazione(payload: any): Observable<PrenotazioneDTO> {
    return this.http.post<PrenotazioneDTO>(`${this.baseUrl}/prenotta`, payload);
  }
}
