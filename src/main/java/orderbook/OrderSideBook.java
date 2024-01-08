package orderbook;

import custom.errors.IllegalOrderState;
import orderbook.enums.OrderState;
import orderbook.order.Order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Objets to maintain  particular  side book.
 */
public class OrderSideBook {
    private TreeMap<BigDecimal, OrderPriceList> priceOrderTree;
    private Map<BigDecimal, OrderPriceList> priceMap;
    private Map<Integer, Order> orderMap;
    int depth;
    int totalOrderCount = 0;


    Logger _logger = Logger.getLogger(OrderSideBook.class.getName());

    public OrderSideBook() {
        this.priceOrderTree = new TreeMap<>();
        this.priceMap = new HashMap<>();
        this.totalOrderCount = 0;
        this.orderMap = new HashMap<>();
    }


    public void createPrice(BigDecimal price) {
        depth += 1;
        OrderPriceList newList = new OrderPriceList();
        priceOrderTree.put(price, newList);
        priceMap.put(price, newList);
    }

    public void removePrice(BigDecimal price) {
        depth -= 1;
        priceOrderTree.remove(price);
        priceMap.remove(price);
    }

    public boolean priceExists(BigDecimal price) {
        return priceMap.containsKey(price);
    }

    public boolean orderExists(int orderId) {
        return orderMap.containsKey(orderId);
    }


    public void insertOrder(Order order) {
        int orderID = order.getOrderID();
        System.out.println("orderDetails :" + order.getOrderID());
        BigDecimal orderPrice = order.getPrice();

        totalOrderCount += 1;
        if (!priceExists(orderPrice)) {
            createPrice(orderPrice);
        }

        priceMap.get(orderPrice).addOrder(order);
        orderMap.put(orderID, order);
    }


    public int getOpenOrderCount() {
        return totalOrderCount;
    }


    public BigDecimal bestBid() {
        if (this.depth > 0) {
            return this.priceOrderTree.lastKey();
        } else {
            return null;
        }
    }

    public BigDecimal bestAsk() {
        if (this.depth > 0) {
            return this.priceOrderTree.firstKey();
        } else {
            return null;
        }
    }


    private OrderPriceList getPriceList(BigDecimal price) {
        return this.priceMap.get(price);
    }

    public OrderPriceList minPriceList() {
        if (this.depth > 0) {
            return this.getPriceList(bestAsk());
        } else {
            return null;
        }
    }


    public OrderPriceList maxPriceList() {
        if (this.depth > 0) {
            return this.getPriceList(bestBid());
        } else {
            return null;
        }
    }

    public void updateExecutedOrderQty(BigDecimal qty, int orderID, int index, boolean isFull) {
        Order order = this.orderMap.get(orderID);
        BigDecimal originalVol = order.getOpenQuantity();
        OrderPriceList orderList = priceOrderTree.get(order.getPrice());

        orderList.updateExecutedQty(qty, orderID, index);
        if (isFull) {
            orderMap.remove(orderID);
            this.totalOrderCount--;
        }
        if (orderList.getOrdersCount() == 0) {
            removePrice(order.getPrice());
        }

//        this.volume += (order.getQuantity() - originalVol);
    }

    // this is to be adjusted to manage
    public boolean removeOrderByID(int id, OrderState newState) throws IllegalOrderState {
        this.totalOrderCount -= 1;
        Order order = orderMap.get(id);
        if (order == null) {
            System.out.println("Order " + id + " is not open");
            return false;
        }
        //this.volume -= order.getQuantity();
        OrderPriceList orders = priceMap.get(order.getPrice());
        if (orders == null || orders.getOrdersCount() == 0) {
            _logger.log(Level.WARNING, "Order count is zero for the price.Cant remove order ");
            return false;
        }
        boolean result = orders.removeOrder(order, newState);
        if (orders.getOrdersCount() == 0) {
            removePrice(order.getPrice());
        }
        this.orderMap.remove(id);

        return result;
    }

    public BigDecimal getQtyForPrice(BigDecimal price) {
        return this.priceOrderTree.get(price).getVolume();
    }

    public Order getOrderDetails(Order o) {
        if (orderExists(o.getOrderID())) {
            return orderMap.get(o.getOrderID());
        }
        return null;
    }
}
