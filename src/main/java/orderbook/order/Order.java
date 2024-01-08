package orderbook.order;

import orderbook.enums.OrderCategory;
import orderbook.enums.OrderSide;
import orderbook.enums.OrderState;
import orderbook.enums.OrderType;
import orderbook.security.Security;

import java.math.BigDecimal;

public class Order {


    private int orderID;
    private final OrderType orderType;
    private final Security security;
    private final BigDecimal price; // no amends
    private final BigDecimal quantity;
    private OrderState state;
    protected OrderCategory category;
    private BigDecimal openQuantity;
    private BigDecimal executedQuantity;
    private final int parentOrderID;

    final OrderSide orderSide;


    public Order(Security security, OrderType orderType, BigDecimal price, BigDecimal quantity, OrderSide side) {
        this.orderType = orderType;
        this.security = security;
        this.price = price;
        this.quantity = quantity;
        this.state = OrderState.OPEN;
        this.parentOrderID = 0;
        this.orderSide = side;
    }

    private Order(OrderType orderType, Security security, BigDecimal price, BigDecimal quantity, OrderSide side, int parentOrderID) {
        this.orderType = orderType;
        this.security = security;
        this.price = price;
        this.quantity = quantity;
        this.state = OrderState.OPEN;
        this.parentOrderID = parentOrderID;
        this.orderSide = side;
        this.category = OrderCategory.Normal;
    }


    public Order(Security security, OrderType orderType, BigDecimal price, BigDecimal quantity, OrderSide orderSide, OrderState state, OrderCategory category) {

        this.orderType = orderType;
        this.security = security;
        this.price = price;
        this.quantity = quantity;
        this.state = state;
        this.openQuantity = quantity;
        this.executedQuantity = BigDecimal.ZERO;
        this.parentOrderID = 0;
        this.orderSide = orderSide;
        this.category = category;
    }


    public Order CreateChildOrder(BigDecimal quantity, BigDecimal price) {
        Order order = new Order(OrderType.LimitOrder, this.security, price, quantity, this.orderSide, this.parentOrderID);
        return order;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderCategory getCategory() {
        return category;
    }

    public Security getSecurity() {
        return security;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public OrderState getState() {
        return state;
    }


    public int getOrderID() {
        return orderID;
    }

    public int getParentOrderID() {
        return parentOrderID;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public BigDecimal getOpenQuantity() {
        return openQuantity;
    }

    public BigDecimal getExecutedQuantity() {
        return executedQuantity;
    }

    public void setOpenQuantity(BigDecimal openQuantity) {
        this.openQuantity = openQuantity;
    }

    public void setExecutedQuantity(BigDecimal executedQuantity) {
        this.executedQuantity = executedQuantity;
    }


    public boolean isLimitOrder() {
        return this.orderType == OrderType.LimitOrder;
    }


    public void updateExecutedQty(BigDecimal qty) {
        this.setOpenQuantity(openQuantity.add(qty.negate()));
        this.setExecutedQuantity(executedQuantity.add(qty));
        if (openQuantity.compareTo(BigDecimal.ZERO) < 1) {
            this.setState(OrderState.EXECUTED);
        }

    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID =" + orderID +
                ", orderType =" + orderType +
                ", security=" + security +
                ", price=" + price +
                ", quantity=" + quantity +
                ", state=" + state +
                ", openQuantity=" + openQuantity +
                ", executedQuantity=" + executedQuantity +
                ", parentOrderID=" + parentOrderID +
                ", orderSide=" + orderSide +
                '}';
    }


    public String toStringPartial() {
        return "Order{" +
                "orderID =" + orderID +
                ", orderType =" + orderType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", orderSide=" + orderSide +
                '}';
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public void setState(OrderState newState) {
        this.state = newState;
    }
}
