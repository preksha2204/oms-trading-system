import { Component, OnInit, OnDestroy } from '@angular/core';
import { WebSocketService } from '../../core/services/websocket.service';
import { MarketData } from '../../core/models/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-market-data',
  template: `
    <div class="market-page">
      <div class="page-header">
        <h1>Market Data</h1>
        <span class="sub">Live quotes streamed via WebSocket</span>
      </div>

      <div class="quotes-grid">
        <div *ngFor="let kv of quotes | keyvalue" class="quote-card">
          <div class="symbol">{{ kv.key }}</div>
          <div class="price-row">
            <div class="bid-ask">
              <span class="label">Bid</span>
              <span class="bid">\${{ kv.value.bid | number:'1.2-2' }}</span>
            </div>
            <div class="last-price">\${{ kv.value.last | number:'1.2-2' }}</div>
            <div class="bid-ask">
              <span class="label">Ask</span>
              <span class="ask">\${{ kv.value.ask | number:'1.2-2' }}</span>
            </div>
          </div>
          <div class="ohlv-row">
            <span><small>O</small>\${{ kv.value.open | number:'1.2-2' }}</span>
            <span class="high"><small>H</small>\${{ kv.value.high | number:'1.2-2' }}</span>
            <span class="low"><small>L</small>\${{ kv.value.low | number:'1.2-2' }}</span>
            <span><small>Vol</small>{{ kv.value.volume | number }}</span>
          </div>
        </div>
      </div>

      <div *ngIf="quotes.size === 0" class="empty">
        <mat-icon>wifi_off</mat-icon>
        <p>Waiting for market data feed...</p>
      </div>
    </div>
  `,
  styles: [`
    .market-page { }
    .page-header h1 { color:#e2e8f0; font-size:1.6rem; margin:0 0 4px; }
    .sub { color:#718096; font-size:.88rem; }
    .quotes-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr)); gap:16px; margin-top:20px; }
    .quote-card { background:#111827; border:1px solid rgba(99,179,237,.15); border-radius:12px;
                  padding:16px; transition:border-color .2s, box-shadow .2s; }
    .quote-card:hover { border-color:#63b3ed; box-shadow:0 0 20px rgba(99,179,237,.1); }
    .symbol { font-size:1.1rem; font-weight:700; color:#90cdf4; margin-bottom:12px; }
    .price-row { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; }
    .bid-ask { display:flex; flex-direction:column; align-items:center; gap:2px; }
    .label { color:#718096; font-size:.7rem; }
    .bid  { color:#68d391; font-weight:700; font-size:.95rem; }
    .ask  { color:#fc8181; font-weight:700; font-size:.95rem; }
    .last-price { font-size:1.4rem; font-weight:800; color:#e2e8f0; }
    .ohlv-row { display:flex; gap:10px; font-size:.75rem; color:#718096; justify-content:space-between; }
    .high { color:#68d391; }
    .low  { color:#fc8181; }
    .empty { text-align:center; color:#4a5568; padding:64px; }
    .empty mat-icon { font-size:3rem; display:block; margin:0 auto 12px; }
  `]
})
export class MarketDataPageComponent implements OnInit, OnDestroy {
  quotes = new Map<string, MarketData>();
  private sub!: Subscription;

  constructor(private ws: WebSocketService) {}

  ngOnInit(): void {
    this.sub = this.ws.marketData$.subscribe(md => {
      this.quotes.set(md.symbol, md);
    });
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}
