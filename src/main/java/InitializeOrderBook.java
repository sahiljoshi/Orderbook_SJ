import db.DbHandler;
import orderbook.enums.OrderCategory;
import orderbook.order.Order;
import orderbook.enums.OrderSide;
import orderbook.enums.OrderState;
import orderbook.enums.OrderType;
import orderbook.security.Security;
import requesthandler.OrderHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public class InitializeOrderBook {


    public static void main(String[] args) {
        // Load properties
        Properties prop = new Properties();
        String fileName = "./config/orderbook.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        // Load securities
        boolean isStartClean = Boolean.parseBoolean(prop.getProperty("startClean"));
        String dbFile = prop.getProperty("dbFileName");
        if (isStartClean) {
            File f = new File(dbFile);
            f.delete();
        }
        ArrayList<Security> securities = new ArrayList<>();
        try {
            Security sec1 = new Security(prop.getProperty("security1.Name"), prop.getProperty("security1.PaymentPrecision"), prop.getProperty("security1.BasePrecision"));
            securities.add(sec1);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Invalid cofig for security 1");
            return;
        }
        // load DB handler
        DbHandler dbHandler = DbHandler.getInstance(dbFile);
        try {
            dbHandler.InitializeDB();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("DB handler initiated");
        // initialize order handler
        OrderHandler oh = new OrderHandler(securities, dbHandler);
        BigDecimal price = new BigDecimal("100.23");
        BigDecimal qty = new BigDecimal("5.679");
        Order buyOrder = new Order(securities.get(0), OrderType.LimitOrder, price, qty, OrderSide.BuySide, OrderState.OPEN, OrderCategory.Normal);
        oh.AddOrder(buyOrder);

        price = new BigDecimal("100.25");
        qty = new BigDecimal("5.679");
        Order sellOrder = new Order(securities.get(0), OrderType.LimitOrder, price, qty, OrderSide.SellSide, OrderState.OPEN, OrderCategory.Normal);
        oh.AddOrder(sellOrder);


//        price = new BigDecimal("100.24");
//        qty = new BigDecimal("5.679");
//        Order buyOrder2 = new Order(securities.get(0), OrderType.LimitOrder, price, qty, OrderSide.BuySide, OrderState.OPEN, OrderCategory.Normal);
//        oh.AddOrder(buyOrder2);
//
//
//        price = new BigDecimal("100.24");
//        qty = new BigDecimal("2.679");
//        Order sellOrder2 = new Order(securities.get(0), OrderType.LimitOrder, price, qty, OrderSide.SellSide, OrderState.OPEN, OrderCategory.Normal);
//        oh.AddOrder(sellOrder2);
//
//
//        price = new BigDecimal("100.25");
//        qty = new BigDecimal("6.679");
//        Order buyOrder3 = new Order(securities.get(0), OrderType.LimitOrder, price, qty, OrderSide.BuySide, OrderState.OPEN, OrderCategory.Normal);
//        oh.AddOrder(buyOrder3);
//
//        oh.removeOrder(sellOrder2);
//        oh.removeOrder(buyOrder2);
//        oh.removeOrder(buyOrder3);
//        oh.removeOrder(buyOrder);

        System.out.println("Market orders");


        price = new BigDecimal("101.25");
        qty = new BigDecimal("5.679");
        Order sellOrder4 = new Order(securities.get(0), OrderType.LimitOrder, price, qty, OrderSide.SellSide, OrderState.OPEN, OrderCategory.Normal);
        oh.AddOrder(sellOrder4);


        qty = new BigDecimal("6.679");
        Order buyOrder4 = new Order(securities.get(0), OrderType.MarketOrder, null, qty, OrderSide.BuySide, OrderState.OPEN, OrderCategory.Normal);
        oh.AddOrder(buyOrder4);


        qty = new BigDecimal("2.679");
        Order sellOrdeR5 = new Order(securities.get(0), OrderType.MarketOrder, null, qty, OrderSide.SellSide, OrderState.OPEN, OrderCategory.Normal);
        oh.AddOrder(sellOrdeR5);

        qty = new BigDecimal("6.679");
        Order sellOrdeR6 = new Order(securities.get(0), OrderType.MarketOrder, null, qty, OrderSide.SellSide, OrderState.OPEN, OrderCategory.Normal);
        oh.AddOrder(sellOrdeR6);

        Map m = oh.GetOrderBookDetails(securities.get(0));
        m.forEach((k, v) -> System.out.println(k + " : " + v));


    }
}
