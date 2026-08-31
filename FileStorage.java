import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence of user accounts, portfolio holdings, and transaction history
 * using standard CSV flat files.
 */
public class FileStorage {

    private static final String USER_FILE = "user_data.txt";
    private static final String PORTFOLIO_FILE = "portfolio.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    /**
     * Saves user details, portfolio, and transaction logs.
     */
    public static synchronized void saveState(User user, List<Transaction> transactions) {
        saveUser(user);
        savePortfolio(user.getPortfolio());
        saveTransactions(transactions);
    }

    private static void saveUser(User user) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE))) {
            pw.println("userId,username,cashBalance");
            pw.printf("%s,%s,%.2f%n", user.getUserId(), user.getUsername(), user.getCashBalance());
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    public static void savePortfolio(Portfolio portfolio) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PORTFOLIO_FILE))) {
            pw.println("symbol,quantity,totalCostBasis");
            for (Holding h : portfolio.getActiveHoldings()) {
                pw.println(h.toCsv());
            }
        } catch (IOException e) {
            System.err.println("Error saving portfolio data: " + e.getMessage());
        }
    }

    public static void saveTransactions(List<Transaction> transactions) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            pw.println("transactionId,type,symbol,quantity,pricePerShare,totalAmount,timestamp");
            for (Transaction tx : transactions) {
                pw.println(tx.toCsv());
            }
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    /**
     * Loads the existing user or initializes a new default user if none exists.
     */
    public static User loadOrCreateUser(String defaultId, String defaultName, double defaultCash) {
        Path path = Paths.get(USER_FILE);
        User user = null;
        if (Files.exists(path)) {
            try (BufferedReader br = Files.newBufferedReader(path)) {
                String header = br.readLine();
                String line = br.readLine();
                if (line != null && !line.isBlank()) {
                    String[] parts = line.split(",", -1);
                    String uId = parts[0];
                    String name = parts[1];
                    double cash = Double.parseDouble(parts[2]);
                    user = new User(uId, name, cash);
                }
            } catch (Exception e) {
                System.err.println("Error loading user data, creating default user: " + e.getMessage());
            }
        }

        if (user == null) {
            user = new User(defaultId, defaultName, defaultCash);
            saveUser(user);
        }

        // Load portfolio holdings into user
        loadPortfolio(user.getPortfolio());
        return user;
    }

    private static void loadPortfolio(Portfolio portfolio) {
        Path path = Paths.get(PORTFOLIO_FILE);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Holding h = Holding.fromCsv(line);
                    portfolio.addHolding(h);
                } catch (Exception ex) {
                    // skip corrupted line
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading portfolio: " + e.getMessage());
        }
    }

    /**
     * Loads transaction history from disk.
     */
    public static List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        Path path = Paths.get(TRANSACTIONS_FILE);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Transaction tx = Transaction.fromCsv(line);
                    list.add(tx);
                } catch (Exception ex) {
                    // skip corrupted line
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
        return list;
    }
}
