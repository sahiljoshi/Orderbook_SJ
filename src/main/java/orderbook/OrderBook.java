package orderbook;

import custom.errors.IllegalOrderState;
import orderbook.order.Order;
import orderbook.enums.OrderSide;
import orderbook.security.Security;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/*
Order book is developed per order and supposed run in syncronized manner.
*/
public class OrderBook {
    private final Security security;
    private OrderSideBook bidBook;
    private OrderSideBook askBook;
    private int lastTradeSide;
    private ArrayList<Trade> lastTrades;  // useful if we want to keep responsibility of trade publishing to order book.


    /// how to make it thread safe ?

    public OrderBook(Security security) {
        this.security = security;
        this.bidBook = new OrderSideBook();
        this.askBook = new OrderSideBook();
    }


    public void addOrder(Order o) {

    }

    public boolean removeOrder(Order o) throws IllegalOrderState {
        if (o.getOrderSide() == OrderSide.BuySide) {
            return this.bidBook.removeOrderByID(o.getOrderID());
        } else {
            return this.askBook.removeOrderByID(o.getOrderID());
        }
    }


    public OrderResponse processOrder(Order order) {
        boolean isLimit = order.isLimitOrder();
        OrderResponse oReport = new OrderResponse();
        // Update time
        //this.time = order.getTimestamp();
        if (order.getQuantity().doubleValue() < 1E-6) {
            throw new IllegalArgumentException("processOrder() given qty <= 0");
        }
        try {
            if (isLimit) {
                System.out.println("processing limit order");
                oReport = processLimitOrder(order);
            } else {
                oReport = processMarketOrder(order);

            }
        } catch (IllegalOrderState e) {
            return oReport;
        }

        // if qty remainging ?

        return oReport;
    }

