package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import model.User;
import service.PriceService;

public class MainFrame extends JFrame {

    // ── Green/Black Brand Colors ──────────────────────────────
    public static final Color BG_DARK          = new Color(0, 0, 0);
    public static final Color BG_PANEL         = new Color(8, 12, 8);
    public static final Color BG_CARD          = new Color(13, 18, 13);
    public static final Color ACCENT_GREEN     = new Color(0, 255, 70);
    public static final Color ACCENT_GREEN_DIM = new Color(0, 180, 50);
    public static final Color ACCENT_RED       = new Color(220, 53, 69);
    public static final Color ACCENT_BLUE      = new Color(0, 255, 70);
    public static final Color TEXT_PRIMARY     = new Color(200, 240, 200);
    public static final Color TEXT_MUTED       = new Color(80, 120, 80);
    public static final Color BORDER           = new Color(0, 60, 20);

    // ── Fonts ─────────────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("Segoe UI",    Font.BOLD,  15);
    public static final Font FONT_BODY  = new Font("Segoe UI",    Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI",    Font.PLAIN, 11);
    public static final Font FONT_MONO  = new Font("Monospaced",  Font.PLAIN, 13);

    private JLabel statusLabel;

    // Panels
    private MarketPanel      marketPanel;
    private PortfolioPanel   portfolioPanel;
    private LeaderboardPanel leaderboardPanel;
    private TradeLogPanel    tradeLogPanel;      // ✅ ADDED

    // Services
    private PriceService priceService;
    private Timer        priceRefreshTimer;

    // User
    private User user;

    public MainFrame(User user) {
        this.user = user;

        applyGlobalLookAndFeel();

        setTitle("TradeQuest  |  Paper Trading Simulator");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setBackground(BG_DARK);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // ── Init services ─────────────────────────────────────
        priceService = new PriceService();

        // ── Init panels ───────────────────────────────────────
        portfolioPanel   = new PortfolioPanel(user);
        marketPanel      = new MarketPanel(user, priceService);
        leaderboardPanel = new LeaderboardPanel(user);
        tradeLogPanel    = new TradeLogPanel(user, marketPanel.getTradeService()); // ✅ ADDED

        marketPanel.setPortfolioPanel(portfolioPanel);

        // ✅ UPDATED — tradeLogPanel.refresh() added
        marketPanel.addPropertyChangeListener("portfolioUpdated", e -> {
            String symbol = (String) e.getOldValue();
            double price  = (double) e.getNewValue();
            portfolioPanel.refresh(symbol, price);

            double portfolioValue = calculatePortfolioValue();
            leaderboardPanel.refresh(user.getCash(), portfolioValue);
            tradeLogPanel.refresh();  // ✅ ADDED

            updateStatus("Trade executed  —  " + symbol
                    + " @ Rs." + String.format("%.2f", price));
        });

        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildTabs(),      BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);

        startPriceFeed();
        updateStatus("Fetching live prices from Yahoo Finance...");
    }

