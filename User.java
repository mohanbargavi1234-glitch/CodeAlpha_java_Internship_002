/**
 * Represents a registered user/trader with a cash balance and a stock portfolio.
 */
public class User {

    private final String userId;
    private final String username;
    private double cashBalance;
    private final Portfolio portfolio;

    public User(String userId, String username, double initialCash) {
        this.userId = userId;
        this.username = username;
        this.cashBalance = Math.round(initialCash * 100.0) / 100.0;
        this.portfolio = new Portfolio();
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public synchronized double getCashBalance() {
        return cashBalance;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public synchronized void deposit(double amount) {
        if (amount > 0) {
            this.cashBalance += Math.round(amount * 100.0) / 100.0;
        }
    }

    public synchronized boolean withdraw(double amount) {
        if (amount > 0 && this.cashBalance >= amount) {
            this.cashBalance -= Math.round(amount * 100.0) / 100.0;
            return true;
        }
        return false;
    }

    public synchronized boolean deductCash(double amount) {
        if (amount > 0 && this.cashBalance >= amount) {
            this.cashBalance -= amount;
            return true;
        }
        return false;
    }

    public synchronized void addCash(double amount) {
        if (amount > 0) {
            this.cashBalance += amount;
        }
    }

    /**
     * Total Net Worth = Cash Balance + Current Portfolio Market Value.
     */
    public double getTotalNetWorth(Market market) {
        return cashBalance + portfolio.getCurrentValue(market);
    }
}
