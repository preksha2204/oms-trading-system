import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject, BehaviorSubject } from 'rxjs';
import { Order, Trade, MarketData, OptionPrice } from '../models/models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  connected$ = new BehaviorSubject<boolean>(false);
  orders$    = new Subject<Order>();
  trades$    = new Subject<Trade>();
  marketData$ = new Subject<MarketData>();
  options$   = new Subject<OptionPrice>();

  private client!: Client;
  private subscriptions: StompSubscription[] = [];

  connect(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl) as any,
      reconnectDelay: 5000,
      onConnect: () => {
        this.connected$.next(true);
        this.subscriptions.push(
          this.client.subscribe('/topic/orders', (msg: IMessage) =>
            this.orders$.next(JSON.parse(msg.body))),
          this.client.subscribe('/topic/trades', (msg: IMessage) =>
            this.trades$.next(JSON.parse(msg.body))),
          this.client.subscribe('/topic/market-data', (msg: IMessage) =>
            this.marketData$.next(JSON.parse(msg.body))),
          this.client.subscribe('/topic/options', (msg: IMessage) =>
            this.options$.next(JSON.parse(msg.body)))
        );
      },
      onDisconnect: () => this.connected$.next(false),
      onStompError: (frame) => console.error('STOMP error', frame)
    });
    this.client.activate();
  }

  disconnect(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.client?.deactivate();
    this.connected$.next(false);
  }

  ngOnDestroy(): void { this.disconnect(); }
}
