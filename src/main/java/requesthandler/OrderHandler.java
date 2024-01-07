package requesthandler;

import custom.errors.IllegalOrderState;
import db.DbHandler;
import orderbook.OrderBook;
import orderbook.OrderResponse;
import orderbook.Trade;
import orderbook.order.Order;
import orderbook.security.Security;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderHandler {

    private final ConcurrentMap<String, OrderBook> SecurityOrderBook;
    private final ArrayList<Security> securities;
    private final DbHandler dbHandler;

    private final static Logger LOGGER = Logger.getLogger(OrderHandler.class.getName());

    public OrderHandler(ArrayList<Security> securities, DbHandler dbHandler) {
        this.dbHandler = dbHandler;
        this.securities = securities;
        this.SecurityOrderBook = securities.stream().collect(Collectors.toConcurrentMap(Security::getMarketPair, OrderBook::new));
    }


    public int AddOrder(Order o) {
        try {
            inserOrderTODb(o);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to insert order to DB . returning. Not added to order book");
            e.printStackTrace();
            System.exit(-101);
        }
        OrderBook orderBook = SecurityOrderBook.get(o.getSecurity().getMarketPair());
        OrderResponse or = orderBook.processOrder(o);
        inserOrderResponseTODb(or);
        return o.getOrderID();
    }

    public boolean removeOrder(Order o) {
        try {
            OrderBook orderBook = SecurityOrderBook.get(o.getSecurity().getMarketPair());
            boolean result = orderBook.removeOrder(o);
        } catch (IllegalOrderState e) {
            return false;
        }
        UpdateOrderInDB(o);
        return true;

    }

    private void inserOrderResponseTODb(OrderResponse orderResponse) {
        orderResponse.trades.stream().forEach(this::InsertTradeToDB);
        orderResponse.matchedOrders.stream().forEach(this::UpdateOrderInDB);
        this.UpdateOrderInDB(orderResponse.order);
    }

    private void inserOrderTODb(Order order) throws SQLException {
        this.dbHandler.insertOrderToDB(order);
    }


    private void UpdateOrderInDB(Order o) {
        try {
            this.dbHandler.UpdateOrderToDB(o);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-101);
        }
    }


    private void InsertTradeToDB(Trade t) {
        try {
            this.dbHandler.insertTradeToDB(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public Map<String, String> GetOrderBookDetails(Security sec) {
        OrderBook orderBook = SecurityOrderBook.get(sec.getMarketPair());
        return orderBook.getBookDetails();

    }

    public Order getOrderDetails(Order o) {
        OrderBook orderBook = SecurityOrderBook.get(o.getSecurity().getMarketPair());
        return orderBook.getOrderDetails(o);

    }
}
