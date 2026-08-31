import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an immutable record of a stock buy or sell order.
 */
public class Transaction {

    public enum Type {
        BUY, SELL
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String transactionId;
    private final Type type;
    private final String symbol;
    private final int quantity;
    private final double pricePerShare;
    private final double totalAmount;
    private final LocalDateTime timestamp;

    public Transaction(String transactionId, Type type, String symbol,
                       int quantity, double pricePerShare, double totalAmount,
                       LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.type = type;
        this.symbol = symbol.toUpperCase().trim();
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Type getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String toCsv() {
        return String.join(",",
                transactionId,
                type.name(),
                symbol,
                String.valueOf(quantity),
                String.format("%.2f", pricePerShare),
                String.format("%.2f", totalAmount),
                timestamp.format(FORMATTER)
        );
    }

    public static Transaction fromCsv(String line) {
        String[] parts = line.split(",", -1);
        String txId = parts[0];
        Type type = Type.valueOf(parts[1]);
        String symbol = parts[2];
        int qty = Integer.parseInt(parts[3]);
        double price = Double.parseDouble(parts[4]);
        double total = Double.parseDouble(parts[5]);
        LocalDateTime time = LocalDateTime.parse(parts[6], FORMATTER);
        return new Transaction(txId, type, symbol, qty, price, total, time);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-4s | %-8s | %4d shares @ Rs.%9.2f | Total: Rs.%11.2f | %s",
                transactionId, type, symbol, quantity, pricePerShare, totalAmount, timestamp.format(FORMATTER));
    }
}
