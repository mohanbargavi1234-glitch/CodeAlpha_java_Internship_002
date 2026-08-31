import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages a user's collection of stock holdings.
 * Computes portfolio valuation, total invested cost basis, and profit/loss.
 */
public class Portfolio {

    private final Map<String, Holding> holdings;

    public Portfolio() {
        this.holdings = new TreeMap<>(); // Sorted by symbol
    }

    public Map<String, Holding> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public Collection<Holding> getActiveHoldings() {
        return holdings.values().stream().filter(h -> h.getQuantity() > 0).toList();
    }

    public Holding getHolding(String symbol) {
        return holdings.get(symbol.toUpperCase().trim());
    }

    public int getSharesCount(String symbol) {
        Holding h = getHolding(symbol);
        return (h != null) ? h.getQuantity() : 0;
    }

    public void addHolding(Holding holding) {
        if (holding.getQuantity() > 0) {
            holdings.put(holding.getSymbol(), holding);
        }
    }

    /**
     * Executes a buy for the portfolio.
     */
    public void buy(String symbol, int quantity, double pricePerShare) {
        String sym = symbol.toUpperCase().trim();
        Holding h = holdings.computeIfAbsent(sym, s -> new Holding(s, 0, 0.0));
        h.addShares(quantity, pricePerShare);
    }

    /**
     * Executes a sell for the portfolio.
     * Returns true if enough shares existed and were sold.
     */
    public boolean sell(String symbol, int quantity) {
        String sym = symbol.toUpperCase().trim();
        Holding h = holdings.get(sym);
        if (h == null || h.getQuantity() < quantity) {
            return false;
        }
        h.removeShares(quantity);
        if (h.getQuantity() == 0) {
            holdings.remove(sym);
        }
        return true;
    }

    /**
     * Calculates total current market value of all holdings.
     */
    public double getCurrentValue(Market market) {
        double totalValue = 0.0;
        for (Holding h : holdings.values()) {
            if (h.getQuantity() <= 0) continue;
            Stock stock = market.getStock(h.getSymbol());
            if (stock != null) {
                totalValue += h.getQuantity() * stock.getCurrentPrice();
            } else {
                totalValue += h.getTotalCostBasis(); // Fallback to cost basis if stock delisted
            }
        }
        return totalValue;
    }

    /**
     * Calculates total invested amount (cost basis) across current holdings.
     */
    public double getTotalInvested() {
        double invested = 0.0;
        for (Holding h : holdings.values()) {
            invested += h.getTotalCostBasis();
        }
        return invested;
    }

    /**
     * Calculates unrealized profit/loss.
     */
    public double getProfitLoss(Market market) {
        return getCurrentValue(market) - getTotalInvested();
    }

    public double getProfitLossPercent(Market market) {
        double invested = getTotalInvested();
        if (invested == 0) return 0.0;
        return (getProfitLoss(market) / invested) * 100.0;
    }

    public boolean isEmpty() {
        return holdings.values().stream().noneMatch(h -> h.getQuantity() > 0);
    }
}
