package game.mainhall;

import game.AssetLoader;
import game.AssetLoader.Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Sảnh chính (Main Hall): nền + nhạc + Play / Settings / Exit. */
public class MainHall extends JPanel {

    private final int WIDTH, HEIGHT;

    // Ảnh nền (ABS ưu tiên, rồi classpath)
    private static final String MAIN_BG_ABS =
            "C:\\Users\\Admin\\Downloads\\basic-arkanoid\\src\\resources\\backgrounds\\mainbackground.jpg";

    // Nhạc sảnh chính (ABS ưu tiên, rồi classpath)
    private static final String MAIN_MUSIC_ABS =
            "C:\\Users\\Admin\\Downloads\\basic-arkanoid\\src\\resources\\sounds\\music.mp3";
    private static final String MAIN_MUSIC_CP = "sounds/music.mp3";

    private Image bg;
    private Music bgm;
    private boolean musicEnabled = true;

    // Nút sảnh
    private final Rectangle btnPlay = new Rectangle();
    private final Rectangle btnSettings = new Rectangle();
    private final Rectangle btnExit = new Rectangle();

    // Settings trong sảnh
    private boolean inSettings = false;
    private final Rectangle btnMusicToggle = new Rectangle();
    private final Rectangle btnBack = new Rectangle();

    // Callback cho cửa sổ chính
    public interface Listener {
        void onStartGame();
        void onExitRequested();
    }
    private final Listener listener;

    public MainHall(int w, int h, Listener listener) {
        this.WIDTH = w; this.HEIGHT = h; this.listener = listener;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);

        loadBackground();

        // Tự động phát nhạc sảnh khi khởi chạy
        playBgm();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleClick(e.getPoint()); }
        });

        // SPACE => Play
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "play");
        getActionMap().put("play", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                stopHallMusic(); // dừng nhạc sảnh trước khi vào game
                if (listener != null) listener.onStartGame();
            }
        });
    }

    /* ================= Assets ================= */
    private void loadBackground() {
        try {
            java.io.File f = new java.io.File(MAIN_BG_ABS);
            if (f.exists())
                bg = javax.imageio.ImageIO.read(f).getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
            else
                bg = AssetLoader.scaled("backgrounds/mainbackground.jpg", WIDTH, HEIGHT);
        } catch (Exception ignored) {}
    }

    private void playBgm() {
        stopHallMusic();
        if (!musicEnabled) return;
        // Ưu tiên đường dẫn tuyệt đối, fallback classpath
        bgm = AssetLoader.loopMusicFromFile(MAIN_MUSIC_ABS);
        if (bgm == null) bgm = AssetLoader.loopMusicFromResource(MAIN_MUSIC_CP);
    }

    /** DỪNG NHẠC SẢNH (public để GameWindow gọi trước khi vào game). */
    public void stopHallMusic() {
        if (bgm != null) { bgm.stop(); bgm = null; }
    }

    /** Khi panel bị tháo khỏi frame, auto dừng nhạc để không lẫn với nhạc level. */
    @Override public void removeNotify() {
        super.removeNotify();
        stopHallMusic();
    }

    /* ================= Layout ================= */
    private void layoutMenuButtons() {
        int bw = 280, bh = 52, gap = 16;
        int cx = (getWidth()-bw)/2;
        int cy = getHeight()/2 - bh - gap;
        btnPlay.setBounds(cx, cy, bw, bh);
        btnSettings.setBounds(cx, cy + (bh + gap), bw, bh);
        btnExit.setBounds(cx, cy + 2*(bh + gap), bw, bh);
    }

    private void layoutSettingsButtons() {
        int bw = 320, bh = 48, gap = 14;
        int cx = (getWidth()-bw)/2;
        int cy = getHeight()/2 - bh;
        btnMusicToggle.setBounds(cx, cy, bw, bh);
        btnBack.setBounds(cx, cy + (bh + gap), bw, bh);
    }

    /* ================= Render ================= */
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bg != null) g2.drawImage(bg, 0, 0, null);
        else {
            g2.setPaint(new GradientPaint(0,0,new Color(12,12,28),0,getHeight(),Color.BLACK));
            g2.fillRect(0,0,getWidth(),getHeight());
        }

        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 44));
        String title = "ARKANOID";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth()-tw)/2, getHeight()/3);

        if (!inSettings) {
            layoutMenuButtons();
            drawBtn(g2, btnPlay, "PLAY");
            drawBtn(g2, btnSettings, "SETTINGS");
            drawBtn(g2, btnExit, "EXIT");
            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.drawString("Press SPACE to Play", 20, getHeight()-20);
        } else {
            layoutSettingsButtons();
            drawBtn(g2, btnMusicToggle, musicEnabled ? "Music: ON (pause/resume)" : "Music: OFF");
            drawBtn(g2, btnBack, "Back");
        }
    }

    private void drawBtn(Graphics2D g2, Rectangle r, String text) {
        g2.setColor(new Color(255,255,255,36));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        int tw = g2.getFontMetrics().stringWidth(text);
        int th = g2.getFontMetrics().getAscent();
        g2.drawString(text, r.x + (r.width - tw)/2, r.y + (r.height + th)/2 - 4);
    }

    /* ================= Input ================= */
    private void handleClick(Point p) {
        if (!inSettings) {
            if (btnPlay.contains(p)) {
                stopHallMusic(); // dừng nhạc sảnh trước khi vào game
                if (listener != null) listener.onStartGame();
            } else if (btnSettings.contains(p)) {
                inSettings = true; repaint();
            } else if (btnExit.contains(p)) {
                if (listener != null) listener.onExitRequested();
            }
        } else {
            if (btnMusicToggle.contains(p)) {
                musicEnabled = !musicEnabled;
                if (!musicEnabled) { if (bgm != null) bgm.pause(); }
                else { if (bgm != null) bgm.resume(); else playBgm(); }
                repaint();
            } else if (btnBack.contains(p)) {
                inSettings = false; repaint();
            }
        }
    }
}
