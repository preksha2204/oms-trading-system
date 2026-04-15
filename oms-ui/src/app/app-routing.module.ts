import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { OrderEntryComponent } from './pages/order-entry/order-entry.component';
import { MarketDataPageComponent } from './pages/market-data/market-data.component';

const routes: Routes = [
  { path: '',            redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard',  component: DashboardComponent },
  { path: 'order-entry',component: OrderEntryComponent },
  { path: 'market-data',component: MarketDataPageComponent },
  { path: '**',         redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
