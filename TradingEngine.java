import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles core business logic for executing stock buy and sell orders.
 * Validates funds, share availability, maintains order history, and auto-saves changes.
 */
public class TradingEngine {

    public static class OrderResult {
        public final boolean success;
        public final String message;
        public final Transaction transaction;

        public OrderResult(boolean success, String message, Transaction transaction) {
            this.success = success;
            this.message = message;
            this.transaction = transaction;
        }
    }

    private final Market market;
    private final List<Transaction> transactions;
    private int transactionCounter;

    public TradingEngine(Market market) {
        this.market = market;
        this.transactions = new ArrayList<>(FileStorage.loadTransactions());
        this.transactionCounter = this.transactions.size();
    }

    public Market getMarket() {
        return market;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Executes a BUY order for the specified user, symbol, and quantity.
     */
    public synchronized OrderResult buy(User user, String symbol, int quantity) {
        if (quantity <= 0) {
            return new OrderResult(false, "Quantity must be greater than 0.", null);
        }

        Stock stock = market.getStock(symbol);
        if (stock == null) {
            return new OrderResult(false, "Stock ticker '" + symbol + "' not found in market.", null);
        }

        double price = stock.getCurrentPrice();
        double totalCost = Math.round(price * quantity * 100.0) / 100.0;

        if (user.getCashBalance() < totalCost) {
            return new OrderResult(false, String.format(
                    "Insufficient funds. Required: Rs.%.2f, Available: Rs.%.2f",
                    totalCost, user.getCashBalance()), null);
        }

        // Deduct cash and add to portfolio
        user.deductCash(totalCost);
        user.getPortfolio().buy(stock.getSymbol(), quantity, price);

        // Record transaction
        transactionCounter++;
        String txId = "TXN" + String.format("%05d", transactionCounter);
        Transaction tx = new Transaction(txId, Transaction.Type.BUY, stock.getSymbol(),
                quantity, price, totalCost, LocalDateTime.now());
        transactions.add(tx);

        // Persist state
        FileStorage.saveState(user, transactions);

        return new OrderResult(true, String.format(
                "Successfully purchased %d share(s) of %s at Rs.%.2f per share.",
                quantity, stock.getSymbol(), price), tx);
    }

    /**
     * Executes a SELL order for the specified user, symbol, and quantity.
     */
    public synchronized OrderResult sell(User user, String symbol, int quantity) {
        if (quantity <= 0) {
            return new OrderResult(false, "Quantity must be greater than 0.", null);
        }

        Stock stock = market.getStock(symbol);
        if (stock == null) {
            return new OrderResult(false, "Stock ticker '" + symbol + "' not found in market.", null);
        }

        int ownedShares = user.getPortfolio().getSharesCount(stock.getSymbol());
        if (ownedShares < quantity) {
            return new OrderResult(false, String.format(
                    "Insufficient shares. You own %d share(s) of %s, but tried to sell %d.",
                    ownedShares, stock.getSymbol(), quantity), null);
        }

        double price = stock.getCurrentPrice();
        double totalRevenue = Math.round(price * quantity * 100.0) / 100.0;

        // Add cash and update portfolio
        user.addCash(totalRevenue);
        user.getPortfolio().sell(stock.getSymbol(), quantity);

        // Record transaction
        transactionCounter++;
        String txId = "TXN" + String.format("%05d", transactionCounter);
        Transaction tx = new Transaction(txId, Transaction.Type.SELL, stock.getSymbol(),
                quantity, price, totalRevenue, LocalDateTime.now());
        transactions.add(tx);

        // Persist state
        FileStorage.saveState(user, transactions);

        return new OrderResult(true, String.format(
                "Successfully sold %d share(s) of %s at Rs.%.2f per share.",
                quantity, stock.getSymbol(), price), tx);
    }
}
