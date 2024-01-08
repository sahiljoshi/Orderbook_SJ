package orderbook.enums;

public enum OrderState {
    OPEN,
    WAIT, // for ICEBERG ORDERS
    EXECUTED,
    CANCELLED,
    EXPIRED,
    TRIGGERED, // For bracket orders
}
