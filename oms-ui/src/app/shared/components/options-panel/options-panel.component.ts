import { Component, OnInit, OnDestroy } from '@angular/core';
import { WebSocketService } from '../../core/services/websocket.service';
import { OptionPrice } from '../../core/models/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-options-panel',
  template: `
    <mat-card class="options-card">
      <mat-card-header>
        <mat-card-title><mat-icon>trending_up</mat-icon> Options Pricing (Black-Scholes)</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <div class="options-grid">
          <div *ngFor="let opt of options | keyvalue" class="option-tile">
            <div class="opt-symbol">{{ opt.key }}</div>
            <div class="opt-row"><span class="label">Call</span><span class="call-price">\${{ opt.value.callPrice | number:'1.4-4' }}</span></div>
            <div class="opt-row"><span class="label">Put</span><span class="put-price">\${{ opt.value.putPrice | number:'1.4-4' }}</span></div>
            <mat-divider></mat-divider>
            <div class="greeks-row">
              <span class="greek"><small>Δ</small>{{ opt.value.delta | number:'1.4-4' }}</span>
              <span class="greek"><small>Γ</small>{{ opt.value.gamma | number:'1.6-6' }}</span>
              <span class="greek"><small>ν</small>{{ opt.value.vega | number:'1.4-4' }}</span>
              <span class="greek"><small>Θ</small>{{ opt.value.theta | number:'1.4-4' }}</span>
            </div>
            <div class="updated-at">{{ opt.value.calculatedAt | date:'HH:mm:ss.SSS' }}</div>
          </div>
        </div>
        <div *ngIf="options.size === 0" class="empty-state">
          <mat-icon>hourglass_empty</mat-icon>
          <p>Waiting for trade events to trigger repricing...</p>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .options-card { background:#111827; border:1px solid rgba(104,211,145,.15); border-radius:12px; }
    mat-card-title { display:flex; align-items:center; gap:8px; color:#e2e8f0; }
    .options-grid { display:flex; flex-wrap:wrap; gap:16px; margin-top:12px; }
    .option-tile { background:#0d1b2a; border:1px solid rgba(104,211,145,.2); border-radius:10px;
                   padding:14px; min-width:200px; flex:1; }
    .opt-symbol { color:#68d391; font-weight:700; font-size:.85rem; margin-bottom:8px; }
    .opt-row { display:flex; justify-content:space-between; align-items:center; margin:4px 0; }
    .label { color:#718096; font-size:.78rem; }
    .call-price { color:#68d391; font-weight:700; font-size:1rem; }
    .put-price { color:#fc8181; font-weight:700; font-size:1rem; }
    .greeks-row { display:flex; gap:12px; margin-top:8px; flex-wrap:wrap; }
    .greek { color:#a0aec0; font-size:.75rem; display:flex; align-items:center; gap:2px; }
    .greek small { color:#4a5568; margin-right:2px; }
    .updated-at { color:#4a5568; font-size:.7rem; margin-top:6px; }
    .empty-state { text-align:center; color:#4a5568; padding:32px; }
    .empty-state mat-icon { font-size:2rem; display:block; }
    mat-divider { margin:8px 0; border-color:rgba(104,211,145,.1) !important; }
  `]
})
export class OptionsPanelComponent implements OnInit, OnDestroy {
  options = new Map<string, OptionPrice>();
  private sub!: Subscription;

  constructor(private ws: WebSocketService) {}

  ngOnInit(): void {
    this.sub = this.ws.options$.subscribe(opt => {
      this.options.set(opt.symbol, opt);
    });
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}
