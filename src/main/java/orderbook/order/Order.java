package orderbook.order;

import orderbook.order.OrderState;
import orderbook.order.OrderType;
import orderbook.security.Security;

import java.math.BigDecimal;

public class Order {
    private int orderID ;
    private final OrderType orderType ;
    private  final Security security ;
    private final  BigDecimal price ; // no amends
    private final BigDecimal quantity ;
    private OrderState state ;
    private BigDecimal openQuantity ;
    private BigDecimal executedQuantity;
    private final int parentOrderID ;


    public Order(OrderType orderType, Security security, BigDecimal price, BigDecimal quantity) {
        this.orderType = orderType;
        this.security = security;
        this.price = price;
        this.quantity = quantity;
        this.state = OrderState.open ;
        this.parentOrderID = 0 ;
    }

    private Order(OrderType orderType, Security security, BigDecimal price, BigDecimal quantity ,  int parentOrderID ) {
        this.orderType = orderType;
        this.security = security;
        this.price = price;
        this.quantity = quantity;
        this.state = OrderState.open ;
        this.parentOrderID = parentOrderID ;
    }

    public  Order CreateChildOrder(  BigDecimal quantity , BigDecimal price ) {
         Order order = new Order(OrderType.LimitOrder ,this.security, this.price, this.quantity , this.parentOrderID) ;
         return  order ;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Security getSecurity() {
        return security;
    }

    public  BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public OrderState getState() {
        return state;
    }

    public BigDecimal getOpenQuantity() {
        return openQuantity;
    }

    public BigDecimal getExecutedQuantity() {
        return executedQuantity;
    }
    public  void setOpenQuantity(BigDecimal openQuantity) {
        this.openQuantity = openQuantity;
    }

    public  void setExecutedQuantity(BigDecimal executedQuantity) {
        this.executedQuantity = executedQuantity;
    }






}
