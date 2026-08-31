import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Represents the stock exchange / market.
 * Holds all tradeable stocks and simulates live price movements (market ticks).
 */
public class Market {

    private final Map<String, Stock> stocks;
    private final Random random;

    public Market() {
        this.stocks = new TreeMap<>();
        this.random = new Random();
        initializeDefaultStocks();
    }

    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol().toUpperCase().trim(), stock);
    }

    public Stock getStock(String symbol) {
        if (symbol == null) return null;
        return stocks.get(symbol.toUpperCase().trim());
    }

    public Collection<Stock> getAllStocks() {
        return Collections.unmodifiableCollection(stocks.values());
    }

    /**
     * Populates the market with initial sample stocks.
     */
    private void initializeDefaultStocks() {
        addStock(new Stock("TCS", "Tata Consultancy Services", 3850.00));
        addStock(new Stock("INFY", "Infosys Limited", 1620.00));
        addStock(new Stock("RELIANCE", "Reliance Industries Ltd", 2950.00));
        addStock(new Stock("HDFCBANK", "HDFC Bank Limited", 1540.00));
        addStock(new Stock("TATAMOTORS", "Tata Motors Limited", 980.00));
        addStock(new Stock("ITC", "ITC Limited", 430.00));
        addStock(new Stock("SBIN", "State Bank of India", 760.00));
        addStock(new Stock("BHARTIARTL", "Bharti Airtel Limited", 1320.00));
    }

    /**
     * Simulates a market tick by updating stock prices with a random fluctuation (-2.5% to +2.5%).
     * Returns the list of updated stocks.
     */
    public synchronized void simulateMarketTick() {
        for (Stock stock : stocks.values()) {
            // Random variation between -2.5% and +2.5%
            double changePercent = (random.nextDouble() * 5.0) - 2.5;
            double priceDelta = stock.getCurrentPrice() * (changePercent / 100.0);
            double newPrice = stock.getCurrentPrice() + priceDelta;
            stock.updatePrice(newPrice);
        }
    }
}
