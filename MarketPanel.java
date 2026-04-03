package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import service.TradeService;
import service.PriceService;
import model.*;

public class MarketPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private User user;
    private TradeService tradeService;
    private PriceService priceService;
    private Timer priceTimer;
    private Random random = new Random();
    private PortfolioPanel portfolioPanel;

    // Stock data — loaded from PriceService
    private String[] symbols;
    private String[] sectors;
    private double[] prices;
    private double[] changes;

    // Search + filter state
    private String searchText     = "";
    private String selectedSector = "All";

    // UI
    private JLabel cashDisplay;
    private JTextField searchField;
    private JComboBox<String> sectorCombo;

    public MarketPanel(User user, PriceService priceService) {
        this.user         = user;
        this.tradeService = new TradeService();
        this.priceService = priceService;

        String[] allSymbols = PriceService.getDisplaySymbols();
        symbols  = allSymbols;
        prices   = new double[symbols.length];
        changes  = new double[symbols.length];
        sectors  = new String[symbols.length];

        Map<String, Double> lastPrices = priceService.getLastKnownPrices();
        for (int i = 0; i < symbols.length; i++) {
            prices[i]  = lastPrices.getOrDefault(symbols[i], 100.0);
            changes[i] = 0;
            sectors[i] = PriceService.getSector(symbols[i]);
        }

        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.BG_DARK);

        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
        add(buildTradeBar(), BorderLayout.SOUTH);

        startPriceSimulation();
    }

    public void setPortfolioPanel(PortfolioPanel portfolioPanel) {
        this.portfolioPanel = portfolioPanel;
    }
    public service.TradeService getTradeService() {
     return tradeService;
    }

    // ─────────────────────────────────────────────────────────
    //  Top bar
    // ─────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Left — title + cash
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // ✅ UPDATED — monospaced, green, no emoji
        JLabel title = new JLabel("[ LIVE MARKET ]  //  Nifty 50");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(MainFrame.ACCENT_GREEN);

        cashDisplay = new JLabel("Cash: Rs." + String.format("%,.2f", user.getCash()));
        cashDisplay.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cashDisplay.setForeground(MainFrame.ACCENT_GREEN_DIM);

        left.add(title);
        left.add(cashDisplay);

        // Right — search + sector filter
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        // Search field
        searchField = new JTextField(14);
        searchField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        searchField.setBackground(MainFrame.BG_CARD);
        searchField.setForeground(MainFrame.TEXT_PRIMARY);
        searchField.setCaretColor(MainFrame.ACCENT_GREEN);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        searchField.setText("Search stocks...");
        searchField.setForeground(MainFrame.TEXT_MUTED);
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search stocks...")) {
                    searchField.setText("");
                    searchField.setForeground(MainFrame.TEXT_PRIMARY);
                }
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            new Color(0, 255, 70, 120), 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search stocks...");
                    searchField.setForeground(MainFrame.TEXT_MUTED);
                }
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MainFrame.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        });

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        // Sector filter dropdown
        String[] sectorOptions = {
            "All", "Financial", "IT", "Energy", "Consumer", "Auto", "Pharma"
        };
        sectorCombo = new JComboBox<>(sectorOptions);
        sectorCombo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        sectorCombo.setBackground(MainFrame.BG_CARD);
        sectorCombo.setForeground(MainFrame.TEXT_PRIMARY);
        sectorCombo.setPreferredSize(new Dimension(130, 34));
        sectorCombo.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));

        sectorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(13, 18, 13) : MainFrame.BG_CARD);
                setForeground(isSelected ? MainFrame.ACCENT_GREEN : MainFrame.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });

        sectorCombo.addActionListener(e -> {
            selectedSector = (String) sectorCombo.getSelectedItem();
            applyFilter();
        });

        right.add(searchField);
        right.add(sectorCombo);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    //  Table
    // ─────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] columns = {"Symbol", "Sector", "Price (Rs.)", "Change", "Change %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        populateTable();

        table = new JTable(tableModel);
        table.setBackground(MainFrame.BG_CARD);
        table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setGridColor(MainFrame.BORDER);
        // ✅ UPDATED — green selection
        table.setSelectionBackground(new Color(0, 255, 70, 40));
        table.setSelectionForeground(MainFrame.ACCENT_GREEN);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(false);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Header ✅ UPDATED — monospaced
        JTableHeader header = table.getTableHeader();
        header.setBackground(MainFrame.BG_PANEL);
        header.setForeground(MainFrame.TEXT_MUTED);
        header.setFont(new Font("Monospaced", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, MainFrame.BORDER));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);

        // Sector badge renderer ✅ updated colors to match green theme
        DefaultTableCellRenderer sectorRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                setBackground(MainFrame.BG_CARD);
                String text = val == null ? "" : val.toString();
                setForeground(getSectorColor(text));
                setFont(new Font("Monospaced", Font.BOLD, 11));
                return this;
            }
        };

        // Change renderer
        DefaultTableCellRenderer changeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
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

        // Right-align price
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
                setFont(new Font("Monospaced", Font.PLAIN, 13));
                return this;
            }
        };

        table.getColumnModel().getColumn(1).setCellRenderer(sectorRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(changeRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(changeRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(MainFrame.BG_DARK);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    // ─────────────────────────────────────────────────────────
    //  Sector colors ✅ updated to green-tinted palette
    // ─────────────────────────────────────────────────────────
    private Color getSectorColor(String sector) {
        switch (sector) {
            case "Financial": return new Color(0,  220, 180);
            case "IT":        return new Color(180, 255, 100);
            case "Energy":    return new Color(255, 200,  0);
            case "Consumer":  return new Color(0,   255, 70);
            case "Auto":      return new Color(255, 120, 80);
            case "Pharma":    return new Color(100, 220, 255);
            default:          return MainFrame.TEXT_MUTED;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Populate table
    // ─────────────────────────────────────────────────────────
    private void populateTable() {
        int selectedRow = (table != null) ? table.getSelectedRow() : -1;
        String selectedSymbol = null;
        if (selectedRow >= 0) {
            int modelRow = table != null
                    ? table.convertRowIndexToModel(selectedRow) : -1;
            if (modelRow >= 0)
                selectedSymbol = tableModel.getValueAt(modelRow, 0).toString();
        }

        tableModel.setRowCount(0);
        for (int i = 0; i < symbols.length; i++) {
            tableModel.addRow(new Object[]{
                symbols[i],
                sectors[i],
                String.format("%.2f", prices[i]),
                formatChange(changes[i]),
                formatChangePct(changes[i], prices[i])
            });
        }

        if (selectedSymbol != null) {
            for (int i = 0; i < table.getRowCount(); i++) {
                int modelRow = table.convertRowIndexToModel(i);
                if (tableModel.getValueAt(modelRow, 0)
                        .toString().equals(selectedSymbol)) {
                    table.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Search + sector filter
    // ─────────────────────────────────────────────────────────
    private void applyFilter() {
        String text = searchField.getText();
        if (text.equals("Search stocks...")) text = "";
        searchText = text.toUpperCase().trim();

        RowFilter<DefaultTableModel, Object> filter =
                new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel,
                    ? extends Object> entry) {
                String symbol = entry.getStringValue(0).toUpperCase();
                String sector = entry.getStringValue(1);
                boolean matchSearch = searchText.isEmpty()
                        || symbol.contains(searchText);
                boolean matchSector = selectedSector.equals("All")
                        || sector.equals(selectedSector);
                return matchSearch && matchSector;
            }
        };

        sorter.setRowFilter(filter);
    }

    // ─────────────────────────────────────────────────────────
    //  Trade bar
    // ─────────────────────────────────────────────────────────
    private JPanel buildTradeBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bar.setBackground(MainFrame.BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, MainFrame.BORDER));

        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setForeground(MainFrame.TEXT_MUTED);
        qtyLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JSpinner qtySpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 10000, 1));
        qtySpinner.setFont(new Font("Monospaced", Font.PLAIN, 13));
        qtySpinner.setPreferredSize(new Dimension(80, 34));
        styleSpinner(qtySpinner);

        JButton buyBtn  = buildButton("BUY",  MainFrame.ACCENT_GREEN);
        JButton sellBtn = buildButton("SELL", MainFrame.ACCENT_RED);

        buyBtn.addActionListener(e ->
                executeTrade(true,  (int) qtySpinner.getValue()));
        sellBtn.addActionListener(e ->
                executeTrade(false, (int) qtySpinner.getValue()));

        bar.add(qtyLabel);
        bar.add(qtySpinner);
        bar.add(sellBtn);
        bar.add(buyBtn);
        return bar;
    }

    private JButton buildButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(color.darker());
                else if (getModel().isRollover())
                    g2.setColor(color.brighter());
                else
                    g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(MainFrame.BG_CARD);
        spinner.setForeground(MainFrame.TEXT_PRIMARY);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(MainFrame.BG_CARD);
            tf.setForeground(MainFrame.TEXT_PRIMARY);
            tf.setCaretColor(MainFrame.ACCENT_GREEN);
            tf.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Trade execution
    // ─────────────────────────────────────────────────────────
    private void executeTrade(boolean isBuy, int qty) {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a stock first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        String symbol = symbols[modelRow];
        double price  = prices[modelRow];
        Stock  stock  = new Stock(symbol, price);

        try {
            if (isBuy) tradeService.buyStock(user, stock, qty);
            else       tradeService.sellStock(user, stock, qty);

            cashDisplay.setText("Cash: Rs."
                    + String.format("%,.2f", user.getCash()));
            firePropertyChange("portfolioUpdated", symbol, price);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Trade Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Called by MainFrame — real Yahoo prices
    // ─────────────────────────────────────────────────────────
    public void updatePrices(Map<String, Double> newPrices) {
        for (int i = 0; i < symbols.length; i++) {
            Double p = newPrices.get(symbols[i]);
            if (p != null) {
                changes[i] = p - prices[i];
                prices[i]  = p;
            }
        }
        SwingUtilities.invokeLater(this::populateTable);
    }

    // ─────────────────────────────────────────────────────────
    //  Local simulation — runs between Yahoo fetches
    // ─────────────────────────────────────────────────────────
    private void startPriceSimulation() {
        priceTimer = new Timer(2000, e -> {
            for (int i = 0; i < prices.length; i++) {
                double move = (random.nextDouble() - 0.48)
                        * prices[i] * 0.008;
                changes[i] = move;
                prices[i]  = Math.max(1, prices[i] + move);
            }
            SwingUtilities.invokeLater(() -> {
                populateTable();
                if (portfolioPanel != null) {
                    HashMap<String, Double> priceMap = new HashMap<>();
                    for (int i = 0; i < symbols.length; i++) {
                        priceMap.put(symbols[i], prices[i]);
                    }
                    portfolioPanel.updateMarketPrices(priceMap);
                }
            });
        });
        priceTimer.start();
    }

    // ─────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────
    private String formatChange(double change) {
        return (change >= 0 ? "+" : "")
                + String.format("%.2f", change);
    }

    private String formatChangePct(double change, double price) {
        if (price - change == 0) return "0.00%";
        double pct = (change / (price - change)) * 100;
        return (pct >= 0 ? "+" : "")
                + String.format("%.2f", pct) + "%";
    }
}