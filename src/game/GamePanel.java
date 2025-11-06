package game;

import game.objects.Ball;
import game.objects.Brick;
import game.objects.Paddle;
import game.powerup.BonusBalls;
import game.powerup.ExpandPaddle;
import game.powerup.Gun;
import game.powerup.Lazer;

import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {

    // ===== màn hình & loop =====
    private final int WIDTH, HEIGHT;
    private final Timer timer = new Timer(1000 / 60, this); // javax.swing.Timer

    // ===== state =====
    private enum State { MENU, PLAY, PAUSE, SETTINGS, GAMEOVER, WIN }
    private State state = State.MENU;

    // ===== managers =====
    private final LevelManager levels;
    private final MusicHub music = new MusicHub();

    // ===== game objects =====
    private final List<Ball> balls = new ArrayList<>();
    private Paddle paddle;

    // ===== UI buttons =====
    private final Rectangle pauseBtn = new Rectangle();
    private final Rectangle homeBtn  = new Rectangle();
    private final Rectangle settingsBtn = new Rectangle();
    private final int btnW = 26, btnH = 26, btnPad = 10;
    private Image pauseIcon, homeIcon, gearIcon;
    private Image mainBg;

    // ===== HUD =====
    private int score = 0, lives = 3, levelIndex = 0; // 0-based
    private final int TOTAL_LEVELS = 5;

    // ===== Settings overlay =====
    private final Rectangle btnMusicToggle = new Rectangle();
    private final Rectangle btnMainMenu    = new Rectangle();
    private final Rectangle btnBack        = new Rectangle();

    // ===== rng & pickups =====
    private final Random rng = new Random();
    private final List<Pickup> pickups = new ArrayList<>();

    // ===== bullets & laser =====
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<LaserRay> lasers = new ArrayList<>();
    private long gunUntil = 0, nextBulletAt = 0;

    // ===== expand paddle =====
    private long expandUntil = 0;
    private Integer paddleOrigW = null;

    // ===== input =====
    private boolean left, right;

    public GamePanel(int w, int h) {
        this.WIDTH = w; this.HEIGHT = h;

        setPreferredSize(new Dimension(w, h));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        levels = new LevelManager(w, h);
        paddle = new Paddle(WIDTH / 2.0 - 50, HEIGHT - 40, 100, 12, 6);
        balls.add(new Ball(WIDTH / 2.0, HEIGHT - 60, 8, 4, -4));

        try { pauseIcon  = AssetLoader.scaled("images/pause.png",  btnW, btnH); } catch (Exception ignored) {}
        try { homeIcon   = AssetLoader.scaled("images/home.png",   btnW, btnH); } catch (Exception ignored) {}
        try { gearIcon   = AssetLoader.scaled("images/gear.png",   btnW, btnH); } catch (Exception ignored) {}
        try { mainBg     = AssetLoader.scaled("backgrounds/mainbackground.jpg", w, h); } catch (Exception ignored) {}

        levels.load(levelIndex);
        music.playMenu(); // khởi động ở MENU → phát nhạc sảnh

        timer.start();
    }

    // ================= loop =================
    @Override public void actionPerformed(ActionEvent e) {
        if (state == State.PLAY) update();
        repaint();
    }

    private void update() {
        // paddle
        if (left)  paddle.move(-1, WIDTH);
        if (right) paddle.move( 1, WIDTH);

        // balls
        for (int i = 0; i < balls.size(); i++) {
            Ball b = balls.get(i);
            b.update();

            // walls
            if (b.x - b.r < 0)      { b.x = b.r;           b.vx = -b.vx; }
            if (b.x + b.r > WIDTH)  { b.x = WIDTH - b.r;   b.vx = -b.vx; }
            if (b.y - b.r < 0)      { b.y = b.r;           b.vy = -b.vy; }

            // lọt đáy
            if (b.y - b.r > HEIGHT) { balls.remove(i--); continue; }

            // paddle
            if (b.getRect().intersects(paddle.getRect()) && b.vy > 0) {
                b.y = paddle.y - b.r - 1;
                b.vy = -b.vy;
                if (left && !right)  b.vx -= 1;
                if (right && !left)  b.vx += 1;
            }

            // bricks
            Rectangle br = b.getRect();
            for (int j = 0; j < levels.bricks.length; j++) {
                Brick brick = levels.bricks[j];
                if (brick == null) continue;
                if (!br.intersects(brick.getRect())) continue;

                // phản xạ cơ bản
                Rectangle r = brick.getRect();
                int leftO   = (int)(br.x + br.width) - r.x;
                int rightO  = (int)(r.x + r.width) - br.x;
                int topO    = (int)(br.y + br.height) - r.y;
                int bottomO = (int)(r.y + r.height) - br.y;
                if (Math.min(leftO, rightO) < Math.min(topO, bottomO)) b.vx = -b.vx; else b.vy = -b.vy;

                // phá gạch
                Brick removed = levels.bricks[j];
                levels.bricks[j] = null;
                score += 10;

                // 25% rơi power-up
                if (removed != null && rng.nextDouble() < 0.25) {
                    pickups.add(Pickup.randomAt(removed.x + removed.w / 2.0, removed.y + removed.h / 2.0));
                }
                break;
            }
        }

        // hết bóng → trừ mạng / respawn TRÊN PADDLE và PAUSE
        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) { state = State.GAMEOVER; music.stop(); return; }
            respawnBallOnPaddle();           // đặt bóng lên paddle hiện tại
            state = State.PAUSE;             // tạm dừng – ấn SPACE để đánh tiếp
            music.pause();
        }

        // pickups rơi
        for (int i = 0; i < pickups.size(); i++) {
            Pickup p = pickups.get(i);
            p.y += p.vy;
            if (p.y > HEIGHT) { pickups.remove(i--); continue; }
            if (p.getRect().intersects(paddle.getRect())) {
                applyPickup(p.type);
                pickups.remove(i--);
            }
        }

        // expand timer
        long now = System.currentTimeMillis();
        if (expandUntil > 0 && now >= expandUntil) {
            if (paddleOrigW != null) paddle.w = paddleOrigW;
            expandUntil = 0; paddleOrigW = null;
        }

        // gun auto fire
        if (now < gunUntil && now >= nextBulletAt) {
            bullets.add(new Bullet(paddleCenterX(), (int)paddle.y, -12));
            nextBulletAt = now + 200;
        }

        // bullets
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.y += b.vy;
            if (b.y < -20) { bullets.remove(i--); continue; }
            Rectangle r = b.getRect();
            for (int j = 0; j < levels.bricks.length; j++) {
                Brick bk = levels.bricks[j];
                if (bk == null) continue;
                if (r.intersects(bk.getRect())) {
                    levels.bricks[j] = null;
                    score += 10;
                    bullets.remove(i--);
                    break;
                }
            }
        }

        // lasers lifetime
        for (int i = 0; i < lasers.size(); i++) {
            if (System.currentTimeMillis() >= lasers.get(i).endTime) lasers.remove(i--);
        }

        // cleared level?
        if (levels.cleared()) {
            if (levelIndex + 1 < TOTAL_LEVELS) {
                levelIndex++;
                levels.load(levelIndex);
                resetBallPaddle();
                state = State.PAUSE;     // dừng 1 nhịp cho người chơi
                music.playLevel(levelIndex);
                music.pause();
            } else {
                state = State.WIN;
                music.stop();
            }
        }
    }

    private void respawnBallOnPaddle() {
        double cx = paddle.x + paddle.w / 2.0;
        double cy = paddle.y - 16; // ngay trên paddle
        balls.clear();
        balls.add(new Ball(cx, cy, 8, 0, -4)); // thẳng lên
    }

    // ============== PowerUps API ==============
    private int paddleCenterX() { return (int)(paddle.x + paddle.w / 2.0); }

    public void activateExpandPaddle(long ms) {
        if (paddleOrigW == null) paddleOrigW = paddle.w;
        paddle.w = (int)Math.round(paddle.w * 1.6);
        if (paddle.x + paddle.w > WIDTH - 10) paddle.x = WIDTH - 10 - paddle.w;
        expandUntil = System.currentTimeMillis() + ms;
    }

    public void spawnBonusBalls(int count) {
        Ball ref = balls.get(0);
        for (int i = 0; i < count; i++) {
            double vx = (i % 2 == 0) ? -3.0 : 3.0;
            balls.add(new Ball(ref.x, ref.y, ref.r, vx, -4 - 0.5 * i));
        }
    }

    public void fireLaser() {
        int x = paddleCenterX();
        for (int i = 0; i < levels.bricks.length; i++) {
            Brick b = levels.bricks[i];
            if (b == null) continue;
            if (x + 4 >= b.x && x - 4 <= b.x + b.w) { levels.bricks[i] = null; score += 10; }
        }
        lasers.add(new LaserRay(x, System.currentTimeMillis() + 120)); // chớp 120ms
    }

    public void enableGun(long ms) {
        long now = System.currentTimeMillis();
        gunUntil = Math.max(gunUntil, now) + ms;
        if (nextBulletAt < now) nextBulletAt = now;
    }

    private void applyPickup(Pickup.Type t) {
        switch (t) {
            case EXPAND -> ExpandPaddle.activate(this);
            case BONUS_BALLS -> BonusBalls.activate(this);
            case LAZER -> Lazer.activate(this);
            case GUN -> Gun.activate(this);
        }
    }

    // ================= render =================
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // nền
        if (state == State.MENU && mainBg != null) {
            g2.drawImage(mainBg, 0, 0, null);
        } else if (levels.background() != null) {
            g2.drawImage(levels.background(), 0, 0, null);
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(10, 10, 20), 0, getHeight(), Color.BLACK));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // gameplay
        if (state != State.MENU) {
            for (Brick b : levels.bricks) if (b != null) b.draw(g2);
            paddle.draw(g2);
            for (Ball b : balls) b.draw(g2);

            for (Pickup p : pickups) p.draw(g2);

            g2.setColor(Color.YELLOW);
            for (Bullet b : bullets) g2.fillRect(b.x - 2, b.y - 8, 4, 8);

            g2.setColor(new Color(0, 255, 255, 180));
            for (LaserRay l : lasers) {
                g2.fillRect(l.x - 2, 0, 4, (int)paddle.y);
                g2.fillOval(l.x - 4, (int)paddle.y - 6, 8, 8);
            }

            // HUD
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.drawString("Score: " + score, 12, 20);
            g2.drawString("Lives: " + Math.max(0, lives), 120, 20);
            g2.drawString("Level: " + (levelIndex + 1) + "/5", 200, 20);

            // buttons (có Fallback nếu thiếu icon)
            int px = getWidth() - btnW - btnPad, py = 8;

            if (pauseIcon != null) g2.drawImage(pauseIcon, px, py, null);
            else { g2.setColor(Color.LIGHT_GRAY); g2.fillRect(px+4,py+3,6,20); g2.fillRect(px+16,py+3,6,20); }
            pauseBtn.setBounds(px, py, btnW, btnH);

            int hx = px - btnW - 8;
            if (homeIcon != null) g2.drawImage(homeIcon, hx, py, null);
            else { g2.setColor(Color.LIGHT_GRAY); int[] xs={hx+3,hx+13,hx+23,hx+23,hx+3}; int[] ys={py+14,py+4,py+14,py+24,py+24}; g2.fillPolygon(xs,ys,5); }
            homeBtn.setBounds(hx, py, btnW, btnH);

            int sx = hx - btnW - 8;
            if (gearIcon != null) g2.drawImage(gearIcon, sx, py, null);
            else { g2.setColor(Color.LIGHT_GRAY); g2.drawOval(sx+4,py+4,18,18); g2.drawLine(sx+13,py+4,sx+13,py+22); g2.drawLine(sx+4,py+13,sx+22,py+13); }
            settingsBtn.setBounds(sx, py, btnW, btnH);
        }

        drawOverlay(g2);
    }

    private void drawOverlay(Graphics2D g2) {
        if (state != State.PLAY) {
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String title = switch (state) {
            case MENU -> "Press SPACE to Start";
            case PAUSE -> "PAUSED - SPACE to Resume";
            case SETTINGS -> "SETTINGS";
            case GAMEOVER -> "GAME OVER - Press R to Retry";
            case WIN -> (levelIndex + 1 < TOTAL_LEVELS)
                    ? "LEVEL CLEARED - Press N for Next"
                    : "ALL LEVELS CLEARED - Press R to Restart";
            default -> "";
        };
        if (state != State.PLAY) {
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (getWidth() - tw) / 2, getHeight() / 3);
        }

        if (state == State.SETTINGS) {
            int bw = 260, bh = 36, gap = 14, cx = (getWidth() - bw) / 2, cy = getHeight() / 2;
            btnMusicToggle.setBounds(cx, cy, bw, bh);
            drawBtn(g2, btnMusicToggle, music.isEnabled() ? "Music: ON (pause/resume)" : "Music: OFF");
            btnMainMenu.setBounds(cx, cy + bh + gap, bw, bh);
            drawBtn(g2, btnMainMenu, "Return to MAIN MENU");
            btnBack.setBounds(cx, cy + 2 * (bh + gap), bw, bh);
            drawBtn(g2, btnBack, "Back");
        }
    }

    private void drawBtn(Graphics2D g2, Rectangle r, String text) {
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        int tw = g2.getFontMetrics().stringWidth(text), th = g2.getFontMetrics().getAscent();
        g2.drawString(text, r.x + (r.width - tw) / 2, r.y + (r.height + th) / 2 - 3);
    }

    // ================= input =================
    @Override public void keyTyped(KeyEvent e) {}

    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (state == State.MENU) { state = State.PLAY; music.playLevel(levelIndex); }
            else if (state == State.PLAY) { state = State.PAUSE; music.pause(); }
            else if (state == State.PAUSE) { state = State.PLAY; music.resume(); }
        }

        if (e.getKeyCode() == KeyEvent.VK_N && state == State.WIN) {
            if (levelIndex + 1 < TOTAL_LEVELS) {
                levelIndex++; levels.load(levelIndex); resetBallPaddle();
                state = State.PAUSE; music.playLevel(levelIndex); music.pause();
            } else {
                state = State.MENU; resetAll(); music.playMenu();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_R && (state == State.GAMEOVER || state == State.WIN)) {
            resetAll(); state = State.PAUSE; music.playLevel(levelIndex); music.pause();
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
    }

    @Override public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();

        if (settingsBtn.contains(p) && state != State.MENU) {
            state = State.SETTINGS; repaint(); return;
        }
        if (pauseBtn.contains(p) && state != State.MENU) {
            if (state == State.PLAY) { state = State.PAUSE; music.pause(); }
            else if (state == State.PAUSE) { state = State.PLAY; music.resume(); }
            repaint(); return;
        }
        if (homeBtn.contains(p)) {
            state = State.MENU; resetAll(); music.playMenu(); repaint(); return;
        }

        if (state == State.SETTINGS) {
            if (btnMusicToggle.contains(p)) {
                music.setEnabled(!music.isEnabled(), state == State.MENU, levelIndex);
                repaint(); return;
            } else if (btnMainMenu.contains(p)) {
                state = State.MENU; resetAll(); music.playMenu(); repaint(); return;
            } else if (btnBack.contains(p)) {
                state = State.PAUSE; repaint(); return;
            }
        }
    }

    private void resetBallPaddle() {
        paddle.x = WIDTH / 2.0 - paddle.w / 2.0;
        balls.clear(); balls.add(new Ball(WIDTH/2.0, HEIGHT - 60, 8, 4, -4));
        pickups.clear(); bullets.clear(); lasers.clear();
        expandUntil = 0; paddleOrigW = null; gunUntil = 0; nextBulletAt = 0;
    }

    private void resetAll() {
        score = 0; lives = 3; levelIndex = 0;
        levels.load(levelIndex);
        resetBallPaddle();
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ================= helper inner types =================
    private static class Pickup {
        enum Type { EXPAND, BONUS_BALLS, LAZER, GUN }
        double x, y; int size = 18; double vy = 2.0; Type type;
        static Pickup randomAt(double x, double y) {
            Pickup p = new Pickup(); p.x = x; p.y = y;
            p.type = Type.values()[(int)(Math.random() * 4)];
            return p;
        }
        Rectangle getRect() { return new Rectangle((int)(x - size/2), (int)(y - size/2), size, size); }
        void draw(Graphics2D g2) {
            Color c = switch (type) {
                case EXPAND -> new Color(50, 200, 255);
                case BONUS_BALLS -> new Color(255, 200, 50);
                case LAZER -> new Color(120, 255, 120);
                case GUN -> new Color(255, 100, 140);
            };
            g2.setColor(c);
            g2.fillOval((int)(x - size/2), (int)(y - size/2), size, size);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            String s = switch (type) { case EXPAND -> "E"; case BONUS_BALLS -> "B"; case LAZER -> "L"; case GUN -> "G"; };
            g2.drawString(s, (int)x - 4, (int)y + 4);
        }
    }

    private static class Bullet {
        int x, y, vy;
        Bullet(int x, int y, int vy) { this.x = x; this.y = y; this.vy = vy; }
        Rectangle getRect() { return new Rectangle(x - 2, y - 8, 4, 8); }
    }

    private static class LaserRay {
        int x; long endTime;
        LaserRay(int x, long endTime) { this.x = x; this.endTime = endTime; }
    }
}
