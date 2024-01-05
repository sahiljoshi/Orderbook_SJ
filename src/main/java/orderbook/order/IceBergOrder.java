package orderbook.order;

import orderbook.security.Security;

import java.math.BigDecimal;
import java.util.ArrayList;

public class IceBergOrder  extends  Order{
    private int  percentageIncrement ;
    private ArrayList<Order> childOrders ;

    public IceBergOrder(OrderType orderType, Security security, BigDecimal price, BigDecimal quantity, int  percentageIncrement ) {
        super(orderType, security, price, quantity);
        this.percentageIncrement = percentageIncrement ;
    }

    public void updateExecutedQty(BigDecimal newExecutedQty) {
        this.getExecutedQuantity().add(newExecutedQty) ;
    }

}
