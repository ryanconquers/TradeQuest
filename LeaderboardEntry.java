package model;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private String name;
    private double startingCash;
    private double currentCash;
    private double portfolioValue;
    private boolean isHuman;

    public LeaderboardEntry(String name, double startingCash, boolean isHuman) {
        this.name         = name;
        this.startingCash = startingCash;
        this.currentCash  = startingCash;
        this.portfolioValue = 0.0;
        this.isHuman      = isHuman;
    }

    // ─────────────────────────────────────────────────────────
    //  Core calculations
    // ─────────────────────────────────────────────────────────
    public double getTotalValue() {
        return currentCash + portfolioValue;
    }

    public double getPnl() {
        return getTotalValue() - startingCash;
    }

    public double getPnlPercent() {
        return (getPnl() / startingCash) * 100;
    }

    public String getFormattedPnl() {
        double pnl = getPnl();
        return (pnl >= 0 ? "+" : "") + String.format("%.2f", pnl);
    }

    public String getFormattedPnlPercent() {
        double pct = getPnlPercent();
        return (pct >= 0 ? "+" : "") + String.format("%.2f", pct) + "%";
    }

    public int getRank(java.util.List<LeaderboardEntry> all) {
        long better = all.stream()
                .filter(e -> e.getTotalValue() > this.getTotalValue())
                .count();
        return (int) better + 1;
    }

    // ─────────────────────────────────────────────────────────
    //  Comparable — sort by total value descending
    // ─────────────────────────────────────────────────────────
    @Override
    public int compareTo(LeaderboardEntry other) {
        return Double.compare(other.getTotalValue(), this.getTotalValue());
    }

    // ─────────────────────────────────────────────────────────
    //  Getters & Setters
    // ─────────────────────────────────────────────────────────
    public String getName()                          { return name; }
    public double getStartingCash()                  { return startingCash; }
    public double getCurrentCash()                   { return currentCash; }
    public double getPortfolioValue()                { return portfolioValue; }
    public boolean isHuman()                         { return isHuman; }

    public void setCurrentCash(double currentCash)         { this.currentCash = currentCash; }
    public void setPortfolioValue(double portfolioValue)   { this.portfolioValue = portfolioValue; }
}