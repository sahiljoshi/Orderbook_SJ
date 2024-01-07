package orderbook;

import orderbook.order.Order;

import java.util.ArrayList;


public class OrderResponse {
    public ArrayList<Trade> trades;
    Order order;
    boolean isSuccess;

    public OrderResponse(ArrayList<Trade> trades, Order order, boolean isSuccess) {
        this.trades = trades;
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
                + ", order=" + order +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
