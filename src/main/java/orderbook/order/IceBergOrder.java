package orderbook.order;

import orderbook.enums.OrderCategory;
import orderbook.enums.OrderSide;
import orderbook.enums.OrderType;
import orderbook.security.Security;

import java.math.BigDecimal;
import java.util.ArrayList;

/*
Iceberg order are orders that sits at the exchange but outside the order book.
There by providing better  order priortiy to incoming orders.
When evera child  order is triggerd, Iceberg order will place a new order.
acts like a bracket order with bracket on volume and not on price.
 */
public class IceBergOrder extends Order {
    private int percentageIncrement;
    private ArrayList<Order> childOrders;

    public IceBergOrder(OrderType orderType, Security security, BigDecimal price, BigDecimal quantity, int percentageIncrement, OrderSide side, int account_id) {
        super(security, orderType, price, quantity, side, account_id);
        this.percentageIncrement = percentageIncrement;
        this.category = OrderCategory.IceBergOrder;
        childOrders = new ArrayList<>();
    }

    public void updateExecutedQty(BigDecimal newExecutedQty) {
        this.getExecutedQuantity().add(newExecutedQty);
    }

}
