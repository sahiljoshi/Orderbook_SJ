package orderbook.security;

public class Security {
    private final String marketPair;
    private final int paymentPrecision;
    private final int tokenPrecision;


    public Security(String marketPair, String paymentPrecision, String tokenPrecision) throws NumberFormatException {
        this.marketPair = marketPair;
        this.paymentPrecision = Integer.parseInt(paymentPrecision);
        this.tokenPrecision = Integer.parseInt(tokenPrecision);
    }

    public String getMarketPair() {
        return marketPair;
    }

    public int getPaymentPrecision() {
        return paymentPrecision;
    }

    public int getTokenPrecision() {
        return tokenPrecision;
    }
}
