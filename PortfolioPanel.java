package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import model.User;

public class PortfolioPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private User user;

    // Summary labels
    private JLabel totalValueLabel;
    private JLabel cashLabel;
    private JLabel pnlLabel;

    private java.util.HashMap<String, Double> avgBuyPrice  = new java.util.HashMap<>();
    private java.util.HashMap<String, Double> marketPrices = new java.util.HashMap<>();

    private static final java.util.HashMap<String, Double> BASE_PRICES = new java.util.HashMap<>();
    static {
        BASE_PRICES.put("RELIANCE",   2500.0);
        BASE_PRICES.put("TCS",        3900.0);
        BASE_PRICES.put("HDFC",       1700.0);
        BASE_PRICES.put("INFY",       1450.0);
        BASE_PRICES.put("WIPRO",       420.0);
        BASE_PRICES.put("BAJFINANCE", 6800.0);
    }

    public PortfolioPanel(User user) {
        this.user = user;
        marketPrices.putAll(BASE_PRICES);

        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.BG_DARK);

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildSummary(), BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────
    //  Top bar ✅ monospaced, green, no emoji
    // ─────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("[ MY PORTFOLIO ]");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(MainFrame.ACCENT_GREEN);

        JLabel name = new JLabel("// " + user.getName().toUpperCase());
        name.setFont(new Font("Monospaced", Font.PLAIN, 11));
        name.setForeground(MainFrame.TEXT_MUTED);

        bar.add(title, BorderLayout.WEST);
        bar.add(name,  BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    //  Table ✅ green selection, monospaced
    // ─────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] columns = {"Symbol", "Qty", "Avg Buy (Rs.)", "Current (Rs.)", "P&L (Rs.)", "P&L %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setBackground(MainFrame.BG_CARD);
        table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(MainFrame.BORDER);
        table.setSelectionBackground(new Color(0, 255, 70, 40));  // ✅ green
        table.setSelectionForeground(MainFrame.ACCENT_GREEN);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Header ✅ monospaced
        JTableHeader header = table.getTableHeader();
        header.setBackground(MainFrame.BG_PANEL);
        header.setForeground(MainFrame.TEXT_MUTED);
        header.setFont(new Font("Monospaced", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        // P&L color renderer ✅ monospaced
        DefaultTableCellRenderer pnlRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBackground(MainFrame.BG_CARD);
                setFont(new Font("Monospaced", Font.PLAIN, 13));
                String text = val == null ? "" : val.toString();
                if (text.startsWith("+"))      setForeground(MainFrame.ACCENT_GREEN);
                else if (text.startsWith("-")) setForeground(MainFrame.ACCENT_RED);
                else                           setForeground(MainFrame.TEXT_MUTED);
                return this;
            }
        };

        // Right-align number renderer ✅ monospaced
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBackground(MainFrame.BG_CARD);
                setForeground(MainFrame.TEXT_PRIMARY);
                setFont(new Font("Monospaced", Font.PLAIN, 13));
                return this;
            }
        };

        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(pnlRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(pnlRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(MainFrame.BG_DARK);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    // ─────────────────────────────────────────────────────────
    //  Summary footer ✅ monospaced, green accents
    // ─────────────────────────────────────────────────────────
    private JPanel buildSummary() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 0, 0));
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, MainFrame.BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        cashLabel       = buildSummaryCard("Cash",            "Rs." + String.format("%,.2f", user.getCash()), MainFrame.ACCENT_GREEN);
        totalValueLabel = buildSummaryCard("Portfolio Value", "Rs.0.00", MainFrame.TEXT_PRIMARY);
        pnlLabel        = buildSummaryCard("Total P&L",       "Rs.0.00", MainFrame.TEXT_MUTED);

        bar.add(cashLabel);
        bar.add(totalValueLabel);
        bar.add(pnlLabel);
        return bar;
    }

    private JLabel buildSummaryCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Monospaced", Font.PLAIN, 11));
        t.setForeground(MainFrame.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Monospaced", Font.BOLD, 16));
        v.setForeground(valueColor);
        v.setName(title);

        card.add(t);
        card.add(v);
        return v;
    }

    // ─────────────────────────────────────────────────────────
    //  Public refresh
    // ─────────────────────────────────────────────────────────
    public void refresh() {
        refresh(null, -1);
    }

    public void refresh(String boughtSymbol, double boughtAtPrice) {
        if (boughtSymbol != null && boughtAtPrice > 0) {
            avgBuyPrice.put(boughtSymbol, boughtAtPrice);
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        double totalValue = 0;
        double totalPnl   = 0;

        for (java.util.Map.Entry<String, Integer> entry : user.getPortfolio().entrySet()) {
            String symbol  = entry.getKey();
            int    qty     = entry.getValue();
            double current = marketPrices.getOrDefault(symbol, 0.0);
            double avgBuy  = avgBuyPrice.getOrDefault(symbol,
                    BASE_PRICES.getOrDefault(symbol, current));

            double pnl    = (current - avgBuy) * qty;
            double pnlPct = ((current - avgBuy) / avgBuy) * 100;
            double value  = current * qty;

            totalValue += value;
            totalPnl   += pnl;

            model.addRow(new Object[]{
                symbol,
                qty,
                String.format("%.2f", avgBuy),
                String.format("%.2f", current),
                (pnl >= 0 ? "+" : "") + String.format("%.2f", pnl),
                (pnlPct >= 0 ? "+" : "") + String.format("%.2f", pnlPct) + "%"
            });
        }

        cashLabel.setText("Rs." + String.format("%,.2f", user.getCash()));

        totalValueLabel.setText("Rs." + String.format("%,.2f", totalValue));
        totalValueLabel.setForeground(MainFrame.TEXT_PRIMARY);

        double finalPnl = totalPnl;
        pnlLabel.setText((finalPnl >= 0 ? "+" : "") + "Rs." + String.format("%,.2f", finalPnl));
        pnlLabel.setForeground(finalPnl >= 0 ? MainFrame.ACCENT_GREEN : MainFrame.ACCENT_RED);
    }

    // ─────────────────────────────────────────────────────────
    //  Called by MarketPanel to push live prices in
    // ─────────────────────────────────────────────────────────
    public void updateMarketPrices(java.util.HashMap<String, Double> prices) {
        this.marketPrices = prices;
        if (!user.getPortfolio().isEmpty()) {
            refresh();
        }
    }
}