    // ─────────────────────────────────────────────────────────
    //  Live price feed
    // ─────────────────────────────────────────────────────────
    private void startPriceFeed() {
        new Thread(() -> {
            Map<String, Double> prices = priceService.fetchPrices();
            SwingUtilities.invokeLater(() -> {
                marketPanel.updatePrices(prices);
                updateStatus("Live prices loaded  —  " + prices.size() + " stocks");
            });
        }).start();

        priceRefreshTimer = new Timer(5000, e -> {
            new Thread(() -> {
                Map<String, Double> prices = priceService.fetchPrices();
                SwingUtilities.invokeLater(() -> {
                    marketPanel.updatePrices(prices);
                    HashMap<String, Double> priceMap = new HashMap<>(prices);
                    portfolioPanel.updateMarketPrices(priceMap);

                    double portfolioValue = calculatePortfolioValue();
                    leaderboardPanel.refresh(user.getCash(), portfolioValue);

                    updateStatus("Prices updated  —  "
                            + java.time.LocalTime.now().format(
                              java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                });
            }).start();
        });
        priceRefreshTimer.start();
    }

    // ─────────────────────────────────────────────────────────
    //  Portfolio value calculator
    // ─────────────────────────────────────────────────────────
    private double calculatePortfolioValue() {
        Map<String, Double> prices = priceService.getLastKnownPrices();
        double total = 0;
        for (Map.Entry<String, Integer> entry : user.getPortfolio().entrySet()) {
            double price = prices.getOrDefault(entry.getKey(), 0.0);
            total += price * entry.getValue();
        }
        return total;
    }

    // ─────────────────────────────────────────────────────────
    //  Header
    // ─────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel logo = new JLabel("[ TRADEQUEST ]");
        logo.setFont(new Font("Monospaced", Font.BOLD, 22));
        logo.setForeground(ACCENT_GREEN);

        JLabel subtitle = new JLabel("Paper Trading Simulator  //  Nifty 50");
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 11));
        subtitle.setForeground(TEXT_MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(logo);
        left.add(subtitle);

        JLabel userLabel = new JLabel(
            user.getName().toUpperCase() + "  |  Rs." +
            String.format("%,.0f", user.getCash()));
        userLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        userLabel.setForeground(ACCENT_GREEN);

        JLabel liveLabel = new JLabel(">> LIVE MARKET");
        liveLabel.setFont(new Font("Monospaced", Font.BOLD, 11));
        liveLabel.setForeground(ACCENT_GREEN_DIM);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(userLabel);
        right.add(liveLabel);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ─────────────────────────────────────────────────────────
    //  Tabs ✅ UPDATED — Trade Log tab added
    // ─────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("Monospaced", Font.BOLD, 13));
        tabs.setBorder(BorderFactory.createEmptyBorder());

        tabs.setUI(new BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                highlight      = BG_PANEL;
                lightHighlight = BG_PANEL;
                shadow         = BORDER;
                darkShadow     = BORDER;
                focus          = ACCENT_GREEN;
            }
            @Override protected int calculateTabHeight(int p, int t, int h) { return 42; }
        });

        UIManager.put("TabbedPane.selected",           BG_CARD);
        UIManager.put("TabbedPane.background",         BG_DARK);
        UIManager.put("TabbedPane.foreground",         TEXT_PRIMARY);
        UIManager.put("TabbedPane.selectedForeground", ACCENT_GREEN);
        UIManager.put("TabbedPane.tabAreaBackground",  BG_PANEL);
        UIManager.put("TabbedPane.contentAreaColor",   BG_DARK);
        UIManager.put("TabbedPane.focus",              ACCENT_GREEN);

        tabs.addTab("  [ MARKET ]      ", marketPanel);
        tabs.addTab("  [ PORTFOLIO ]   ", portfolioPanel);
        tabs.addTab("  [ LEADERBOARD ] ", leaderboardPanel);
        tabs.addTab("  [ TRADE LOG ]   ", tradeLogPanel);  // ✅ ADDED

        return tabs;
    }

    // ─────────────────────────────────────────────────────────
    //  Status bar
    // ─────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(6, 20, 6, 20)
        ));

        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_MUTED);

        JLabel version = new JLabel("TradeQuest v2.0  //  Yahoo Finance  //  Nifty 50");
        version.setFont(new Font("Monospaced", Font.PLAIN, 11));
        version.setForeground(TEXT_MUTED);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(version,     BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    //  Global LAF
    // ─────────────────────────────────────────────────────────
    private void applyGlobalLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",             BG_DARK);
        UIManager.put("ScrollPane.background",        BG_DARK);
        UIManager.put("Viewport.background",          BG_DARK);
        UIManager.put("Table.background",             BG_CARD);
        UIManager.put("Table.foreground",             TEXT_PRIMARY);
        UIManager.put("Table.gridColor",              BORDER);
        UIManager.put("Table.selectionBackground",    new Color(0, 255, 70, 40));
        UIManager.put("Table.selectionForeground",    ACCENT_GREEN);
        UIManager.put("TableHeader.background",       BG_PANEL);
        UIManager.put("TableHeader.foreground",       TEXT_MUTED);
        UIManager.put("OptionPane.background",        BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Button.background",            BG_CARD);
        UIManager.put("Button.foreground",            TEXT_PRIMARY);
        UIManager.put("Label.foreground",             TEXT_PRIMARY);
        UIManager.put("ScrollBar.background",         BG_PANEL);
        UIManager.put("ScrollBar.thumb",              BORDER);
        UIManager.put("TextField.background",         BG_CARD);
        UIManager.put("TextField.foreground",         TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",    ACCENT_GREEN);
        UIManager.put("ComboBox.background",          BG_CARD);
        UIManager.put("ComboBox.foreground",          TEXT_PRIMARY);
        UIManager.put("Spinner.background",           BG_CARD);
        UIManager.put("Spinner.foreground",           TEXT_PRIMARY);
    }

    // ─────────────────────────────────────────────────────────
    //  Status flash
    // ─────────────────────────────────────────────────────────
    public void updateStatus(String msg) {
        statusLabel.setText(">  " + msg);
        statusLabel.setForeground(ACCENT_GREEN);
        Timer t = new Timer(4000, e -> statusLabel.setForeground(TEXT_MUTED));
        t.setRepeats(false);
        t.start();
    }
}