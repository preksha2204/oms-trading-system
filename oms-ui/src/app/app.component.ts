import { Component, OnInit } from '@angular/core';
import { WebSocketService } from './core/services/websocket.service';

@Component({
  selector: 'app-root',
  template: `
    <div class="app-shell">
      <mat-toolbar class="app-toolbar" color="primary">
        <span class="logo">⚡ OMS Trading</span>
        <span class="spacer"></span>
        <app-status-bar></app-status-bar>
        <nav class="nav-links">
          <a routerLink="/dashboard"   routerLinkActive="active">Dashboard</a>
          <a routerLink="/order-entry" routerLinkActive="active">Order Entry</a>
          <a routerLink="/market-data" routerLinkActive="active">Market Data</a>
        </nav>
      </mat-toolbar>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-shell { display:flex; flex-direction:column; min-height:100vh; background:#0a0e1a; }
    .app-toolbar { background: linear-gradient(135deg,#0d1b2a 0%,#1a2744 100%);
                   border-bottom:1px solid rgba(99,179,237,.2); }
    .logo { font-size:1.4rem; font-weight:700; letter-spacing:.05em;
            background:linear-gradient(90deg,#63b3ed,#9f7aea); -webkit-background-clip:text;
            -webkit-text-fill-color:transparent; }
    .spacer { flex:1 1 auto; }
    .nav-links a { color:rgba(255,255,255,.7); text-decoration:none; margin-left:24px;
                   font-size:.9rem; letter-spacing:.05em; transition:color .2s;
                   padding:6px 12px; border-radius:6px; }
    .nav-links a.active, .nav-links a:hover { color:#fff; background:rgba(99,179,237,.15); }
    .main-content { flex:1; padding:24px; overflow:auto; }
  `]
})
export class AppComponent implements OnInit {
  constructor(private wsService: WebSocketService) {}
  ngOnInit(): void { this.wsService.connect(); }
}
