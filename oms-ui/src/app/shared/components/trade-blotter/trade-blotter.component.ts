import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { WebSocketService } from '../../core/services/websocket.service';
import { Trade } from '../../core/models/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-trade-blotter',
  template: `
    <mat-card class="blotter-card">
      <mat-card-header>
        <mat-card-title><mat-icon>show_chart</mat-icon> Trade Blotter <span class="badge">{{ trades.length }}</span></mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <div class="table-container">
          <table mat-table [dataSource]="dataSource" class="blotter-table">
            <ng-container matColumnDef="tradeId">
              <th mat-header-cell *matHeaderCellDef>Trade ID</th>
              <td mat-cell *matCellDef="let t" class="mono">{{ t.tradeId | slice:0:12 }}...</td>
            </ng-container>
            <ng-container matColumnDef="symbol">
              <th mat-header-cell *matHeaderCellDef>Symbol</th>
              <td mat-cell *matCellDef="let t"><span class="symbol-chip">{{ t.symbol }}</span></td>
            </ng-container>
            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef>Quantity</th>
              <td mat-cell *matCellDef="let t" class="number">{{ t.quantity | number:'1.0-0' }}</td>
            </ng-container>
            <ng-container matColumnDef="price">
              <th mat-header-cell *matHeaderCellDef>Price</th>
              <td mat-cell *matCellDef="let t" class="number price">{{ t.price | number:'1.2-4' }}</td>
            </ng-container>
            <ng-container matColumnDef="executedAt">
              <th mat-header-cell *matHeaderCellDef>Time</th>
              <td mat-cell *matCellDef="let t" class="mono">{{ t.executedAt | date:'HH:mm:ss.SSS' }}</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns; sticky:true"></tr>
            <tr mat-row *matRowDef="let row; columns:columns;" [class.row-new]="row.isNew"></tr>
          </table>
        </div>
        <mat-paginator [pageSizeOptions]="[25,50,100]" showFirstLastButtons></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .blotter-card { background:#111827; border:1px solid rgba(159,122,234,.15); border-radius:12px; }
    mat-card-title { display:flex; align-items:center; gap:8px; color:#e2e8f0; }
    .badge { background:#2d3748; color:#9f7aea; border-radius:12px; padding:2px 8px; font-size:.75rem; }
    .table-container { max-height:380px; overflow:auto; }
    table { width:100%; }
    th.mat-header-cell { color:#9f7aea; font-weight:600; font-size:.78rem; border-bottom:1px solid #2d3748; }
    td.mat-cell { color:#cbd5e0; font-size:.82rem; border-bottom:1px solid rgba(45,55,72,.5); }
    .mono { font-family:'Courier New', monospace; font-size:.78rem; }
    .number { text-align:right; font-variant-numeric:tabular-nums; }
    .price  { color:#f6e05e; font-weight:700; }
    .symbol-chip { background:#2d1b4e; color:#b794f4; padding:2px 8px; border-radius:6px; font-weight:700; font-size:.8rem; }
    .row-new { animation:flashTrade .8s ease-out; }
    @keyframes flashTrade { from { background:rgba(159,122,234,.25); } to { background:transparent; } }
  `]
})
export class TradeBlotterComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  columns = ['tradeId','symbol','quantity','price','executedAt'];
  trades: Trade[] = [];
  dataSource = new MatTableDataSource<Trade>([]);
  private sub!: Subscription;

  constructor(private ws: WebSocketService) {}

  ngOnInit(): void {
    this.sub = this.ws.trades$.subscribe(trade => {
      trade.isNew = true;
      this.trades.unshift(trade);
      if (this.trades.length > 500) this.trades.pop();
      this.dataSource.data = [...this.trades];
      setTimeout(() => { trade.isNew = false; }, 1000);
    });
  }

  ngAfterViewInit(): void { this.dataSource.paginator = this.paginator; }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}
