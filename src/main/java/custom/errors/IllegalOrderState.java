package custom.errors;

public class IllegalOrderState extends Exception {
    public IllegalOrderState() {
        super("Invalid order book state");
    }
}



