package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TradeLog {

    public enum TradeType { BUY, SELL }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime timestamp;
    private TradeType     type;
    private String        symbol;
    private double        price;
    private int           qty;
    private double        value;
    private double        cashAfter;

    public TradeLog(TradeType type, String symbol,
                    double price, int qty, double cashAfter) {
        this.timestamp = LocalDateTime.now();
        this.type      = type;
        this.symbol    = symbol;
        this.price     = price;
        this.qty       = qty;
        this.value     = price * qty;
        this.cashAfter = cashAfter;
    }

    // ─────────────────────────────────────────────────────────
    //  Formatted getters — ready for UI and CSV
    // ─────────────────────────────────────────────────────────
    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    public String getTypeString() {
        return type == TradeType.BUY ? "BUY" : "SELL";
    }

    public String getFormattedPrice() {
        return String.format("%.2f", price);
    }

    public String getFormattedValue() {
        return String.format("%.2f", value);
    }

    public String getFormattedCashAfter() {
        return String.format("%.2f", cashAfter);
    }

    // ─────────────────────────────────────────────────────────
    //  CSV row — one line per trade
    // ─────────────────────────────────────────────────────────
    public String toCsvRow() {
        return String.join(",",
            getFormattedTimestamp(),
            getTypeString(),
            symbol,
            getFormattedPrice(),
            String.valueOf(qty),
            getFormattedValue(),
            getFormattedCashAfter()
        );
    }

    // ─────────────────────────────────────────────────────────
    //  Raw getters
    // ─────────────────────────────────────────────────────────
    public LocalDateTime getTimestamp() { return timestamp; }
    public TradeType     getType()      { return type; }
    public String        getSymbol()    { return symbol; }
    public double        getPrice()     { return price; }
    public int           getQty()       { return qty; }
    public double        getValue()     { return value; }
    public double        getCashAfter() { return cashAfter; }
}