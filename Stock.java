import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a publicly traded stock on the market.
 * Tracks ticker symbol, company name, current price, and historical prices.
 */
public class Stock {

    private final String symbol;
    private final String companyName;
    private double currentPrice;
    private double previousPrice;
    private final List<Double> priceHistory;

    public Stock(String symbol, String companyName, double initialPrice) {
        this.symbol = symbol.toUpperCase().trim();
        this.companyName = companyName;
        this.currentPrice = initialPrice;
        this.previousPrice = initialPrice;
        this.priceHistory = new ArrayList<>();
        this.priceHistory.add(initialPrice);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public List<Double> getPriceHistory() {
        return Collections.unmodifiableList(priceHistory);
    }

    /**
     * Updates the stock price (e.g. after a market tick) and records history.
     */
    public synchronized void updatePrice(double newPrice) {
        if (newPrice < 1.0) {
            newPrice = 1.0; // Minimum stock price floor
        }
        this.previousPrice = this.currentPrice;
        this.currentPrice = Math.round(newPrice * 100.0) / 100.0;
        this.priceHistory.add(this.currentPrice);
    }

    public double getPriceChange() {
        return currentPrice - previousPrice;
    }

    public double getPriceChangePercent() {
        if (previousPrice == 0) return 0.0;
        return ((currentPrice - previousPrice) / previousPrice) * 100.0;
    }

    @Override
    public String toString() {
        double change = getPriceChange();
        double pct = getPriceChangePercent();
        String sign = change >= 0 ? "+" : "";
        return String.format("%-10s | %-28s | Rs.%9.2f | %s%.2f (%s%.2f%%)",
                symbol, companyName, currentPrice, sign, change, sign, pct);
    }
}
