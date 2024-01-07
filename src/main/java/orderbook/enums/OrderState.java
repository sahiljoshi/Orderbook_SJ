package orderbook.enums;

public enum OrderState {
    OPEN,
    WAIT, // for ICEBERG ORDERS
    EXECUTED,
    CANCELLED,
    Expired,
    TRIGGERED, // For bracket orders
}
