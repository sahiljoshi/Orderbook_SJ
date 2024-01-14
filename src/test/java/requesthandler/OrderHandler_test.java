package requesthandler;

import db.DbHandler;
import orderbook.enums.OrderCategory;
import orderbook.enums.OrderSide;
import orderbook.enums.OrderState;
import orderbook.enums.OrderType;
import orderbook.order.Order;
import orderbook.security.Security;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OrderHandler_test {


    private OrderHandler handler;
    Security security;
    Security security2;
    DbHandler _dbHandler;
    final String BID_PRICE = "BidPrice";
    final String ASK_PRICE = "AskPrice";
    final String BID_QTY = "BidQty";
    final String ASK_QTY = "AskQty";

    @Before
    public void setup() {
        security = new Security("ABCSGD", "2", "2");
        security2 = new Security("XYZUSD", "2", "2");
        ArrayList<Security> securities = new ArrayList();
        securities.add(security);
        securities.add(security2);
        String file = "test";
        File f = new File(file);
        f.delete();
        _dbHandler = DbHandler.getInstance(file);
        try {
            _dbHandler.InitializeDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        handler = new OrderHandler(securities, _dbHandler);
    }


    @Test
    public void AddSingleOrderBuy() {
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        OrderSide side = OrderSide.BuySide;
        Order secOrder = new Order(security, OrderType.LimitOrder, price, qty, side, OrderState.OPEN, OrderCategory.Normal, 1);
        handler.AddOrder(secOrder);
        Map<String, String> details = handler.GetOrderBookDetails(security);
        assertEquals("Singe buy Order  Test", price.toString(), details.get(BID_PRICE));
        Map<String, String> details2 = handler.GetOrderBookDetails(security2);
        assertEquals("Singe buy Order  Test", null, details2.get(BID_PRICE));
        Order o = handler.getOrderDetails(secOrder);
        assertEquals("Singe buy Order  Test State", OrderState.OPEN, o.getState());
    }

    @Test
    public void AddSingleMarketOrderBuy() {
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        OrderSide side = OrderSide.BuySide;
        Order secOrder = new Order(security, OrderType.MarketOrder, null, qty, side, OrderState.OPEN, OrderCategory.Normal, 1);
        handler.AddOrder(secOrder);
        Map<String, String> details = handler.GetOrderBookDetails(security);
        assertEquals("Singe buy Order  Test", null, details.get(BID_PRICE));
        Map<String, String> details2 = handler.GetOrderBookDetails(security2);
        assertEquals("Singe buy Order  Test", null, details2.get(BID_PRICE));
        Order o = handler.getOrderDetails(secOrder);
        assertEquals("Singe buy Order  Test State", null, o);
    }


    @Test
    public void AddSingleOrderBuyFor2Securities() {
        OrderSide side = OrderSide.BuySide;

        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        Order secOrder = new Order(security, OrderType.LimitOrder, price, qty, side, OrderState.OPEN, OrderCategory.Normal, 1);
        handler.AddOrder(secOrder);

        BigDecimal price2 = new BigDecimal("1000.02");
        BigDecimal qty2 = new BigDecimal("1.2");
        Order secOrder2 = new Order(security2, OrderType.LimitOrder, price2, qty2, side, OrderState.OPEN, OrderCategory.Normal, 2);
        handler.AddOrder(secOrder2);

        Map<String, String> details = handler.GetOrderBookDetails(security);
        assertEquals("Singe buy Order  Test", price.toString(), details.get(BID_PRICE));
        assertEquals("Singe buy Order  Test", qty.toString(), details.get(BID_QTY));
        Map<String, String> details2 = handler.GetOrderBookDetails(security2);
        assertEquals("Singe buy Order  Test", price2.toString(), details2.get(BID_PRICE));
        assertEquals("Singe buy Order  Test", qty2.toString(), details2.get(BID_QTY));

    }

    @Test
    public void verifySimpleMatch() {
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        OrderSide side = OrderSide.BuySide;
        Order secOrder = new Order(security, OrderType.LimitOrder, price, qty, side, OrderState.OPEN, OrderCategory.Normal, 1);
        handler.AddOrder(secOrder);
        Map<String, String> details = handler.GetOrderBookDetails(security);
        assertEquals("verifySimpleMatch", price.toString(), details.get(BID_PRICE));
        assertEquals("Singe buy Order  Test", qty.toString(), details.get(BID_QTY));
        //SellOrder
        Order secOrder_sell = new Order(security, OrderType.LimitOrder, price, qty, OrderSide.SellSide, OrderState.OPEN, OrderCategory.Normal, 2);
        int sell_order_id = handler.AddOrder(secOrder_sell);
        details = handler.GetOrderBookDetails(security);
        assertEquals("verifySimpleMatch", null, details.get(BID_PRICE));
        assertEquals("verifySimpleMatch", null, details.get(BID_QTY));
        assertEquals("Singe buy Order  Test", 2, sell_order_id);
    }


    @Test
    public void verifyMarketMatch() {
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        OrderSide side = OrderSide.BuySide;
        Order secOrder = new Order(security, OrderType.LimitOrder, price, qty, side, OrderState.OPEN, OrderCategory.Normal, 1);
        handler.AddOrder(secOrder);
        Map<String, String> details = handler.GetOrderBookDetails(security);
        assertEquals("verifySimpleMatch", price.toString(), details.get(BID_PRICE));
        assertEquals("Singe buy Order  Test", qty.toString(), details.get(BID_QTY));
        //SellOrder
        Order secOrder_sell = new Order(security, OrderType.MarketOrder, null, qty, OrderSide.SellSide, OrderState.OPEN, OrderCategory.Normal, 2);
        int sell_order_id = handler.AddOrder(secOrder_sell);
        details = handler.GetOrderBookDetails(security);
        assertEquals("verifySimpleMatch", null, details.get(BID_PRICE));
        assertEquals("verifySimpleMatch", null, details.get(BID_QTY));
        assertEquals("Singe buy Order  Test", 2, sell_order_id);
    }

}


