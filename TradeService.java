package service;

import model.*;
import exception.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeService {

    // ── Trade log — every buy/sell recorded here ──────────────
    private List<TradeLog> tradeLogs = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    //  Buy
    // ─────────────────────────────────────────────────────────
    public void buyStock(User user, Stock stock, int qty)
            throws TradeException {

        double cost = stock.getPrice() * qty;

        if (user.getCash() < cost) {
            throw new TradeException(
                "Insufficient balance. Need Rs."
                + String.format("%,.2f", cost)
                + " but have Rs."
                + String.format("%,.2f", user.getCash()));
        }

        user.setCash(user.getCash() - cost);

        user.getPortfolio().put(
            stock.getSymbol(),
            user.getPortfolio().getOrDefault(stock.getSymbol(), 0) + qty
        );

        // ✅ Record trade
        tradeLogs.add(new TradeLog(
            TradeLog.TradeType.BUY,
            stock.getSymbol(),
            stock.getPrice(),
            qty,
            user.getCash()
        ));
    }

    // ─────────────────────────────────────────────────────────
    //  Sell
    // ─────────────────────────────────────────────────────────
    public void sellStock(User user, Stock stock, int qty)
            throws TradeException {

        String symbol  = stock.getSymbol();
        int    holding = user.getPortfolio().getOrDefault(symbol, 0);

        if (holding == 0) {
            throw new TradeException(
                "You don't own any shares of " + symbol);
        }

        if (holding < qty) {
            throw new TradeException(
                "Insufficient shares. You own "
                + holding + " share(s) of " + symbol
                + " but tried to sell " + qty);
        }

        user.setCash(user.getCash() + stock.getPrice() * qty);

        int remaining = holding - qty;
        if (remaining == 0) {
            user.getPortfolio().remove(symbol);
        } else {
            user.getPortfolio().put(symbol, remaining);
        }

        // ✅ Record trade
        tradeLogs.add(new TradeLog(
            TradeLog.TradeType.SELL,
            stock.getSymbol(),
            stock.getPrice(),
            qty,
            user.getCash()
        ));
    }

    // ─────────────────────────────────────────────────────────
    //  Log access
    // ─────────────────────────────────────────────────────────
    public List<TradeLog> getTradeLogs() {
        return Collections.unmodifiableList(tradeLogs);
    }

    public int getTotalTrades() {
        return tradeLogs.size();
    }

    public int getBuyCount() {
        return (int) tradeLogs.stream()
                .filter(t -> t.getType() == TradeLog.TradeType.BUY)
                .count();
    }

    public int getSellCount() {
        return (int) tradeLogs.stream()
                .filter(t -> t.getType() == TradeLog.TradeType.SELL)
                .count();
    }

    public TradeLog getBestTrade() {
        return tradeLogs.stream()
                .filter(t -> t.getType() == TradeLog.TradeType.SELL)
                .max(java.util.Comparator.comparingDouble(TradeLog::getValue))
                .orElse(null);
    }

    public TradeLog getWorstTrade() {
        return tradeLogs.stream()
                .filter(t -> t.getType() == TradeLog.TradeType.SELL)
                .min(java.util.Comparator.comparingDouble(TradeLog::getValue))
                .orElse(null);
    }

    public double getTotalVolume() {
        return tradeLogs.stream()
                .mapToDouble(TradeLog::getValue)
                .sum();
    }
}