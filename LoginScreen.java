package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import model.User;

public class LoginScreen extends JFrame {

    private static final int WIDTH  = 900;
    private static final int HEIGHT = 600;

    private java.util.function.Consumer<User> onLogin;

    private JTextField    nameField;
    private JComboBox<String> capitalCombo;
    private JButton       startButton;
    private JLabel        errorLabel;

    private static final String[] CAPITAL_OPTIONS = {
        "₹10,000  —  Beginner",
        "₹50,000  —  Intermediate",
        "₹1,00,000  —  Advanced",
        "₹5,00,000  —  Pro Trader",
        "₹10,00,000  —  Hedge Fund"
    };

    private static final double[] CAPITAL_VALUES = {
        10000, 50000, 100000, 500000, 1000000
    };

    public LoginScreen(java.util.function.Consumer<User> onLogin) {
        this.onLogin = onLogin;

        setUndecorated(true);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(buildBackground());
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────
    //  Background panel
    // ─────────────────────────────────────────────────────────
    private JPanel buildBackground() {
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                // Background
                g2.setColor(new Color(5, 8, 12));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle dot grid
                g2.setColor(new Color(0, 255, 70, 15));
                for (int x = 0; x < getWidth(); x += 30)
                    for (int y = 0; y < getHeight(); y += 30)
                        g2.fillOval(x, y, 2, 2);

                // Top-left glow
                g2.setPaint(new RadialGradientPaint(
                    0, 0, 350,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 255, 70, 25), new Color(0,0,0,0)}
                ));
                g2.fillRect(0, 0, 350, 350);

                // Bottom-right glow
                g2.setPaint(new RadialGradientPaint(
                    getWidth(), getHeight(), 350,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 100, 255, 20), new Color(0,0,0,0)}
                ));
                g2.fillRect(getWidth()-350, getHeight()-350, 350, 350);
            }
        };
        bg.add(buildCard());
        return bg;
    }

    // ─────────────────────────────────────────────────────────
    //  Card
    // ─────────────────────────────────────────────────────────
    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(13, 17, 23));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0, 255, 70, 60));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 460));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        // ── Logo ──────────────────────────────────────────────
        JLabel logo = new JLabel("◈  TradeQuest", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(new Color(0, 255, 70));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logo);

        card.add(Box.createVerticalStrut(6));

        JLabel tagline = new JLabel("Paper Trading Simulator", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(110, 118, 129));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tagline);

        card.add(Box.createVerticalStrut(18));

        // ── Divider ───────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(48, 54, 61));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep);

        card.add(Box.createVerticalStrut(18));

        // ── Name label ────────────────────────────────────────
        card.add(buildLabel("Trader Name"));
        card.add(Box.createVerticalStrut(6));

        // ── Name field ────────────────────────────────────────
        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBackground(new Color(22, 27, 34));
        nameField.setForeground(new Color(230, 237, 243));
        nameField.setCaretColor(new Color(0, 255, 70));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(48, 54, 61), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        nameField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 255, 70, 150), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(48, 54, 61), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        nameField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        });
        card.add(nameField);

        card.add(Box.createVerticalStrut(16));

        // ── Capital label ─────────────────────────────────────
        card.add(buildLabel("Starting Capital"));
        card.add(Box.createVerticalStrut(6));

        // ── Capital dropdown ──────────────────────────────────
        capitalCombo = new JComboBox<>(CAPITAL_OPTIONS);
        capitalCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        capitalCombo.setBackground(new Color(22, 27, 34));
        capitalCombo.setForeground(new Color(230, 237, 243));
        capitalCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        capitalCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(30, 37, 46) : new Color(22, 27, 34));
                setForeground(isSelected ? new Color(0, 255, 70) : new Color(230, 237, 243));
                setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
        card.add(capitalCombo);

        card.add(Box.createVerticalStrut(10));

        // ── Error label ───────────────────────────────────────
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 53, 69));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(errorLabel);

        card.add(Box.createVerticalStrut(8));

        // ── Start button ──────────────────────────────────────
        startButton = new JButton("START TRADING") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())       g2.setColor(new Color(0, 180, 50));
                else if (getModel().isRollover()) g2.setColor(new Color(0, 220, 60));
                else                              g2.setColor(new Color(0, 255, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        startButton.setFocusPainted(false);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> handleLogin());
        card.add(startButton);

        card.add(Box.createVerticalStrut(20));

        // ── Footer ────────────────────────────────────────────
        JLabel footer = new JLabel(
            "Simulated trading only  •  No real money involved",
            SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(new Color(48, 54, 61));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(footer);

        return card;
    }

    // ─────────────────────────────────────────────────────────
    //  Helper
    // ─────────────────────────────────────────────────────────
    private JLabel buildLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(110, 118, 129));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ─────────────────────────────────────────────────────────
    //  Login handler
    // ─────────────────────────────────────────────────────────
    private void handleLogin() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            errorLabel.setText("⚠  Please enter your trader name");
            nameField.requestFocus();
            return;
        }
        if (name.length() < 2) {
            errorLabel.setText("⚠  Name must be at least 2 characters");
            nameField.requestFocus();
            return;
        }

        double capital = CAPITAL_VALUES[capitalCombo.getSelectedIndex()];
        User user = new User(name, capital);

        startButton.setEnabled(false);
        Timer delay = new Timer(200, e -> {
            dispose();
            onLogin.accept(user);
        });
        delay.setRepeats(false);
        delay.start();
    }
}