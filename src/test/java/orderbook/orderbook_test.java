package orderbook;

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
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class orderbook_test {

    OrderBook book;
    Security sec;
    DbHandler _dbHandler;
    final String BID_PRICE = "BidPrice";
    final String ASK_PRICE = "AskPrice";
    final String BID_QTY = "BidQty";
    final String ASK_QTY = "AskQty";

    @Before
    public void setup() {
        String file = "test";
        File f = new File(file);
        f.delete();
        _dbHandler = DbHandler.getInstance(file);
        try {
            _dbHandler.InitializeDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sec = new Security("XYZUSDT", "2", "3");
        book = new OrderBook(sec);
    }

    public Order createOrder(BigDecimal price, BigDecimal qty, OrderSide side, OrderType type) {
        Order secOrder = new Order(sec, type, price, qty, side, OrderState.OPEN, OrderCategory.Normal);
        try {
            secOrder = _dbHandler.insertOrderToDB(secOrder);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return secOrder;
    }

    @Test
    public void TestAddLimitOrder() {
        String TestName = "TestAddLimitOrder";
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        OrderSide side = OrderSide.BuySide;
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);

        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " price validation", or.order.getPrice(), price);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + " qty validation ", or.order.getQuantity(), qty);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        assertEquals(TestName + "trade count validation", or.trades.size(), 0);
    }


    @Test
    public void TestAddMarketOrder() {
        String TestName = "TestAddMarketOrder";
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        OrderSide side = OrderSide.BuySide;
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.MarketOrder);

        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " price validation", or.order.getPrice(), price);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + " qty validation ", or.order.getQuantity(), qty);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.EXPIRED);
        assertEquals(TestName + "trade count validation", or.trades.size(), 0);
    }


    @Test
    public void TestMatchLimitOrder() {
        String TestName = "TestMatchLimitOrder";
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        Order secOrder2 = createOrder(price, qty, OrderSide.SellSide, OrderType.LimitOrder);
        or = book.processOrder(secOrder2);
        assertEquals(TestName + " sell id validation ", 2, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXECUTED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("0.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 1, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "trade count  ", 1, or.trades.size());
        assertEquals(TestName + "trade count  ", qty, or.trades.get(0).getExecutedQty());
    }


    @Test
    public void TestMatchMarketOrder() {
        String TestName = "TestMatchMarketOrder";
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        Order secOrder2 = createOrder(price, qty, OrderSide.SellSide, OrderType.MarketOrder);
        or = book.processOrder(secOrder2);
        assertEquals(TestName + " sell id validation ", 2, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXECUTED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("0.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 1, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "trade count  ", 1, or.trades.size());
        assertEquals(TestName + "trade count  ", qty, or.trades.get(0).getExecutedQty());
    }


    @Test
    public void TestPartialMatchMarketOrder() {
        String TestName = "TestPartialMatchMarketOrder";
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        BigDecimal qty2 = new BigDecimal("2.01");
        Order secOrder2 = createOrder(price, qty2, OrderSide.SellSide, OrderType.MarketOrder);
        or = book.processOrder(secOrder2);
        assertEquals(TestName + " sell id validation ", 2, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXPIRED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", qty2.add(qty.negate()), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 1, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "trade count  ", 1, or.trades.size());
        assertEquals(TestName + "trade count  ", qty, or.trades.get(0).getExecutedQty());
    }


    @Test
    public void TestTakerPartialMatchLimitOrder() {
        String TestName = "TestPartialMatchMarketOrder";
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("1.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        BigDecimal qty2 = new BigDecimal("2.01");
        Order secOrder2 = createOrder(price, qty2, OrderSide.SellSide, OrderType.LimitOrder);
        or = book.processOrder(secOrder2);
        assertEquals(TestName + " sell id validation ", 2, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.OPEN, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", qty2.add(qty.negate()), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 1, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "trade count  ", 1, or.trades.size());
        assertEquals(TestName + "trade count  ", qty, or.trades.get(0).getExecutedQty());

        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", null, orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", secOrder2.getPrice().toString(), orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  ask Qty  ", qty2.add(qty.negate()).toString(), orderBookdDetails.get(ASK_QTY));
    }

    // TakerMakerMatch


    @Test
    public void TestMakerPartialMatchLimitOrder() {
        String TestName = "TestMakerPartialMatchLimitOrder";
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("2.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);

        BigDecimal qty2 = new BigDecimal("1.01");
        Order secOrder2 = createOrder(price, qty2, OrderSide.SellSide, OrderType.LimitOrder);
        or = book.processOrder(secOrder2);
        assertEquals(TestName + " sell id validation ", 2, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXECUTED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("0.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty2, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 1, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty2, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder id open qty    ", qty.add(qty2.negate()), or.matchedOrders.get(0).getOpenQuantity());

        assertEquals(TestName + "  matchedOrder id order state  validation   ", OrderState.OPEN, or.matchedOrders.get(0).getState());

        assertEquals(TestName + "trade count  ", 1, or.trades.size());
        assertEquals(TestName + "trade qty   ", qty2, or.trades.get(0).getExecutedQty());

        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", secOrder2.getPrice().toString(), orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", null, orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  bid Qty  ", qty.add(qty2.negate()).toString(), orderBookdDetails.get(BID_QTY));
    }


    // multipleOrderSameProceLevelMatch

    @Test
    public void TestMakerMultipleMatchLimitOrder() {
        String TestName = "TestMakerMultipleMatchLimitOrder";
        // order 1
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("2.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        // order 2

        Order secOrder2 = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or2 = book.processOrder(secOrder2);
        assertEquals(TestName + " id validation ", or2.order.getOrderID(), 2);
        assertEquals(TestName + "state validation ", or2.order.getState(), OrderState.OPEN);


        BigDecimal qty2 = new BigDecimal("3.02");
        Order secOrder3 = createOrder(price, qty2, OrderSide.SellSide, OrderType.LimitOrder);
        or = book.processOrder(secOrder3);

        assertEquals(TestName + " sell id validation ", 3, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXECUTED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("0.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty2, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 2, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(0).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder id order state  validation   ", OrderState.EXECUTED, or.matchedOrders.get(0).getState());


        assertEquals(TestName + "  matchedOrder2 id  ", 2, or.matchedOrders.get(1).getOrderID());
        assertEquals(TestName + "  matchedOrder2 id exeuctedQty   ", new BigDecimal("1.01"), or.matchedOrders.get(1).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder2 id open qty    ", new BigDecimal("1.00"), or.matchedOrders.get(1).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder2 id state    ", OrderState.OPEN, or.matchedOrders.get(1).getState());


        assertEquals(TestName + "trade count  ", 2, or.trades.size());
        assertEquals(TestName + "trade qty   ", qty, or.trades.get(0).getExecutedQty());
        assertEquals(TestName + "trade buyOrder ID    ", 1, or.trades.get(0).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade sellOrder ID    ", 3, or.trades.get(0).getSellOrder().getOrderID());

        assertEquals(TestName + "trade2 qty   ", new BigDecimal("1.01"), or.trades.get(1).getExecutedQty());
        assertEquals(TestName + "trade2 buyOrder ID    ", 2, or.trades.get(1).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade2 sellOrder ID    ", 3, or.trades.get(1).getSellOrder().getOrderID());

        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", secOrder2.getPrice().toString(), orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", null, orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  bid Qty  ", "1.00", orderBookdDetails.get(BID_QTY));
    }
    // multipleOrderMultiplePriceLevel Match;

    @Test
    public void TestMakerMultipleMatchMarketOrder() {
        String TestName = "TestMakerMultipleMatchMarketOrder";
        // order 1
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("2.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        // order 2

        Order secOrder2 = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or2 = book.processOrder(secOrder2);
        assertEquals(TestName + " id validation ", or2.order.getOrderID(), 2);
        assertEquals(TestName + "state validation ", or2.order.getState(), OrderState.OPEN);


        BigDecimal qty2 = new BigDecimal("3.02");
        Order secOrder3 = createOrder(price, qty2, OrderSide.SellSide, OrderType.MarketOrder);
        or = book.processOrder(secOrder3);

        assertEquals(TestName + " sell id validation ", 3, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXECUTED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("0.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty2, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 2, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(0).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder id order state  validation   ", OrderState.EXECUTED, or.matchedOrders.get(0).getState());


        assertEquals(TestName + "  matchedOrder2 id  ", 2, or.matchedOrders.get(1).getOrderID());
        assertEquals(TestName + "  matchedOrder2 id exeuctedQty   ", new BigDecimal("1.01"), or.matchedOrders.get(1).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder2 id open qty    ", new BigDecimal("1.00"), or.matchedOrders.get(1).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder2 id state    ", OrderState.OPEN, or.matchedOrders.get(1).getState());


        assertEquals(TestName + "trade count  ", 2, or.trades.size());
        assertEquals(TestName + "trade qty   ", qty, or.trades.get(0).getExecutedQty());
        assertEquals(TestName + "trade buyOrder ID    ", 1, or.trades.get(0).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade sellOrder ID    ", 3, or.trades.get(0).getSellOrder().getOrderID());

        assertEquals(TestName + "trade2 qty   ", new BigDecimal("1.01"), or.trades.get(1).getExecutedQty());
        assertEquals(TestName + "trade2 buyOrder ID    ", 2, or.trades.get(1).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade2 sellOrder ID    ", 3, or.trades.get(1).getSellOrder().getOrderID());

        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", secOrder2.getPrice().toString(), orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", null, orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  bid Qty  ", "1.00", orderBookdDetails.get(BID_QTY));
    }


    @Test
    public void TestMakerMultiplePriceMatchMarketOrder() {
        String TestName = "TestMakerMultiplePriceMatchMarketOrder";
        // order 1
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("2.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        // order 2

        BigDecimal price2 = new BigDecimal("99.02");
        Order secOrder2 = createOrder(price2, qty, OrderSide.BuySide, OrderType.LimitOrder);

        OrderReceipt or2 = book.processOrder(secOrder2);
        assertEquals(TestName + " id validation ", or2.order.getOrderID(), 2);
        assertEquals(TestName + "state validation ", or2.order.getState(), OrderState.OPEN);


        BigDecimal qty2 = new BigDecimal("3.02");
        Order secOrder3 = createOrder(price, qty2, OrderSide.SellSide, OrderType.MarketOrder);
        or = book.processOrder(secOrder3);

        assertEquals(TestName + " sell id validation ", 3, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXECUTED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("0.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", qty2, or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 2, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(0).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder id order state  validation   ", OrderState.EXECUTED, or.matchedOrders.get(0).getState());


        assertEquals(TestName + "  matchedOrder2 id  ", 2, or.matchedOrders.get(1).getOrderID());
        assertEquals(TestName + "  matchedOrder2 id exeuctedQty   ", new BigDecimal("1.01"), or.matchedOrders.get(1).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder2 id open qty    ", new BigDecimal("1.00"), or.matchedOrders.get(1).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder2 id state    ", OrderState.OPEN, or.matchedOrders.get(1).getState());


        assertEquals(TestName + "trade count  ", 2, or.trades.size());
        assertEquals(TestName + "trade qty   ", qty, or.trades.get(0).getExecutedQty());
        assertEquals(TestName + "trade buyOrder ID    ", 1, or.trades.get(0).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade sellOrder ID    ", 3, or.trades.get(0).getSellOrder().getOrderID());
        assertEquals(TestName + "trade price   ", price, or.trades.get(0).getExecutionPrice());


        assertEquals(TestName + "trade2 qty   ", new BigDecimal("1.01"), or.trades.get(1).getExecutedQty());
        assertEquals(TestName + "trade2 buyOrder ID    ", 2, or.trades.get(1).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade2 sellOrder ID    ", 3, or.trades.get(1).getSellOrder().getOrderID());
        assertEquals(TestName + "trade2 price   ", price2, or.trades.get(1).getExecutionPrice());


        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", secOrder2.getPrice().toString(), orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", null, orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  bid Qty  ", "1.00", orderBookdDetails.get(BID_QTY));
    }


    @Test
    public void TestMakerMultiplePriceMatchMarketOrder_CleanBook() {
        String TestName = "TestMakerMultiplePriceMatchMarketOrder_CleanBook";
        // order 1
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("2.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        // order 2

        BigDecimal price2 = new BigDecimal("99.02");
        Order secOrder2 = createOrder(price2, qty, OrderSide.BuySide, OrderType.LimitOrder);

        OrderReceipt or2 = book.processOrder(secOrder2);
        assertEquals(TestName + " id validation ", or2.order.getOrderID(), 2);
        assertEquals(TestName + "state validation ", or2.order.getState(), OrderState.OPEN);


        BigDecimal qty2 = new BigDecimal("6.02");
        Order secOrder3 = createOrder(price, qty2, OrderSide.SellSide, OrderType.MarketOrder);
        or = book.processOrder(secOrder3);

        assertEquals(TestName + " sell id validation ", 3, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXPIRED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("2.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", new BigDecimal("4.02"), or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 2, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(0).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder id order state  validation   ", OrderState.EXECUTED, or.matchedOrders.get(0).getState());


        assertEquals(TestName + "  matchedOrder2 id  ", 2, or.matchedOrders.get(1).getOrderID());
        assertEquals(TestName + "  matchedOrder2 id exeuctedQty   ", qty, or.matchedOrders.get(1).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder2 id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(1).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder2 id state    ", OrderState.EXECUTED, or.matchedOrders.get(1).getState());


        assertEquals(TestName + "trade count  ", 2, or.trades.size());
        assertEquals(TestName + "trade qty   ", qty, or.trades.get(0).getExecutedQty());
        assertEquals(TestName + "trade buyOrder ID    ", 1, or.trades.get(0).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade sellOrder ID    ", 3, or.trades.get(0).getSellOrder().getOrderID());
        assertEquals(TestName + "trade price   ", price, or.trades.get(0).getExecutionPrice());


        assertEquals(TestName + "trade2 qty   ", new BigDecimal("2.01"), or.trades.get(1).getExecutedQty());
        assertEquals(TestName + "trade2 buyOrder ID    ", 2, or.trades.get(1).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade2 sellOrder ID    ", 3, or.trades.get(1).getSellOrder().getOrderID());
        assertEquals(TestName + "trade2 price   ", price2, or.trades.get(1).getExecutionPrice());


        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", null, orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", null, orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  bid Qty  ", null, orderBookdDetails.get(BID_QTY));
    }


    @Test
    public void TestMakerMultiplePriceMatchLimitOrder_CleanBook() {
        String TestName = "TestMakerMultiplePriceMatchLimitOrder_CleanBook";
        // order 1
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("2.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        // order 2

        BigDecimal price2 = new BigDecimal("99.02");
        Order secOrder2 = createOrder(price2, qty, OrderSide.BuySide, OrderType.LimitOrder);

        OrderReceipt or2 = book.processOrder(secOrder2);
        assertEquals(TestName + " id validation ", or2.order.getOrderID(), 2);
        assertEquals(TestName + "state validation ", or2.order.getState(), OrderState.OPEN);


        BigDecimal qty2 = new BigDecimal("6.02");
        Order secOrder3 = createOrder(price2, qty2, OrderSide.SellSide, OrderType.LimitOrder);
        or = book.processOrder(secOrder3);

        assertEquals(TestName + " sell id validation ", 3, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.OPEN, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("2.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", new BigDecimal("4.02"), or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 2, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(0).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder id order state  validation   ", OrderState.EXECUTED, or.matchedOrders.get(0).getState());


        assertEquals(TestName + "  matchedOrder2 id  ", 2, or.matchedOrders.get(1).getOrderID());
        assertEquals(TestName + "  matchedOrder2 id exeuctedQty   ", qty, or.matchedOrders.get(1).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder2 id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(1).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder2 id state    ", OrderState.EXECUTED, or.matchedOrders.get(1).getState());


        assertEquals(TestName + "trade count  ", 2, or.trades.size());
        assertEquals(TestName + "trade qty   ", qty, or.trades.get(0).getExecutedQty());
        assertEquals(TestName + "trade buyOrder ID    ", 1, or.trades.get(0).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade sellOrder ID    ", 3, or.trades.get(0).getSellOrder().getOrderID());
        assertEquals(TestName + "trade price   ", price, or.trades.get(0).getExecutionPrice());


        assertEquals(TestName + "trade2 qty   ", new BigDecimal("2.01"), or.trades.get(1).getExecutedQty());
        assertEquals(TestName + "trade2 buyOrder ID    ", 2, or.trades.get(1).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade2 sellOrder ID    ", 3, or.trades.get(1).getSellOrder().getOrderID());
        assertEquals(TestName + "trade2 price   ", price2, or.trades.get(1).getExecutionPrice());


        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", null, orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", "99.02", orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  bid Qty  ", null, orderBookdDetails.get(BID_QTY));
        assertEquals(TestName + "  ask Qty  ", "2.00", orderBookdDetails.get(ASK_QTY));
    }


    @Test
    public void TestMakerMultiplePriceMatchLimitOrder() {
        String TestName = "TestMakerMultiplePriceMatchLimitOrder";
        // order 1
        BigDecimal price = new BigDecimal("100.02");
        BigDecimal qty = new BigDecimal("2.01");
        Order secOrder = createOrder(price, qty, OrderSide.BuySide, OrderType.LimitOrder);
        OrderReceipt or = book.processOrder(secOrder);
        assertEquals(TestName + " id validation ", or.order.getOrderID(), 1);
        assertEquals(TestName + "state validation ", or.order.getState(), OrderState.OPEN);
        // order 2

        BigDecimal price2 = new BigDecimal("99.02");
        Order secOrder2 = createOrder(price2, qty, OrderSide.BuySide, OrderType.LimitOrder);

        OrderReceipt or2 = book.processOrder(secOrder2);
        assertEquals(TestName + " id validation ", or2.order.getOrderID(), 2);
        assertEquals(TestName + "state validation ", or2.order.getState(), OrderState.OPEN);


        BigDecimal qty2 = new BigDecimal("3.02");
        Order secOrder3 = createOrder(price2, qty2, OrderSide.SellSide, OrderType.LimitOrder);
        or = book.processOrder(secOrder3);

        assertEquals(TestName + " sell id validation ", 3, or.order.getOrderID());
        assertEquals(TestName + " sell state validation ", OrderState.EXECUTED, or.order.getState());
        assertEquals(TestName + " sell openQty validation ", new BigDecimal("0.00"), or.order.getOpenQuantity());
        assertEquals(TestName + " sell executedQty  validation ", new BigDecimal("3.02"), or.order.getExecutedQuantity());

        assertEquals(TestName + "  matchedOrder count  ", 2, or.matchedOrders.size());
        assertEquals(TestName + "  matchedOrder id  ", 1, or.matchedOrders.get(0).getOrderID());
        assertEquals(TestName + "  matchedOrder id exeuctedQty   ", qty, or.matchedOrders.get(0).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder id open qty    ", new BigDecimal("0.00"), or.matchedOrders.get(0).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder id order state  validation   ", OrderState.EXECUTED, or.matchedOrders.get(0).getState());


        assertEquals(TestName + "  matchedOrder2 id  ", 2, or.matchedOrders.get(1).getOrderID());
        assertEquals(TestName + "  matchedOrder2 id exeuctedQty   ", new BigDecimal("1.01"), or.matchedOrders.get(1).getExecutedQuantity());
        assertEquals(TestName + "  matchedOrder2 id open qty    ", new BigDecimal("1.00"), or.matchedOrders.get(1).getOpenQuantity());
        assertEquals(TestName + "  matchedOrder2 id state    ", OrderState.OPEN, or.matchedOrders.get(1).getState());


        assertEquals(TestName + "trade count  ", 2, or.trades.size());
        assertEquals(TestName + "trade qty   ", qty, or.trades.get(0).getExecutedQty());
        assertEquals(TestName + "trade buyOrder ID    ", 1, or.trades.get(0).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade sellOrder ID    ", 3, or.trades.get(0).getSellOrder().getOrderID());
        assertEquals(TestName + "trade price   ", price, or.trades.get(0).getExecutionPrice());


        assertEquals(TestName + "trade2 qty   ", new BigDecimal("1.01"), or.trades.get(1).getExecutedQty());
        assertEquals(TestName + "trade2 buyOrder ID    ", 2, or.trades.get(1).getBuyOrder().getOrderID());
        assertEquals(TestName + "trade2 sellOrder ID    ", 3, or.trades.get(1).getSellOrder().getOrderID());
        assertEquals(TestName + "trade2 price   ", price2, or.trades.get(1).getExecutionPrice());
        
        Map<String, String> orderBookdDetails = book.getBookDetails();
        assertEquals(TestName + " Bid price ", "99.02", orderBookdDetails.get(BID_PRICE));
        assertEquals(TestName + " ask price ", null, orderBookdDetails.get(ASK_PRICE));
        assertEquals(TestName + "  bid Qty  ", "1.00", orderBookdDetails.get(BID_QTY));
        assertEquals(TestName + "  ask Qty  ", null, orderBookdDetails.get(ASK_QTY));
    }

}
