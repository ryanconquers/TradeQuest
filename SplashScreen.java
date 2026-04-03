package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SplashScreen extends JWindow {

    private static final int WIDTH  = 900;
    private static final int HEIGHT = 600;

    // Matrix settings
    private static final int FONT_SIZE   = 16;
    private static final int COLS        = WIDTH / FONT_SIZE;
    private static final int ROWS        = HEIGHT / FONT_SIZE;

    // Matrix characters — katakana + digits
    private static final char[] CHARS = (
        "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン" +
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    ).toCharArray();

    // Matrix state
    private int[]    drops    = new int[COLS];       // y position of each column drop
    private char[][] grid     = new char[ROWS][COLS]; // current char at each cell
    private float[]  opacity  = new float[COLS];      // opacity of each column

    // Title reveal state
    private static final String TITLE    = "TRADEQUEST";
    private static final String SUBTITLE = "Your Paper Trading Simulator";
    private int    revealedChars  = 0;
    private boolean titleVisible  = false;
    private boolean subtitleVisible = false;
    private boolean progressVisible = false;
    private float   subtitleAlpha = 0f;
    private float   progressValue = 0f;

    // Timers
    private Timer matrixTimer;
    private Timer revealTimer;
    private Timer progressTimer;
    private Timer transitionTimer;

    // Callback — called when splash is done
    private Runnable onComplete;

    private Random random = new Random();
    private MatrixPanel matrixPanel;

    public SplashScreen(Runnable onComplete) {
        this.onComplete = onComplete;

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        // Init drops at random positions
        for (int i = 0; i < COLS; i++) {
            drops[i]   = random.nextInt(ROWS);
            opacity[i] = 0.3f + random.nextFloat() * 0.7f;
        }

        // Init grid with random chars
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = CHARS[random.nextInt(CHARS.length)];

        matrixPanel = new MatrixPanel();
        add(matrixPanel);

        setVisible(true);

        startMatrix();
        scheduleReveal();
    }

    // ─────────────────────────────────────────────────────────
    //  Matrix rain timer — 60fps
    // ─────────────────────────────────────────────────────────
    private void startMatrix() {
        matrixTimer = new Timer(50, e -> {
            for (int c = 0; c < COLS; c++) {
                // Randomize char at drop head
                grid[drops[c]][c] = CHARS[random.nextInt(CHARS.length)];

                // Advance drop
                drops[c]++;
                if (drops[c] >= ROWS) {
                    drops[c]   = 0;
                    opacity[c] = 0.3f + random.nextFloat() * 0.7f;
                }
            }
            matrixPanel.repaint();
        });
        matrixTimer.start();
    }

    // ─────────────────────────────────────────────────────────
    //  Schedule title reveal after 1.5 seconds
    // ─────────────────────────────────────────────────────────
    private void scheduleReveal() {
        Timer delay = new Timer(1500, e -> {
            titleVisible = true;
            startTitleReveal();
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void startTitleReveal() {
        revealTimer = new Timer(80, e -> {
            if (revealedChars < TITLE.length()) {
                revealedChars++;
                matrixPanel.repaint();
            } else {
                ((Timer) e.getSource()).stop();
                // Show subtitle after title is fully revealed
                Timer subtitleDelay = new Timer(400, e2 -> {
                    subtitleVisible = true;
                    startSubtitleFade();
                });
                subtitleDelay.setRepeats(false);
                subtitleDelay.start();
            }
        });
        revealTimer.start();
    }

    private void startSubtitleFade() {
        Timer fadeTimer = new Timer(30, e -> {
            subtitleAlpha = Math.min(1f, subtitleAlpha + 0.05f);
            matrixPanel.repaint();
            if (subtitleAlpha >= 1f) {
                ((Timer) e.getSource()).stop();
                // Show progress bar
                Timer progressDelay = new Timer(300, e2 -> {
                    progressVisible = true;
                    startProgressBar();
                });
                progressDelay.setRepeats(false);
                progressDelay.start();
            }
        });
        fadeTimer.start();
    }

    private void startProgressBar() {
        progressTimer = new Timer(30, e -> {
            progressValue = Math.min(1f, progressValue + 0.008f);
            matrixPanel.repaint();
            if (progressValue >= 1f) {
                ((Timer) e.getSource()).stop();
                startTransition();
            }
        });
        progressTimer.start();
    }

    // ─────────────────────────────────────────────────────────
    //  Transition — fade out and call onComplete
    // ─────────────────────────────────────────────────────────
    private void startTransition() {
        Timer wait = new Timer(500, e -> {
            matrixTimer.stop();
            dispose();
            onComplete.run();
        });
        wait.setRepeats(false);
        wait.start();
    }

    // ─────────────────────────────────────────────────────────
    //  Matrix Panel — custom painting
    // ─────────────────────────────────────────────────────────
    private class MatrixPanel extends JPanel {

        private static final Color BG_COLOR     = new Color(0, 0, 0);
        private static final Color GREEN_BRIGHT = new Color(0, 255, 70);
        private static final Color GREEN_MID    = new Color(0, 200, 50);
        private static final Color GREEN_DARK   = new Color(0, 100, 20);
        private static final Color GREEN_FADE   = new Color(0, 40, 10);

        private Font matrixFont;
        private Font titleFont;
        private Font subtitleFont;
        private Font progressFont;

        public MatrixPanel() {
            setBackground(BG_COLOR);
            matrixFont   = new Font("Monospaced", Font.PLAIN, FONT_SIZE);
            titleFont    = new Font("Segoe UI",   Font.BOLD,  72);
            subtitleFont = new Font("Segoe UI",   Font.PLAIN, 18);
            progressFont = new Font("Segoe UI",   Font.PLAIN, 12);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background
            g2.setColor(BG_COLOR);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw matrix rain
            g2.setFont(matrixFont);
            for (int c = 0; c < COLS; c++) {
                for (int r = 0; r < ROWS; r++) {
                    int x = c * FONT_SIZE;
                    int y = r * FONT_SIZE;

                    // Drop head — brightest
                    if (r == drops[c]) {
                        g2.setColor(GREEN_BRIGHT);
                    }
                    // Just behind head
                    else if (r == drops[c] - 1) {
                        g2.setColor(new Color(180, 255, 180));
                    }
                    // Trail — fades with distance from head
                    else {
                        int dist = (drops[c] - r + ROWS) % ROWS;
                        if (dist < 8) {
                            float fade = 1f - (dist / 8f);
                            g2.setColor(new Color(0,
                                (int)(200 * fade * opacity[c]),
                                (int)(50 * fade * opacity[c])));
                        } else {
                            g2.setColor(GREEN_FADE);
                        }
                    }

                    g2.drawString(String.valueOf(grid[r][c]), x, y + FONT_SIZE);
                }
            }

            // Dark overlay in center for title readability
            if (titleVisible) {
                GradientPaint vignette = new GradientPaint(
                    getWidth() / 2f, getHeight() / 2f - 100,
                    new Color(0, 0, 0, 220),
                    getWidth() / 2f, getHeight() / 2f + 100,
                    new Color(0, 0, 0, 180)
                );
                g2.setPaint(vignette);
                g2.fillRoundRect(getWidth() / 2 - 350, getHeight() / 2 - 120,
                                 700, 240, 20, 20);
            }

            // Draw title — letter by letter reveal
            if (titleVisible && revealedChars > 0) {
                g2.setFont(titleFont);
                String revealed = TITLE.substring(0, revealedChars);

                FontMetrics fm   = g2.getFontMetrics();
                int totalWidth   = fm.stringWidth(TITLE);
                int startX       = (getWidth() - totalWidth) / 2;
                int titleY       = getHeight() / 2 - 20;

                // Shadow
                g2.setColor(new Color(0, 255, 70, 40));
                g2.drawString(revealed, startX + 3, titleY + 3);

                // Main title — bright green
                g2.setColor(GREEN_BRIGHT);
                g2.drawString(revealed, startX, titleY);

                // Blinking cursor at end of revealed text
                if (revealedChars < TITLE.length()) {
                    int cursorX = startX + fm.stringWidth(revealed);
                    long time   = System.currentTimeMillis();
                    if ((time / 300) % 2 == 0) {
                        g2.setColor(GREEN_BRIGHT);
                        g2.fillRect(cursorX + 2, titleY - fm.getAscent(),
                                    6, fm.getHeight());
                    }
                }
            }

            // Draw subtitle with fade
            if (subtitleVisible && subtitleAlpha > 0) {
                g2.setFont(subtitleFont);
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, subtitleAlpha));

                FontMetrics fm = g2.getFontMetrics();
                int subX = (getWidth() - fm.stringWidth(SUBTITLE)) / 2;
                int subY = getHeight() / 2 + 40;

                g2.setColor(GREEN_MID);
                g2.drawString(SUBTITLE, subX, subY);

                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 1f));
            }

            // Draw progress bar
            if (progressVisible) {
                int barW = 400;
                int barH = 4;
                int barX = (getWidth() - barW) / 2;
                int barY = getHeight() / 2 + 80;

                // Track
                g2.setColor(new Color(0, 60, 20));
                g2.fillRoundRect(barX, barY, barW, barH, barH, barH);

                // Fill
                g2.setColor(GREEN_BRIGHT);
                g2.fillRoundRect(barX, barY,
                        (int)(barW * progressValue), barH, barH, barH);

                // Glow effect on fill
                g2.setColor(new Color(0, 255, 70, 60));
                g2.fillRoundRect(barX, barY - 2,
                        (int)(barW * progressValue), barH + 4, barH, barH);

                // Progress text
                g2.setFont(progressFont);
                g2.setColor(GREEN_DARK);
                String pText = "Loading market data...  "
                        + (int)(progressValue * 100) + "%";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(pText,
                        (getWidth() - fm.stringWidth(pText)) / 2,
                        barY + 20);
            }
        }
    }
}