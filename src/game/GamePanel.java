package game;

import game.logic.CollisionUtil;
import game.logic.ServingController;

import game.objects.Ball;
import game.objects.Brick;
import game.objects.Paddle;

import game.play.Bullet;
import game.play.LazerRay;
import game.play.Pickup;

import game.powerup.BonusBalls;
import game.powerup.ExpandPaddle;
import game.powerup.Gun;
import game.powerup.Lazer;

import game.ui.HudOverlay;
import game.ui.SettingsOverlay;

import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {

    // ===== màn hình & loop =====
    private final int WIDTH, HEIGHT;
    private final Timer timer = new Timer(1000 / 60, this);

    // ===== state =====
    private enum State { MENU, PLAY, PAUSE, SETTINGS, GAMEOVER, WIN }
    private State state = State.MENU;

    // ===== managers =====
    private final LevelManager levels;
    private final MusicHub music = new MusicHub();
    private final ServingController serveCtl = new ServingController();

    // ===== game objects =====
    private final List<Ball> balls = new ArrayList<>();
    private Paddle paddle;

    // ===== UI / HUD =====
    private final HudOverlay hud = new HudOverlay();
    private final SettingsOverlay settingsUI = new SettingsOverlay();

    private Image pauseIcon, homeIcon, gearIcon, mainBg;

    // ===== HUD data =====
    private int score = 0, lives = 3, levelIndex = 0; // 0-based
    private final int TOTAL_LEVELS = 5;

    // ===== rng & pickups =====
    private final Random rng = new Random();
    private final List<Pickup> pickups = new ArrayList<>();

    // ===== bullets & laser =====
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<LazerRay> lasers = new ArrayList<>();
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
        Ball first = new Ball(WIDTH / 2.0, HEIGHT - 60, 8, 0, 0);
        balls.add(first);

        try { pauseIcon  = AssetLoader.scaled("images/pause.png",  26, 26); } catch (Exception ignored) {}
        try { homeIcon   = AssetLoader.scaled("images/home.png",   26, 26); } catch (Exception ignored) {}
        try { gearIcon   = AssetLoader.scaled("images/gear.png",   26, 26); } catch (Exception ignored) {}
        try { mainBg     = AssetLoader.scaled("backgrounds/mainbackground.jpg", w, h); } catch (Exception ignored) {}

        levels.load(levelIndex);
        music.playMenu();

        // bắt đầu ở chế độ serving
        serveCtl.enterServing(paddle, first);

        timer.start();
    }

    // ============ API từ MainHall/GameWindow ============
    public void prepareLevel1FromHall() {
        music.stop();
        levelIndex = 0;
        levels.load(levelIndex);
        score = 0; lives = 3;

        balls.clear();
        Ball b = new Ball(WIDTH/2.0, HEIGHT - 60, 8, 0, 0);
        balls.add(b);
        serveCtl.enterServing(paddle, b);

        state = State.PLAY;
        music.playLevel(levelIndex);
    }

    // ================= loop =================
    @Override public void actionPerformed(ActionEvent e) {
        if (state == State.PLAY) update();
        repaint();
    }

    private void update() {
        if (left)  paddle.move(-1, WIDTH);
        if (right) paddle.move( 1, WIDTH);

        // serving?
        if (serveCtl.isServing()) {
            serveCtl.updateWhileServing(paddle, balls.get(0));
            updatePickupsAndProjectiles();
            return;
        }

        // bóng đang bay
        for (int i = 0; i < balls.size(); i++) {
            Ball b = balls.get(i);
            b.update();

            CollisionUtil.reflectWithWalls(b, WIDTH, HEIGHT);

            // lọt đáy
            if (b.y - b.r > HEIGHT) { balls.remove(i--); continue; }

            // paddle
            CollisionUtil.ballBounceOnPaddle(b, paddle);

            // bricks
            int idx = CollisionUtil.hitBrickIndex(b, levels.bricks);
            if (idx >= 0) {
                Brick removed = levels.bricks[idx];
                levels.bricks[idx] = null;
                score += 10;
                if (removed != null && rng.nextDouble() < 0.25)
                    pickups.add(Pickup.randomAt(removed.x + removed.w/2.0, removed.y + removed.h/2.0));
            }
        }

        // hết bóng
        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) { state = State.GAMEOVER; music.stop(); return; }
            // quay về serving-mode
            Ball b = new Ball(paddle.x + paddle.w/2.0, paddle.y - 9, 8, 0, 0);
            balls.add(b);
            serveCtl.enterServing(paddle, b);
        }

        updatePickupsAndProjectiles();

        // cleared level?
        if (levels.cleared()) {
            if (levelIndex + 1 < TOTAL_LEVELS) {
                levelIndex++;
                levels.load(levelIndex);
                resetBallPaddle();
                serveCtl.enterServing(paddle, balls.get(0));
                music.playLevel(levelIndex);
            } else {
                state = State.WIN;
                music.stop();
            }
        }
    }

    private void updatePickupsAndProjectiles() {
        long now = System.currentTimeMillis();

        // pickups
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
        if (expandUntil > 0 && now >= expandUntil) {
            if (paddleOrigW != null) paddle.w = paddleOrigW;
            expandUntil = 0; paddleOrigW = null;
        }

        // gun auto
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

        // lasers
        for (int i = 0; i < lasers.size(); i++) {
            if (System.currentTimeMillis() >= lasers.get(i).endTime) lasers.remove(i--);
        }
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
        if (serveCtl.isServing()) return;
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
        lasers.add(new LazerRay(x, System.currentTimeMillis() + 120));
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
            for (LazerRay l : lasers) {
                g2.fillRect(l.x - 2, 0, 4, (int)paddle.y);
                g2.fillOval(l.x - 4, (int)paddle.y - 6, 8, 8);
            }

            // HUD & buttons
            hud.drawTopHUD(g2, score, Math.max(0, lives), (levelIndex + 1) + "/5");
            hud.drawTopButtons(g2, getWidth(), getHeight(), pauseIcon, homeIcon, gearIcon);
        }

        // Settings/GameOver/Win overlays
        if (state == State.SETTINGS) {
            settingsUI.draw(g2, music.isEnabled(), getWidth(), getHeight());
        } else if (state == State.GAMEOVER || state == State.WIN) {
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
            String title = (state == State.GAMEOVER)
                    ? "GAME OVER - Press R to Retry"
                    : ((levelIndex + 1 < 5) ? "LEVEL CLEARED - Press N for Next"
                    : "ALL LEVELS CLEARED - Press R to Restart");
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (getWidth() - tw) / 2, getHeight() / 3);
        }
    }

    // ================= input =================
    @Override public void keyTyped(KeyEvent e) {}

    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        // ← hoặc A | → hoặc D
        if (k == KeyEvent.VK_LEFT  || k == KeyEvent.VK_A) left  = true;
        if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) right = true;

        // SPACE: nếu đang serving -> bắn
        if (k == KeyEvent.VK_SPACE) {
            if (state == State.MENU) {
                prepareLevel1FromHall(); return;
            } else if (serveCtl.isServing()) {
                if (serveCtl.launch(paddle, balls.get(0))) {
                    state = State.PLAY; music.resume();
                }
                return;
            }
        }

        // P: toggle pause (chỉ khi đã bắn)
        if (k == KeyEvent.VK_P && state != State.MENU && state != State.SETTINGS) {
            if (state == State.PLAY && !serveCtl.isServing()) { state = State.PAUSE; music.pause(); }
            else if (state == State.PAUSE)                    { state = State.PLAY;  music.resume(); }
        }

        if (k == KeyEvent.VK_N && state == State.WIN) {
            if (levelIndex + 1 < TOTAL_LEVELS) {
                levelIndex++; levels.load(levelIndex); resetBallPaddle();
                serveCtl.enterServing(paddle, balls.get(0));
                music.playLevel(levelIndex);
            } else {
                state = State.MENU; resetAll(); music.playMenu();
            }
        }

        if (k == KeyEvent.VK_R && (state == State.GAMEOVER || state == State.WIN)) {
            resetAll(); serveCtl.enterServing(paddle, balls.get(0)); music.playLevel(levelIndex);
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_LEFT  || k == KeyEvent.VK_A) left  = false;
        if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) right = false;
    }

    @Override public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();

        // buttons
        if (state != State.MENU) {
            if (hud.settingsBtn().contains(p)) { state = State.SETTINGS; repaint(); return; }
            if (hud.pauseBtn().contains(p)) {
                if (state == State.PLAY && !serveCtl.isServing()) { state = State.PAUSE; music.pause(); }
                else if (state == State.PAUSE) { state = State.PLAY; music.resume(); }
                repaint(); return;
            }
            if (hud.homeBtn().contains(p)) { goToMainMenu(); return; }
        }

        // settings overlay
        if (state == State.SETTINGS) {
            if (settingsUI.btnMusicToggle.contains(p)) {
                music.setEnabled(!music.isEnabled(), state == State.MENU, levelIndex);
                repaint(); return;
            } else if (settingsUI.btnMainMenu.contains(p)) {
                goToMainMenu(); return;
            } else if (settingsUI.btnBack.contains(p)) {
                state = State.PAUSE; repaint(); return;
            }
        }
    }

    /** Thoát ra sảnh. */
    private void goToMainMenu() {
        try { music.stop(); } catch (Exception ignored) {}
        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (win != null) win.dispose();
        javax.swing.SwingUtilities.invokeLater(() -> new game.mainhall.MainHall());
    }

    private void resetBallPaddle() {
        paddle.x = WIDTH / 2.0 - paddle.w / 2.0;
        balls.clear();
        balls.add(new Ball(WIDTH/2.0, HEIGHT - 60, 8, 0, 0));
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
}
