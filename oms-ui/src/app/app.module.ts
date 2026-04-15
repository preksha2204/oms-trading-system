import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { OrderEntryComponent } from './pages/order-entry/order-entry.component';
import { MarketDataPageComponent } from './pages/market-data/market-data.component';
import { OrderGridComponent } from './shared/components/order-grid/order-grid.component';
import { TradeBlotterComponent } from './shared/components/trade-blotter/trade-blotter.component';
import { OptionsPanelComponent } from './shared/components/options-panel/options-panel.component';
import { StatusBarComponent } from './shared/components/status-bar/status-bar.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    OrderEntryComponent,
    MarketDataPageComponent,
    OrderGridComponent,
    TradeBlotterComponent,
    OptionsPanelComponent,
    StatusBarComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    AppRoutingModule,
    MatToolbarModule, MatCardModule, MatTableModule, MatPaginatorModule,
    MatSortModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatChipsModule, MatBadgeModule, MatIconModule,
    MatSnackBarModule, MatProgressSpinnerModule, MatTooltipModule, MatDividerModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
