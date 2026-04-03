package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.LeaderboardEntry;
import model.User;

public class LeaderboardPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private User user;
    private Timer aiTimer;
    private Random random = new Random();

    private static final double STARTING_CASH = 10000.0;

    private LeaderboardEntry humanEntry;
    private List<LeaderboardEntry> aiTraders = new ArrayList<>();
    private double[] aiPortfolioValues = {0, 0, 0};
    private double[] aiCashValues;

    private JLabel rankLabel;
    private JLabel scoreLabel;
    private JLabel pnlLabel;

    public LeaderboardPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.BG_DARK);

        initEntries();

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        startAiSimulation();
    }

    // ─────────────────────────────────────────────────────────
    //  Init entries
    // ─────────────────────────────────────────────────────────
    private void initEntries() {
        humanEntry = new LeaderboardEntry(user.getName(), STARTING_CASH, true);

        aiTraders.add(new LeaderboardEntry("AlgoBot-X",   STARTING_CASH, false));
        aiTraders.add(new LeaderboardEntry("QuantTrader", STARTING_CASH, false));
        aiTraders.add(new LeaderboardEntry("MarketMaven", STARTING_CASH, false));

        aiCashValues = new double[]{STARTING_CASH, STARTING_CASH, STARTING_CASH};
    }

    // ─────────────────────────────────────────────────────────
    //  Top bar — ✅ UPDATED: no emoji, monospaced, green
    // ─────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("[ LEADERBOARD ]");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(MainFrame.ACCENT_GREEN);

        JLabel subtitle = new JLabel("// ranked by % return from starting capital");
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 11));
        subtitle.setForeground(MainFrame.TEXT_MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(subtitle);

        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    //  Table
    // ─────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] columns = {"Rank", "Trader", "Total Value (Rs.)", "P&L (Rs.)", "Return %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setBackground(MainFrame.BG_CARD);
        table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        table.setGridColor(MainFrame.BORDER);
        table.setSelectionBackground(new Color(0, 255, 70, 40)); // ✅ green
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
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        // Rank renderer ✅ no medals
        DefaultTableCellRenderer rankRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBackground(MainFrame.BG_CARD);
                setFont(new Font("Monospaced", Font.BOLD, 13));
                String text = val == null ? "" : val.toString();
                switch (text) {
                    case "1": setText("#1"); setForeground(MainFrame.ACCENT_GREEN); break;
                    case "2": setText("#2"); setForeground(MainFrame.ACCENT_GREEN_DIM); break;
                    case "3": setText("#3"); setForeground(new Color(0, 150, 40)); break;
                    default:  setText("#" + text); setForeground(MainFrame.TEXT_MUTED);
                }
                return this;
            }
        };

        // Name renderer ✅ >> instead of star
        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(MainFrame.BG_CARD);
                String text = val == null ? "" : val.toString();
                if (text.startsWith(">>")) {
                    setForeground(MainFrame.ACCENT_GREEN);
                    setFont(new Font("Monospaced", Font.BOLD, 13));
                } else {
                    setForeground(MainFrame.TEXT_PRIMARY);
                    setFont(new Font("Monospaced", Font.PLAIN, 13));
                }
                return this;
            }
        };

        // P&L renderer
        DefaultTableCellRenderer pnlRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBackground(MainFrame.BG_CARD);
                setFont(new Font("Monospaced", Font.PLAIN, 13));
                String text = val == null ? "" : val.toString();
                if (text.startsWith("+")) setForeground(MainFrame.ACCENT_GREEN);
                else if (text.startsWith("-")) setForeground(MainFrame.ACCENT_RED);
                else setForeground(MainFrame.TEXT_MUTED);
                return this;
            }
        };

        // Value renderer
        DefaultTableCellRenderer valueRenderer = new DefaultTableCellRenderer() {
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

        table.getColumnModel().getColumn(0).setCellRenderer(rankRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(nameRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(valueRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(pnlRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(pnlRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(MainFrame.BG_DARK);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    // ─────────────────────────────────────────────────────────
    //  Footer
    // ─────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 0, 0));
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, MainFrame.BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        rankLabel  = buildStatCard("Your Rank",   "#-",         MainFrame.ACCENT_GREEN);
        scoreLabel = buildStatCard("Total Value", "Rs.10,000.00", MainFrame.TEXT_PRIMARY);
        pnlLabel   = buildStatCard("Your Return", "0.00%",      MainFrame.TEXT_MUTED);

        bar.add(rankLabel);
        bar.add(scoreLabel);
        bar.add(pnlLabel);
        return bar;
    }

    private JLabel buildStatCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Monospaced", Font.PLAIN, 11));
        t.setForeground(MainFrame.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Monospaced", Font.BOLD, 16));
        v.setForeground(valueColor);

        card.add(t);
        card.add(v);
        return v;
    }

    // ─────────────────────────────────────────────────────────
    //  AI simulation
    // ─────────────────────────────────────────────────────────
    private void startAiSimulation() {
        aiTimer = new Timer(3000, e -> {
            for (int i = 0; i < aiTraders.size(); i++) {
                double drift = (random.nextDouble() - 0.47) * aiCashValues[i] * 0.008;
                aiPortfolioValues[i] = Math.max(0, aiPortfolioValues[i] + drift);

                if (random.nextInt(5) == 0) {
                    double trade = (random.nextDouble() - 0.5) * 500;
                    aiCashValues[i]     = Math.max(0, aiCashValues[i] + trade);
                    aiPortfolioValues[i] = Math.max(0, aiPortfolioValues[i] - trade);
                }

                aiTraders.get(i).setCurrentCash(aiCashValues[i]);
                aiTraders.get(i).setPortfolioValue(aiPortfolioValues[i]);
            }
            SwingUtilities.invokeLater(this::refreshLeaderboard);
        });
        aiTimer.start();
    }

    // ─────────────────────────────────────────────────────────
    //  Public refresh
    // ─────────────────────────────────────────────────────────
    public void refresh(double currentCash, double portfolioValue) {
        humanEntry.setCurrentCash(currentCash);
        humanEntry.setPortfolioValue(portfolioValue);
        SwingUtilities.invokeLater(this::refreshLeaderboard);
    }

    // ─────────────────────────────────────────────────────────
    //  Rebuild table ✅ >> instead of star, green rank colors
    // ─────────────────────────────────────────────────────────
    private void refreshLeaderboard() {
        List<LeaderboardEntry> all = new ArrayList<>(aiTraders);
        all.add(humanEntry);
        Collections.sort(all);

        tableModel.setRowCount(0);

        for (int i = 0; i < all.size(); i++) {
            LeaderboardEntry entry = all.get(i);
            String name = entry.isHuman()
                    ? ">>  " + entry.getName() + "  (You)"
                    : "    " + entry.getName();

            tableModel.addRow(new Object[]{
                String.valueOf(i + 1),
                name,
                "Rs." + String.format("%,.2f", entry.getTotalValue()),
                entry.getFormattedPnl(),
                entry.getFormattedPnlPercent()
            });
        }

        // Footer update
        int rank = humanEntry.getRank(all);
        rankLabel.setText("#" + rank);
        rankLabel.setForeground(rank == 1
                ? MainFrame.ACCENT_GREEN
                : MainFrame.ACCENT_GREEN_DIM);

        scoreLabel.setText("Rs." + String.format("%,.2f", humanEntry.getTotalValue()));

        double pct = humanEntry.getPnlPercent();
        pnlLabel.setText(humanEntry.getFormattedPnlPercent());
        pnlLabel.setForeground(pct >= 0 ? MainFrame.ACCENT_GREEN : MainFrame.ACCENT_RED);
    }
}