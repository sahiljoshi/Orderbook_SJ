package orderbook;

import orderbook.order.Order;

import java.util.ArrayList;


public class OrderResponse {
    public ArrayList<Trade> trades;
    public ArrayList<Order> matchedOrders;
    public Order order;
    boolean isSuccess;

    public OrderResponse(ArrayList<Trade> trades, ArrayList<Order> matchedOrders, Order order, boolean isSuccess) {
        this.trades = trades;
        this.matchedOrders = matchedOrders;
        this.isSuccess = isSuccess;
        this.order = order;
    }

    public OrderResponse() {
        isSuccess = false;
        trades = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "trades=" + trades.stream().toString()
                + "matchedOrders " + this.matchedOrders.stream().toString()
                + ", order=" + order +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
