export interface Order {
  orderId: number;
  clOrdId: string;
  symbol: string;
  customerId: string;
  side: 'BUY' | 'SELL';
  orderType: 'LIMIT' | 'MARKET';
  quantity: number;
  filledQty: number;
  price: number;
  status: 'NEW' | 'PARTIAL' | 'FILLED' | 'CANCELLED' | 'REJECTED';
  receivedAt: string;
  isNew?: boolean;
}

export interface Trade {
  tradeId: string;
  buyOrderId: number;
  sellOrderId: number;
  symbol: string;
  quantity: number;
  price: number;
  execId: string;
  executedAt: string;
  isNew?: boolean;
}

export interface MarketData {
  symbol: string;
  bid: number;
  ask: number;
  last: number;
  open: number;
  high: number;
  low: number;
  volume: number;
  timestamp: string;
}

export interface OptionPrice {
  symbol: string;
  underlying: string;
  strikePrice: number;
  optionType: string;
  callPrice: number;
  putPrice: number;
  delta: number;
  gamma: number;
  vega: number;
  theta: number;
  calculatedAt: string;
}

export interface OrderRequest {
  clOrdId: string;
  symbol: string;
  side: string;
  orderType: string;
  quantity: number;
  price: number;
}
