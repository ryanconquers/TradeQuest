package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.TradeLog;
import model.User;
import service.TradeService;

public class TradeLogPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private User user;
    private TradeService tradeService;

    // Summary labels
    private JLabel totalTradesLabel;
    private JLabel buyCountLabel;
    private JLabel sellCountLabel;
    private JLabel volumeLabel;
    private JLabel bestTradeLabel;
    private JLabel worstTradeLabel;

    public TradeLogPanel(User user, TradeService tradeService) {
        this.user         = user;
        this.tradeService = tradeService;

        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.BG_DARK);

        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
        add(buildSummary(),  BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────
    //  Top bar
    // ─────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("[ TRADE LOG ]");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(MainFrame.ACCENT_GREEN);

        JLabel subtitle = new JLabel("// complete history of all executed trades");
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 11));
        subtitle.setForeground(MainFrame.TEXT_MUTED);

        left.add(title);
        left.add(subtitle);

        // Export button
        JButton exportBtn = new JButton("EXPORT CSV") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(MainFrame.ACCENT_GREEN.darker());
                else if (getModel().isRollover())
                    g2.setColor(MainFrame.ACCENT_GREEN.brighter());
                else
                    g2.setColor(MainFrame.ACCENT_GREEN_DIM);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        exportBtn.setContentAreaFilled(false);
        exportBtn.setBorderPainted(false);
        exportBtn.setFocusPainted(false);
        exportBtn.setPreferredSize(new Dimension(130, 34));
        exportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportToCsv());

        bar.add(left,      BorderLayout.WEST);
        bar.add(exportBtn, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    //  Table
    // ─────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] columns = {
            "Timestamp", "Type", "Symbol",
            "Price (Rs.)", "Qty", "Value (Rs.)", "Cash After (Rs.)"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setBackground(MainFrame.BG_CARD);
        table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setGridColor(MainFrame.BORDER);
        table.setSelectionBackground(new Color(0, 255, 70, 40));
        table.setSelectionForeground(MainFrame.ACCENT_GREEN);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(MainFrame.BG_PANEL);
        header.setForeground(MainFrame.TEXT_MUTED);
        header.setFont(new Font("Monospaced", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, MainFrame.BORDER));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);

        // Type renderer — BUY green, SELL red
        DefaultTableCellRenderer typeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBackground(MainFrame.BG_CARD);
                setFont(new Font("Monospaced", Font.BOLD, 12));
                String text = val == null ? "" : val.toString();
                if (text.equals("BUY"))       setForeground(MainFrame.ACCENT_GREEN);
                else if (text.equals("SELL")) setForeground(MainFrame.ACCENT_RED);
                else                          setForeground(MainFrame.TEXT_MUTED);
                return this;
            }
        };

        // Right-align number renderer
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBackground(MainFrame.BG_CARD);
                setForeground(MainFrame.TEXT_PRIMARY);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
                return this;
            }
        };

        // Timestamp renderer — muted color
        DefaultTableCellRenderer timeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                setBackground(MainFrame.BG_CARD);
                setForeground(MainFrame.TEXT_MUTED);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
                return this;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(timeRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(typeRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(MainFrame.BG_DARK);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    // ─────────────────────────────────────────────────────────
    //  Summary footer — 6 stat cards
    // ─────────────────────────────────────────────────────────
    private JPanel buildSummary() {
        JPanel bar = new JPanel(new GridLayout(1, 6, 0, 0));
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, MainFrame.BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        totalTradesLabel = buildStatCard("Total Trades", "0",       MainFrame.ACCENT_GREEN);
        buyCountLabel    = buildStatCard("Buys",         "0",       MainFrame.ACCENT_GREEN);
        sellCountLabel   = buildStatCard("Sells",        "0",       MainFrame.ACCENT_RED);
        volumeLabel      = buildStatCard("Total Volume", "Rs.0.00", MainFrame.TEXT_PRIMARY);
        bestTradeLabel   = buildStatCard("Best Trade",   "—",       MainFrame.ACCENT_GREEN);
        worstTradeLabel  = buildStatCard("Worst Trade",  "—",       MainFrame.ACCENT_RED);

        bar.add(totalTradesLabel);
        bar.add(buyCountLabel);
        bar.add(sellCountLabel);
        bar.add(volumeLabel);
        bar.add(bestTradeLabel);
        bar.add(worstTradeLabel);
        return bar;
    }

    private JLabel buildStatCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Monospaced", Font.PLAIN, 10));
        t.setForeground(MainFrame.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Monospaced", Font.BOLD, 14));
        v.setForeground(valueColor);

        card.add(t);
        card.add(v);
        return v;
    }

    // ─────────────────────────────────────────────────────────
    //  Public refresh — called after every trade
    // ─────────────────────────────────────────────────────────
    public void refresh() {
        List<TradeLog> logs = tradeService.getTradeLogs();

        tableModel.setRowCount(0);

        // Add rows in reverse order — newest first
        for (int i = logs.size() - 1; i >= 0; i--) {
            TradeLog log = logs.get(i);
            tableModel.addRow(new Object[]{
                log.getFormattedTimestamp(),
                log.getTypeString(),
                log.getSymbol(),
                log.getFormattedPrice(),
                log.getQty(),
                log.getFormattedValue(),
                log.getFormattedCashAfter()
            });
        }

        // Update summary
        totalTradesLabel.setText(String.valueOf(tradeService.getTotalTrades()));
        buyCountLabel.setText(String.valueOf(tradeService.getBuyCount()));
        sellCountLabel.setText(String.valueOf(tradeService.getSellCount()));
        volumeLabel.setText("Rs." + String.format("%,.2f",
                tradeService.getTotalVolume()));

        TradeLog best = tradeService.getBestTrade();
        TradeLog worst = tradeService.getWorstTrade();

        bestTradeLabel.setText(best != null
                ? best.getSymbol() + " Rs." + best.getFormattedValue()
                : "—");
        worstTradeLabel.setText(worst != null
                ? worst.getSymbol() + " Rs." + worst.getFormattedValue()
                : "—");
    }

    // ─────────────────────────────────────────────────────────
    //  CSV Export
    // ─────────────────────────────────────────────────────────
    private void exportToCsv() {
        List<TradeLog> logs = tradeService.getTradeLogs();

        if (logs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No trades to export yet.",
                "Export Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Save to desktop
        String home     = System.getProperty("user.home");
        String filename = home + "/Desktop/TradeQuest_Log_"
                + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Header row
            writer.println(
                "Timestamp,Type,Symbol,Price (Rs.),Qty,Value (Rs.),Cash After (Rs.)"
            );

            // Data rows — chronological order
            for (TradeLog log : logs) {
                writer.println(log.toCsvRow());
            }

            // Blank line then summary
            writer.println();
            writer.println("SESSION SUMMARY");
            writer.println("Trader," + user.getName());
            writer.println("Total Trades," + tradeService.getTotalTrades());
            writer.println("Buys,"  + tradeService.getBuyCount());
            writer.println("Sells," + tradeService.getSellCount());
            writer.println("Total Volume,Rs." + String.format("%.2f",
                    tradeService.getTotalVolume()));
            writer.println("Final Cash,Rs." + String.format("%.2f",
                    user.getCash()));

            TradeLog best = tradeService.getBestTrade();
            if (best != null)
                writer.println("Best Trade," + best.getSymbol()
                        + " Rs." + best.getFormattedValue());

            TradeLog worst = tradeService.getWorstTrade();
            if (worst != null)
                writer.println("Worst Trade," + worst.getSymbol()
                        + " Rs." + worst.getFormattedValue());

            // Success message
            JOptionPane.showMessageDialog(this,
                "Exported to Desktop:\n" + "TradeQuest_Log_"
                + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".csv",
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Export failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}