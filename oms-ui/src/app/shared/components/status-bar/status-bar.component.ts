import { Component, OnInit } from '@angular/core';
import { WebSocketService } from '../../core/services/websocket.service';

@Component({
  selector: 'app-status-bar',
  template: `
    <div class="status-bar">
      <span [class]="connected ? 'dot connected' : 'dot disconnected'"></span>
      <span class="status-text">{{ connected ? 'Live' : 'Disconnected' }}</span>
    </div>
  `,
  styles: [`
    .status-bar { display:flex; align-items:center; gap:6px; }
    .dot { width:8px; height:8px; border-radius:50%; }
    .connected { background:#68d391; box-shadow:0 0 8px #68d391; animation:pulse 2s infinite; }
    .disconnected { background:#fc8181; }
    .status-text { font-size:.8rem; color:rgba(255,255,255,.7); }
    @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.5} }
  `]
})
export class StatusBarComponent implements OnInit {
  connected = false;
  constructor(private ws: WebSocketService) {}
  ngOnInit(): void { this.ws.connected$.subscribe(c => this.connected = c); }
}
