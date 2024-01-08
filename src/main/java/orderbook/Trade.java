package orderbook;

import orderbook.order.Order;

import java.math.BigDecimal;

public class Trade {


    private int tradeId;
    private final Order buyOrder;
    private final Order SellOrder;
    private final boolean isBuyerTaker;
    private final BigDecimal executionPrice;
    private final BigDecimal executedQty;
    long time;

    public Trade(Order buyOrder, Order sellOrder, BigDecimal executionPrice, BigDecimal executedQty, boolean isBuyerTaker, long time) {
        this.buyOrder = buyOrder;
        SellOrder = sellOrder;
        this.isBuyerTaker = isBuyerTaker;
        this.executionPrice = executionPrice;
        this.executedQty = executedQty;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "buyOrder ID =" + buyOrder.getOrderID() +
                ", SellOrderID =" + SellOrder.getOrderID() +
                ", isBuyerTaker=" + isBuyerTaker +
                ", executionPrice=" + executionPrice.toString() +
                ", executedQty=" + executedQty.toString() +
                ", time=" + time +
                '}';
    }

    public Order getBuyOrder() {
        return buyOrder;
    }

    public Order getSellOrder() {
        return SellOrder;
    }

    public boolean isBuyerTaker() {
        return isBuyerTaker;
    }

    public BigDecimal getExecutionPrice() {
        return executionPrice;
    }

    public BigDecimal getExecutedQty() {
        return executedQty;
    }

    public long getTime() {
        return time;
    }

    public int getTradeId() {
        return tradeId;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }
}
