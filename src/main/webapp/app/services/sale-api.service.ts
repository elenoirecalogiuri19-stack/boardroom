import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ISalaDTO {
  id: string;
  nome: string;
  capienza: number;
}

@Injectable({ providedIn: 'root' })
export class SaleApiService {
  private resourceUrl = '/api/sales';

  constructor(private http: HttpClient) {}

  getSaleDisponibili(data: string, inizio: string, fine: string, capienzaMin: number, capienzaMax?: number): Observable<ISalaDTO[]> {
    let params = new HttpParams().set('data', data).set('inizio', inizio).set('fine', fine).set('capienzaMin', capienzaMin.toString());

    if (capienzaMax !== undefined) {
      params = params.set('capienzaMax', capienzaMax.toString());
    }

    return this.http.get<ISalaDTO[]>(`${this.resourceUrl}/disponibili`, { params });
  }
}
