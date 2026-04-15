import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { WebSocketService } from '../../core/services/websocket.service';
import { Order } from '../../core/models/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-order-grid',
  template: `
    <mat-card class="grid-card">
      <mat-card-header>
        <mat-card-title>
          <mat-icon>list_alt</mat-icon> Live Orders
          <span class="badge">{{ dataSource.data.length }}</span>
        </mat-card-title>
        <span class="spacer"></span>
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Filter</mat-label>
          <input matInput (keyup)="applyFilter($event)" placeholder="Symbol, status...">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      </mat-card-header>
      <mat-card-content>
        <div class="table-container">
          <table mat-table [dataSource]="dataSource" matSort class="orders-table">

            <ng-container matColumnDef="orderId">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Order ID</th>
              <td mat-cell *matCellDef="let o" class="mono">{{ o.orderId }}</td>
            </ng-container>

            <ng-container matColumnDef="clOrdId">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Cl Ord ID</th>
              <td mat-cell *matCellDef="let o" class="mono">{{ o.clOrdId }}</td>
            </ng-container>

            <ng-container matColumnDef="symbol">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Symbol</th>
              <td mat-cell *matCellDef="let o"><span class="symbol-chip">{{ o.symbol }}</span></td>
            </ng-container>

            <ng-container matColumnDef="side">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Side</th>
              <td mat-cell *matCellDef="let o">
                <span [class]="'side-badge side-' + o.side?.toLowerCase()">{{ o.side }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Qty</th>
              <td mat-cell *matCellDef="let o" class="number">{{ o.quantity | number:'1.0-0' }}</td>
            </ng-container>

            <ng-container matColumnDef="filledQty">
              <th mat-header-cell *matHeaderCellDef>Filled</th>
              <td mat-cell *matCellDef="let o" class="number">{{ o.filledQty | number:'1.0-0' }}</td>
            </ng-container>

            <ng-container matColumnDef="price">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Price</th>
              <td mat-cell *matCellDef="let o" class="number price">{{ o.price | number:'1.2-2' }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
              <td mat-cell *matCellDef="let o">
                <span [class]="'status-badge status-' + o.status?.toLowerCase()">{{ o.status }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="receivedAt">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Time</th>
              <td mat-cell *matCellDef="let o" class="mono time">{{ o.receivedAt | date:'HH:mm:ss.SSS' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns; sticky: true"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;"
                [class.row-new]="row.isNew"
                [class.row-buy]="row.side === 'BUY'"
                [class.row-sell]="row.side === 'SELL'"></tr>
          </table>
        </div>
        <mat-paginator [pageSizeOptions]="[25,50,100]" showFirstLastButtons></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .grid-card { background:#111827; border:1px solid rgba(99,179,237,.15); border-radius:12px; }
    mat-card-title { display:flex; align-items:center; gap:8px; color:#e2e8f0; font-size:1rem; }
    .badge { background:#2d3748; color:#63b3ed; border-radius:12px; padding:2px 8px; font-size:.75rem; }
    .spacer { flex:1; }
    .filter-field { width:220px; }
    .table-container { max-height:420px; overflow:auto; }
    table { width:100%; }
    th.mat-header-cell { color:#63b3ed; font-weight:600; font-size:.78rem; border-bottom:1px solid #2d3748; }
    td.mat-cell { color:#cbd5e0; font-size:.82rem; border-bottom:1px solid rgba(45,55,72,.5); }
    .mono { font-family: 'Courier New', monospace; font-size:.78rem; }
    .number { text-align:right; font-variant-numeric:tabular-nums; }
    .price { color:#68d391; font-weight:600; }
    .time { color:#718096; font-size:.74rem; }
    .symbol-chip { background:#1a2744; color:#90cdf4; padding:2px 8px; border-radius:6px; font-weight:700; font-size:.8rem; }
    .side-badge { padding:2px 8px; border-radius:6px; font-size:.75rem; font-weight:700; }
    .side-buy  { background:rgba(104,211,145,.15); color:#68d391; }
    .side-sell { background:rgba(252,129,129,.15); color:#fc8181; }
    .status-badge { padding:2px 8px; border-radius:6px; font-size:.72rem; font-weight:600; text-transform:uppercase; }
    .status-new     { background:rgba(99,179,237,.15); color:#63b3ed; }
    .status-partial { background:rgba(237,184,87,.15); color:#edb857; }
    .status-filled  { background:rgba(104,211,145,.15); color:#68d391; }
    .status-cancelled { background:rgba(160,174,192,.15); color:#a0aec0; }
    .status-rejected  { background:rgba(252,129,129,.15); color:#fc8181; }
    .row-new { animation: flashNew .8s ease-out; }
    @keyframes flashNew { from { background:rgba(99,179,237,.2); } to { background:transparent; } }
  `]
})
export class OrderGridComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  columns = ['orderId','clOrdId','symbol','side','quantity','filledQty','price','status','receivedAt'];
  orders: Order[] = [];
  dataSource = new MatTableDataSource<Order>([]);
  private sub!: Subscription;

  constructor(private ws: WebSocketService) {}

  ngOnInit(): void {
    this.sub = this.ws.orders$.subscribe(order => {
      order.isNew = true;
      const idx = this.orders.findIndex(o => o.orderId === order.orderId);
      if (idx >= 0) this.orders[idx] = order;
      else this.orders.unshift(order);
      this.dataSource.data = [...this.orders];
      setTimeout(() => { order.isNew = false; }, 1000);
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  applyFilter(event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    this.dataSource.filter = val.trim().toLowerCase();
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}
