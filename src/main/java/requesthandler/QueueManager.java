package requesthandler;

import orderbook.OrderBook;
import orderbook.order.Order;
import orderbook.security.Security;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueManager {

    // this will kepp a map of all the queues that created and intitiate a relevant  Order book instance
    private final ConcurrentMap<String, OrderBook> SecurityOrderBook;
    private final ConcurrentMap<String, Queue<Order>> SecurityOrderBookQueues;
    Object objectToLock;


    public QueueManager() {
        SecurityOrderBookQueues = new ConcurrentHashMap<>();
        SecurityOrderBook = new ConcurrentHashMap<>();
    }

    public Queue createOrderBookAndQueue(Security security) {
        if (SecurityOrderBookQueues.containsKey(security.getMarketPair())) {
            return SecurityOrderBookQueues.get(security.getMarketPair());
        }
        synchronized (objectToLock) {
            OrderBook orderBook = new OrderBook(security);
            String queueName = orderBook.getQueueName();
            Queue<Order> orderQueue = new LinkedBlockingQueue<>(10000);
            if (!SecurityOrderBook.containsKey(security.getMarketPair())) {
                SecurityOrderBookQueues.put(security.getMarketPair(), orderQueue);
                SecurityOrderBook.put(security.getMarketPair(), orderBook);
            }
        }

        if (SecurityOrderBookQueues.containsKey(security.getMarketPair())) {
            return SecurityOrderBookQueues.get(security.getMarketPair());
        }
        return null;
    }

    public Queue getQueue(Security security) {
        if (SecurityOrderBookQueues.containsKey(security.getMarketPair())) {
            return SecurityOrderBookQueues.get(security.getMarketPair());
        }
        return null;
    }


}