    boolean isRemaingQty(BigDecimal d) {
        if (d.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        return false;
    }


    boolean isGreater(BigDecimal d1, BigDecimal d2) {
        if (d1.compareTo(d2) > 0) {
            return true;
        }
        return false;
    }

    private BigDecimal processOrderList(ArrayList<Trade> trades, OrderPriceList orders,
                                        BigDecimal qtyRemaining, Order incomingOrder
    ) throws IllegalOrderState {
        OrderSide side = incomingOrder.getOrderSide();
        Order buyer, seller;
        int takerId = incomingOrder.getOrderID();
        int currentIndex = 0;
        boolean isBuyerTaker = false;
        long time = System.currentTimeMillis();
        while ((orders.getOrdersCount() > 0) && (isRemaingQty(qtyRemaining))) {
            BigDecimal qtyTraded = BigDecimal.ZERO;
            Order bookOrder = orders.getOrderByPosition(currentIndex);
            if (isGreater(bookOrder.getOpenQuantity(), qtyRemaining)) {
                qtyTraded = qtyRemaining;
                if (side == OrderSide.SellSide) {
                    this.bidBook.updateExecutedOrderQty(qtyTraded,
                            bookOrder.getOrderID(), currentIndex);
                } else {
                    this.askBook.updateExecutedOrderQty(qtyTraded,
                            bookOrder.getOrderID(), currentIndex);
                }
                qtyRemaining = BigDecimal.ZERO;
            } else {
                qtyTraded = bookOrder.getQuantity();
                if (side == OrderSide.SellSide) {
                    this.bidBook.removeOrderByID(bookOrder.getOrderID());
                } else {
                    this.askBook.removeOrderByID(bookOrder.getOrderID());
                }
                qtyRemaining = qtyRemaining.add(qtyTraded.negate());
            }
            if (side == OrderSide.SellSide) {
                buyer = bookOrder;
                seller = incomingOrder;
                isBuyerTaker = false;
            } else {
                buyer = incomingOrder;
                seller = bookOrder;
                isBuyerTaker = true;
            }
            Trade trade = new Trade(buyer, seller, bookOrder.getPrice(), qtyTraded, isBuyerTaker, time);
            trades.add(trade);
            System.out.println(trade);

        }
        return qtyRemaining;
    }


    private OrderResponse processMarketOrder(Order incomingOrder) throws IllegalOrderState {
        ArrayList<Trade> trades = new ArrayList<Trade>();
        OrderSide side = incomingOrder.getOrderSide();
        BigDecimal remainingQty = incomingOrder.getQuantity();
        if (side == OrderSide.BuySide) {
            this.lastTradeSide = 1;
            while (isRemaingQty(remainingQty) && (this.askBook.getOpenOrderCount() > 0)) {
                OrderPriceList ordersAtBest = this.askBook.minPriceList();
                remainingQty = processOrderList(trades, ordersAtBest, remainingQty,
                        incomingOrder);
            }
        } else if (side == OrderSide.SellSide) {
            this.lastTradeSide = -1;
            while (isRemaingQty(remainingQty) && (this.bidBook.getOpenOrderCount() > 0)) {
                OrderPriceList ordersAtBest = this.bidBook.maxPriceList();
                remainingQty = processOrderList(trades, ordersAtBest, remainingQty,
                        incomingOrder);
            }
        } else {
            throw new IllegalArgumentException("order neither market nor limit: " +
                    side);
        }
        OrderResponse report = new OrderResponse(trades, incomingOrder, true);
        return report;
    }


    private OrderResponse processLimitOrder(Order incomingOrder) throws IllegalOrderState {
        boolean orderInBook = false;
        ArrayList<Trade> trades = new ArrayList<Trade>();
        OrderSide side = incomingOrder.getOrderSide();
        BigDecimal qtyRemaining = incomingOrder.getQuantity();
        BigDecimal price = incomingOrder.getPrice();

        if (side == OrderSide.BuySide) {
            System.out.println("processLimitOrder: buySide " + incomingOrder.getOrderID());
            this.lastTradeSide = 1;
            while (this.askBook.getOpenOrderCount() > 0 &&
                    isRemaingQty(qtyRemaining) &&
                    price.compareTo(askBook.bestAsk()) >= 0) {
                OrderPriceList ordersAtBest = askBook.minPriceList();
                qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
                        incomingOrder);
            }
            // If volume remains, add order to book
            if (isRemaingQty(qtyRemaining)) {
                incomingOrder.setOpenQuantity(qtyRemaining);
                this.bidBook.insertOrder(incomingOrder);
                orderInBook = true;
            } else {
                orderInBook = false;
            }
        } else if (side == OrderSide.SellSide) {
            System.out.println("processLimitOrder: sellSide " + incomingOrder.getOrderID());
            this.lastTradeSide = -1;
            while (this.bidBook.getOpenOrderCount() > 0 &&
                    isRemaingQty(qtyRemaining) &&
                    price.compareTo(bidBook.bestBid()) <= 0) {
                OrderPriceList ordersAtBest = bidBook.maxPriceList();
                qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
                        incomingOrder);
            }
            // If volume remains, add to book
            if (isRemaingQty(qtyRemaining)) {
                System.out.println("processLimitOrder: inserting to  " + incomingOrder.getOrderID());
                incomingOrder.setOpenQuantity(qtyRemaining);
                this.askBook.insertOrder(incomingOrder);
                orderInBook = true;

            } else {
                orderInBook = false;
            }
        } else {
            throw new IllegalArgumentException("order neither market nor limit: " +
                    side);
        }
        OrderResponse report = new OrderResponse(trades, incomingOrder, true);

        return report;
    }

    public Map<String, String> getBookDetails() {
        HashMap<String, String> hashMap = new HashMap<>();
        BigDecimal bidPrice = this.bidBook.bestBid();
        BigDecimal askPrice = this.askBook.bestAsk();
        if (bidPrice != null) {
            hashMap.put("BidPrice", bidPrice.toString());
            hashMap.put("BidQty", this.bidBook.getQtyForPrice(bidPrice).toString());
        }
        if (askPrice != null) {
            hashMap.put("AskPrice", askPrice.toString());
            hashMap.put("AskQty", this.askBook.getQtyForPrice(askPrice).toString());
        }

        return hashMap;
    }
}
