import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Stock Trading & Portfolio Management System.
 * Provides a rich console interface for real-time market data, trading,
 * portfolio valuation, and account management.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static Market market;
    private static TradingEngine engine;
    private static User user;

    public static void main(String[] args) {
        market = new Market();
        engine = new TradingEngine(market);
        user = FileStorage.loadOrCreateUser("U1001", "Mohan Krishna", 100000.00);

        System.out.println("=========================================");
        System.out.println("      Stock Trading Platform");
        System.out.println("=========================================");
        System.out.printf("Logged in as: %s (ID: %s)%n", user.getUsername(), user.getUserId());

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> viewMarket();
                case "2" -> buyShares();
                case "3" -> sellShares();
                case "4" -> viewPortfolio();
                case "5" -> viewTransactionHistory();
                case "6" -> simulateMarketTick();
                case "7" -> manageFunds();
                case "8" -> {
                    FileStorage.saveState(user, engine.getTransactions());
                    System.out.println("\nAll portfolio data and transactions saved.");
                    System.out.println("Thank you for using Stock Trading Platform. Happy Trading!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n----- MENU -----");
        System.out.printf("Cash Available: Rs.%.2f | Net Worth: Rs.%.2f%n",
                user.getCashBalance(), user.getTotalNetWorth(market));
        System.out.println("1. View live market (all stocks)");
        System.out.println("2. Buy shares");
        System.out.println("3. Sell shares");
        System.out.println("4. View my portfolio (holdings & P/L)");
        System.out.println("5. View transaction history");
        System.out.println("6. Simulate market fluctuation (tick)");
        System.out.println("7. Deposit / Withdraw funds");
        System.out.println("8. Exit");
        System.out.print("Enter choice: ");
    }

    private static void viewMarket() {
        System.out.println("\n=========================== LIVE MARKET WATCH ===========================");
        System.out.printf("%-10s | %-28s | %-13s | %-14s%n", "Symbol", "Company Name", "Current Price", "Change");
        System.out.println("-------------------------------------------------------------------------");
        for (Stock stock : market.getAllStocks()) {
            System.out.println(stock);
        }
        System.out.println("=========================================================================");
    }

    private static void buyShares() {
        System.out.println("\n--- Buy Shares ---");
        System.out.printf("Available Cash Balance: Rs.%.2f%n", user.getCashBalance());
        System.out.print("Enter stock ticker symbol (e.g. TCS, INFY, RELIANCE): ");
        String symbol = sc.nextLine().trim().toUpperCase();

        Stock stock = market.getStock(symbol);
        if (stock == null) {
            System.out.println("Stock symbol '" + symbol + "' not found.");
            return;
        }

        System.out.printf("Selected: %s (%s) @ Rs.%.2f per share%n",
                stock.getSymbol(), stock.getCompanyName(), stock.getCurrentPrice());

        int maxAffordable = (int) (user.getCashBalance() / stock.getCurrentPrice());
        System.out.printf("Maximum shares you can afford: %d%n", maxAffordable);

        System.out.print("Enter quantity to buy: ");
        try {
            int qty = Integer.parseInt(sc.nextLine().trim());
            TradingEngine.OrderResult result = engine.buy(user, symbol, qty);
            System.out.println(result.message);
            if (result.success && result.transaction != null) {
                System.out.println("\nOrder Confirmation:");
                System.out.println("Transaction ID : " + result.transaction.getTransactionId());
                System.out.println("Symbol         : " + result.transaction.getSymbol());
                System.out.println("Quantity       : " + result.transaction.getQuantity());
                System.out.printf("Price Per Share: Rs.%.2f%n", result.transaction.getPricePerShare());
                System.out.printf("Total Amount   : Rs.%.2f%n", result.transaction.getTotalAmount());
                System.out.printf("Remaining Cash : Rs.%.2f%n", user.getCashBalance());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity entered. Please enter a whole number.");
        }
    }

    private static void sellShares() {
        System.out.println("\n--- Sell Shares ---");
        Portfolio portfolio = user.getPortfolio();
        if (portfolio.isEmpty()) {
            System.out.println("You do not own any shares to sell.");
            return;
        }

        System.out.println("Your current holdings:");
        for (Holding h : portfolio.getActiveHoldings()) {
            Stock s = market.getStock(h.getSymbol());
            double currPrice = (s != null) ? s.getCurrentPrice() : 0.0;
            System.out.printf("  %-10s : %4d shares (Current Market Price: Rs.%.2f)%n",
                    h.getSymbol(), h.getQuantity(), currPrice);
        }

        System.out.print("Enter stock ticker symbol to sell: ");
        String symbol = sc.nextLine().trim().toUpperCase();

        int owned = portfolio.getSharesCount(symbol);
        if (owned <= 0) {
            System.out.println("You do not own any shares of " + symbol + ".");
            return;
        }

        System.out.printf("You currently own %d shares of %s.%n", owned, symbol);
        System.out.print("Enter quantity to sell: ");
        try {
            int qty = Integer.parseInt(sc.nextLine().trim());
            TradingEngine.OrderResult result = engine.sell(user, symbol, qty);
            System.out.println(result.message);
            if (result.success && result.transaction != null) {
                System.out.println("\nOrder Confirmation:");
                System.out.println("Transaction ID : " + result.transaction.getTransactionId());
                System.out.println("Symbol         : " + result.transaction.getSymbol());
                System.out.println("Quantity       : " + result.transaction.getQuantity());
                System.out.printf("Price Per Share: Rs.%.2f%n", result.transaction.getPricePerShare());
                System.out.printf("Total Proceeds : Rs.%.2f%n", result.transaction.getTotalAmount());
                System.out.printf("Updated Cash   : Rs.%.2f%n", user.getCashBalance());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity entered. Please enter a whole number.");
        }
    }

    private static void viewPortfolio() {
        System.out.println("\n============================== MY PORTFOLIO ==============================");
        Portfolio portfolio = user.getPortfolio();
        double totalInvested = portfolio.getTotalInvested();
        double currentVal = portfolio.getCurrentValue(market);
        double totalPL = portfolio.getProfitLoss(market);
        double totalPLPct = portfolio.getProfitLossPercent(market);
        double netWorth = user.getTotalNetWorth(market);

        System.out.printf("Account Holder   : %s%n", user.getUsername());
        System.out.printf("Cash Balance     : Rs.%.2f%n", user.getCashBalance());
        System.out.printf("Total Invested   : Rs.%.2f%n", totalInvested);
        System.out.printf("Portfolio Value  : Rs.%.2f%n", currentVal);
        System.out.printf("Total Net Worth  : Rs.%.2f%n", netWorth);

        String sign = totalPL >= 0 ? "+" : "";
        System.out.printf("Unrealized P/L   : %sRs.%.2f (%s%.2f%%)%n", sign, totalPL, sign, totalPLPct);
        System.out.println("--------------------------------------------------------------------------");

        Collection<Holding> holdings = portfolio.getActiveHoldings();
        if (holdings.isEmpty()) {
            System.out.println("No active stock holdings. Use option 2 to buy shares.");
        } else {
            System.out.printf("%-10s | %-6s | %-12s | %-13s | %-13s | %-15s%n",
                    "Symbol", "Qty", "Avg Buy (Rs)", "Current (Rs)", "Valuation (Rs)", "P/L (Rs / %)");
            System.out.println("--------------------------------------------------------------------------");
            for (Holding h : holdings) {
                Stock s = market.getStock(h.getSymbol());
                double currPrice = (s != null) ? s.getCurrentPrice() : h.getAverageBuyPrice();
                double valuation = h.getQuantity() * currPrice;
                double pl = valuation - h.getTotalCostBasis();
                double plPct = (h.getTotalCostBasis() > 0) ? (pl / h.getTotalCostBasis()) * 100.0 : 0.0;
                String sSign = pl >= 0 ? "+" : "";

                System.out.printf("%-10s | %6d | %12.2f | %13.2f | %14.2f | %s%.2f (%s%.2f%%)%n",
                        h.getSymbol(), h.getQuantity(), h.getAverageBuyPrice(),
                        currPrice, valuation, sSign, pl, sSign, plPct);
            }
        }
        System.out.println("==========================================================================");
    }

    private static void viewTransactionHistory() {
        System.out.println("\n=========================== TRANSACTION HISTORY ===========================");
        List<Transaction> list = engine.getTransactions();
        if (list.isEmpty()) {
            System.out.println("No transactions recorded yet.");
        } else {
            for (Transaction tx : list) {
                System.out.println(tx);
            }
        }
        System.out.println("===========================================================================");
    }

    private static void simulateMarketTick() {
        System.out.println("\nSimulating market price movements...");
        market.simulateMarketTick();
        System.out.println("Market prices updated successfully!");
        viewMarket();
    }

    private static void manageFunds() {
        System.out.println("\n--- Deposit / Withdraw Funds ---");
        System.out.printf("Current Cash Balance: Rs.%.2f%n", user.getCashBalance());
        System.out.println("1. Deposit Funds");
        System.out.println("2. Withdraw Funds");
        System.out.print("Select option: ");
        String opt = sc.nextLine().trim();
        switch (opt) {
            case "1" -> {
                System.out.print("Enter amount to deposit (Rs): ");
                try {
                    double amt = Double.parseDouble(sc.nextLine().trim());
                    if (amt > 0) {
                        user.deposit(amt);
                        FileStorage.saveState(user, engine.getTransactions());
                        System.out.printf("Deposited Rs.%.2f successfully. New Cash Balance: Rs.%.2f%n",
                                amt, user.getCashBalance());
                    } else {
                        System.out.println("Deposit amount must be positive.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount entered.");
                }
            }
            case "2" -> {
                System.out.print("Enter amount to withdraw (Rs): ");
                try {
                    double amt = Double.parseDouble(sc.nextLine().trim());
                    if (amt <= 0) {
                        System.out.println("Withdrawal amount must be positive.");
                    } else if (user.withdraw(amt)) {
                        FileStorage.saveState(user, engine.getTransactions());
                        System.out.printf("Withdrew Rs.%.2f successfully. New Cash Balance: Rs.%.2f%n",
                                amt, user.getCashBalance());
                    } else {
                        System.out.println("Insufficient cash balance for withdrawal.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount entered.");
                }
            }
            default -> System.out.println("Invalid option.");
        }
    }
}
