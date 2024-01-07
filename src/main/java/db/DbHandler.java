package db;

import orderbook.Trade;
import orderbook.order.Order;

import java.sql.*;
import java.time.LocalDateTime;


public class DbHandler {
    private static DbHandler dbHandler = null;
    String url;
    String fileName;
    Connection conn;

    private DbHandler(String fileName) {
        this.fileName = fileName;
        String url = "jdbc:sqlite:./" + fileName;
        this.url = url;
        System.out.println("Setting connection");
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                conn.setAutoCommit(false);
                System.out.println("Setting connection");
                this.conn = conn;
            }
            System.out.println("Setting connection complete");


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static DbHandler getInstance(String fileName) {
        if (dbHandler == null) {
            synchronized (DbHandler.class) {
                if (dbHandler == null) {
                    dbHandler = new DbHandler(fileName);
                }
            }
        }
        return dbHandler;
    }


    public void InitializeDB() throws SQLException {
        connect();
        InitialzieOrderTable(conn);
        InitialzieTradeTable(conn);

    }

    private void connect() {
        try {
            this.conn = DriverManager.getConnection(this.url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private void InitialzieOrderTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS orders (
                	Id  integer PRIMARY KEY,
                	market  varchar (24) NOT NULL,
                	side  varchar (24) NOT NULL,
                 order_state  VARCHAR(255) ,\s
                	order_type   VARCHAR(255) ,
                	order_category   VARCHAR(255),\s
                	price DECIMAL(100 , 10 ) ,\s
                	quantity DECIMAL(100 , 10 )  NOT NULL ,\s
                	open_quantity DECIMAL(100 , 10 ) NOT NULL  ,
                	executed_quantity DECIMAL(100 , 10 ) NOT NULL  ,
                	parent_order_id int ,\s
                	created_at   time , \s
                	updated_at   time \s
                );""";


        Statement stmt = conn.createStatement();
        // create a new table
        stmt.execute(sql);
    }


    public Order insertOrderToDB(Order order) throws SQLException {
        String sql = "INSERT INTO orders ( "
                + "market  ,side , order_state , order_type , order_category , price, " +
                " quantity, open_quantity, executed_quantity, parent_order_id, created_at, updated_at ) " +
                "VALUES( ? , ? , ? , ? , ? , ?," +
                " ? , ? , ? , ? , ? , ? )";
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, order.getSecurity().getMarketPair());
        pstmt.setString(2, order.getOrderSide().toString());
        pstmt.setString(3, order.getState().toString());
        pstmt.setString(4, order.getOrderType().toString());
        pstmt.setString(5, order.getCategory().toString());
        if (order.isLimitOrder()) {
            pstmt.setString(6, order.getPrice().toString());
        } else {
            pstmt.setString(6, null);
        }

        pstmt.setDouble(7, order.getQuantity().floatValue());
        ;
        pstmt.setDouble(8, order.getOpenQuantity().floatValue());
        pstmt.setDouble(9, order.getExecutedQuantity().floatValue());
        pstmt.setInt(10, order.getParentOrderID());
        pstmt.setTime(11, Time.valueOf(LocalDateTime.now().toLocalTime()));
        pstmt.setTime(12, Time.valueOf(LocalDateTime.now().toLocalTime()));
        this.connect();
        pstmt.execute();
        pstmt.close();
        // SQLlite issue with JDBC .not returning  insert ID. Hacking through last ID
        String query = "SELECT MAX(ID) AS LAST FROM orders";
        PreparedStatement pst1 = conn.prepareStatement(query);
        ResultSet rs1 = pst1.executeQuery();
        int orderid = Integer.parseInt(rs1.getString("LAST"));
        pst1.close();

        order.setOrderID(orderid);
        return order;
    }


    public Order UpdateOrderToDB(Order order) throws SQLException {
        String sql = "UPDATE  orders SET   "
                + "  order_state = ?  ,  " +
                "  open_quantity = ? , executed_quantity = ? ,  updated_at = ?   " +
                " WHERE id = ? ";
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, order.getState().toString());
        pstmt.setDouble(2, order.getOpenQuantity().floatValue());
        pstmt.setDouble(3, order.getExecutedQuantity().floatValue());
        pstmt.setTime(4, Time.valueOf(LocalDateTime.now().toLocalTime()));
        pstmt.setInt(5, order.getOrderID());
        this.connect();
        pstmt.execute();
        pstmt.close();
        return order;
    }


    private void InitialzieTradeTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS trades (\n"
                + "	ID  INTEGER PRIMARY KEY AUTOINCREMENT ,\n"
                + "	market  varchar (24) NOT NULL,\n"
                + "	buy_order_id  int NOT NULL,\n"
                + "	sell_order_id  int NOT NULL,\n"
                + " is_buyer_taker  bool NOT NULL  , \n"
                + "	price DECIMAL(100 , 10 ) , \n"
                + "	quantity DECIMAL(100 , 10 )  NOT NULL , \n"
                + "	created_at   time ,  \n"
                + "	updated_at   time  \n"
                + ");";

        Statement stmt = conn.createStatement();
        // create a new table
        stmt.execute(sql);
    }

    public Trade insertTradeToDB(Trade trade) throws SQLException {
        String sql = "INSERT INTO trades ( "
                + "market  ,buy_order_id , sell_order_id , is_buyer_taker  , price, quantity, " +
                " created_at, updated_at ) " +
                "VALUES( ? , ? , ? , ? , ? , ?, ? , ? )";
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, trade.getBuyOrder().getSecurity().getMarketPair());
        pstmt.setInt(2, trade.getBuyOrder().getOrderID());
        pstmt.setInt(3, trade.getSellOrder().getOrderID());
        pstmt.setBoolean(4, trade.isBuyerTaker());
        pstmt.setDouble(5, trade.getExecutionPrice().floatValue());
        pstmt.setDouble(6, trade.getExecutedQty().floatValue());

        pstmt.setTime(7, Time.valueOf(LocalDateTime.now().toLocalTime()));
        pstmt.setTime(8, Time.valueOf(LocalDateTime.now().toLocalTime()));
        this.connect();
        pstmt.execute();
        pstmt.close();
        // SQLlite issue with JDBC .not returning  insert ID. Hacking through last ID
        String query = "SELECT MAX(ID) AS LAST FROM trades";
        PreparedStatement pst1 = conn.prepareStatement(query);
        ResultSet rs1 = pst1.executeQuery();
        int tradeID = Integer.parseInt(rs1.getString("LAST"));
        pst1.close();

        trade.setTradeId(tradeID);
        return trade;
    }
}
