/**
 * Represents the shares of a single stock held in a user's portfolio.
 * Tracks quantity and total invested cost basis to calculate average purchase price.
 */
public class Holding {

    private final String symbol;
    private int quantity;
    private double totalCostBasis;

    public Holding(String symbol, int quantity, double totalCostBasis) {
        this.symbol = symbol.toUpperCase().trim();
        this.quantity = quantity;
        this.totalCostBasis = totalCostBasis;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalCostBasis() {
        return totalCostBasis;
    }

    public double getAverageBuyPrice() {
        if (quantity <= 0) return 0.0;
        return totalCostBasis / quantity;
    }

    /**
     * Adds shares and increases the total cost basis.
     */
    public void addShares(int qty, double pricePerShare) {
        this.quantity += qty;
        this.totalCostBasis += (qty * pricePerShare);
    }

    /**
     * Removes shares and reduces the cost basis proportionally based on average cost.
     * Returns the realized cost basis of the sold shares.
     */
    public double removeShares(int qty) {
        if (qty <= 0) return 0.0;
        if (qty > this.quantity) {
            qty = this.quantity;
        }
        double avgCost = getAverageBuyPrice();
        double costOfSoldShares = avgCost * qty;
        this.quantity -= qty;
        this.totalCostBasis -= costOfSoldShares;
        if (this.quantity == 0) {
            this.totalCostBasis = 0.0;
        }
        return costOfSoldShares;
    }

    public String toCsv() {
        return String.join(",", symbol, String.valueOf(quantity), String.format("%.2f", totalCostBasis));
    }

    public static Holding fromCsv(String line) {
        String[] parts = line.split(",", -1);
        String sym = parts[0];
        int qty = Integer.parseInt(parts[1]);
        double cost = Double.parseDouble(parts[2]);
        return new Holding(sym, qty, cost);
    }
}
