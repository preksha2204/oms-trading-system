import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="dashboard">
      <div class="page-header">
        <h1>Trading Dashboard</h1>
        <span class="sub">Real-time OMS monitoring — orders, trades, and options pricing</span>
      </div>

      <div class="top-row">
        <app-order-grid></app-order-grid>
        <app-trade-blotter></app-trade-blotter>
      </div>

      <div class="bottom-row">
        <app-options-panel></app-options-panel>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { display:flex; flex-direction:column; gap:20px; }
    .page-header h1 { color:#e2e8f0; font-size:1.6rem; font-weight:700; margin:0 0 4px; }
    .sub { color:#718096; font-size:.88rem; }
    .top-row { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
    .bottom-row { display:grid; grid-template-columns:1fr; gap:20px; }
    @media(max-width:1100px) { .top-row { grid-template-columns:1fr; } }
  `]
})
export class DashboardComponent {}
