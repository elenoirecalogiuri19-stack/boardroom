import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface PrenotazioneDTO {
  id: string;
  eventoId?: string | null;
  data: string;
  oraInizio: string;
  oraFine: string;
  numPersone?: number;
  stato?: {
    id: string;
    codice: string;
    descrizione: string;
  } | null;
  utente?: {
    id: string;
    nome: string;
  } | null;
  sala?: {
    id: string;
    nome: string;
  } | null;
  salaId?: string;
  evento?: {
    titolo: string;
    tipo: string;
    prezzo: number;
  };
}

@Injectable({ providedIn: 'root' })
export class PrenotazioniApiService {
  private baseUrl = '/api/prenotazionis';

  constructor(private http: HttpClient) {}

  creaPrenotazione(payload: any): Observable<PrenotazioneDTO> {
    return this.http.post<PrenotazioneDTO>(`${this.baseUrl}/prenotta`, payload);
  }

  getMiePrenotazioni(): Observable<PrenotazioneDTO[]> {
    return this.http.get<PrenotazioneDTO[]>(`${this.baseUrl}/odierne`);
  }

  cancellaPrenotazione(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/cancella/${id}`);
  }
}
