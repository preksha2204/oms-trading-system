import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Trade } from '../models/models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TradeService {

  private readonly baseUrl = `${environment.apiBaseUrl}/api/trades`;

  constructor(private http: HttpClient) {}

  getBySymbol(symbol: string, page = 0, size = 50): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<any>(`${this.baseUrl}/symbol/${symbol}`, { params });
  }
}
