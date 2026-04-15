import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrderService } from '../../core/services/order.service';
import { OrderRequest } from '../../core/models/models';

@Component({
  selector: 'app-order-entry',
  template: `
    <div class="order-entry-page">
      <div class="page-header">
        <h1>Order Entry</h1>
        <span class="sub">Submit new orders to the OMS</span>
      </div>

      <mat-card class="entry-card">
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()" class="order-form">

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Client Order ID</mat-label>
                <input matInput formControlName="clOrdId" placeholder="e.g. ORD-001">
                <mat-error *ngIf="form.get('clOrdId')?.invalid">Required</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Symbol</mat-label>
                <mat-select formControlName="symbol">
                  <mat-option *ngFor="let s of symbols" [value]="s">{{ s }}</mat-option>
                </mat-select>
                <mat-error *ngIf="form.get('symbol')?.invalid">Required</mat-error>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Side</mat-label>
                <mat-select formControlName="side">
                  <mat-option value="BUY">BUY</mat-option>
                  <mat-option value="SELL">SELL</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Order Type</mat-label>
                <mat-select formControlName="orderType">
                  <mat-option value="LIMIT">LIMIT</mat-option>
                  <mat-option value="MARKET">MARKET</mat-option>
                </mat-select>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Quantity</mat-label>
                <input matInput type="number" formControlName="quantity" placeholder="100">
                <mat-error *ngIf="form.get('quantity')?.invalid">Must be > 0</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Price</mat-label>
                <input matInput type="number" formControlName="price" placeholder="150.00" step="0.01">
                <mat-error *ngIf="form.get('price')?.invalid">Must be > 0</mat-error>
              </mat-form-field>
            </div>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit"
                      [disabled]="form.invalid || loading" class="submit-btn">
                <mat-icon>send</mat-icon>
                {{ loading ? 'Submitting...' : 'Submit Order' }}
              </button>
              <button mat-button type="button" (click)="reset()">Reset</button>
            </div>
          </form>

          <div *ngIf="lastOrder" class="confirmation">
            <mat-icon color="accent">check_circle</mat-icon>
            <span>Order submitted: <strong>{{ lastOrder.orderId }}</strong> — {{ lastOrder.status }}</span>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .order-entry-page { max-width:760px; margin:0 auto; }
    .page-header h1 { color:#e2e8f0; font-size:1.6rem; margin:0 0 4px; }
    .sub { color:#718096; font-size:.88rem; }
    .entry-card { background:#111827; border:1px solid rgba(99,179,237,.15); border-radius:12px; margin-top:20px; }
    .order-form { padding:8px 0; }
    .form-row { display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:8px; }
    mat-form-field { width:100%; }
    .form-actions { display:flex; gap:12px; align-items:center; margin-top:12px; }
    .submit-btn { background:linear-gradient(135deg,#2b6cb0,#553c9a); color:#fff; padding:0 24px;
                  height:44px; border-radius:8px; display:flex; align-items:center; gap:8px; }
    .confirmation { display:flex; align-items:center; gap:8px; margin-top:16px; padding:12px;
                    background:rgba(104,211,145,.1); border-radius:8px; color:#68d391; }
    @media(max-width:600px) { .form-row { grid-template-columns:1fr; } }
  `]
})
export class OrderEntryComponent {
  symbols = ['AAPL','MSFT','GOOGL','AMZN','TSLA','META','NVDA','JPM','GS','BAC'];
  form: FormGroup;
  loading = false;
  lastOrder: any = null;

  constructor(private fb: FormBuilder,
              private orderService: OrderService,
              private snack: MatSnackBar) {
    this.form = this.fb.group({
      clOrdId:   ['', Validators.required],
      symbol:    ['AAPL', Validators.required],
      side:      ['BUY', Validators.required],
      orderType: ['LIMIT', Validators.required],
      quantity:  [100, [Validators.required, Validators.min(1)]],
      price:     [150.00, [Validators.required, Validators.min(0.0001)]]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const req: OrderRequest = this.form.value;
    this.orderService.submitOrder(req).subscribe({
      next: order => {
        this.lastOrder = order;
        this.snack.open(`Order ${order.orderId} submitted — ${order.status}`, 'OK', { duration: 4000 });
        this.loading = false;
      },
      error: err => {
        this.snack.open('Order failed: ' + (err.error?.message || err.message), 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  reset(): void { this.form.reset({ symbol:'AAPL', side:'BUY', orderType:'LIMIT', quantity:100, price:150 }); this.lastOrder = null; }
}
