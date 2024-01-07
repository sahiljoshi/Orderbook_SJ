package orderbook;

import custom.errors.IllegalOrderState;
import orderbook.enums.OrderState;
import orderbook.order.Order;

import java.math.BigDecimal;
import java.util.ArrayList;


/*
List of all the order present at a particular price.
 */
public class OrderPriceList {

    private ArrayList<Order> orderList = new ArrayList<>();
    private BigDecimal volume = BigDecimal.ZERO; // helps get the volume at each level

    public int getOrdersCount() {
        return this.orderList.size();
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    public Order getOrderByPosition(int index) {
        return orderList.get(index);
    }


    public boolean removeOrder(Order o, OrderState newState) throws IllegalOrderState {
        boolean success = orderList.remove(o);
        if (success) {
            volume = volume.add(o.getOpenQuantity().negate());
            if (volume.compareTo(BigDecimal.ZERO) == -1) {
                throw new IllegalOrderState();
            }
            o.setState(newState);
        }
        return success;
    }

    public void addOrder(Order o) {
        System.out.println("Order Price list : addOrder " + o.toStringPartial());
        orderList.add(o);
        volume = volume.add(o.getOpenQuantity());
    }


    public void updateExecutedQty(BigDecimal qty, int orderID, int index) {
        Order order = orderList.get(index);
        if (order.getOrderID() != orderID) {
            System.out.println("Invalid order state");
            System.exit(-1);
        }
        order.updateExecutedQty(qty);
        if (order.getState() == OrderState.EXECUTED) {
            orderList.remove(index);
        }
        System.out.println("Order Price list : update qty  " + order.toString());
        volume = volume.add(qty.negate());
    }
}
