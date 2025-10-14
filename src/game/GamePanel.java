package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import game.objects.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH, HEIGHT;
    private final Timer timer;

    private Paddle paddle;
    private Ball ball;
    private Brick[] bricks;

    private boolean left, right;
    private final int rows = 5, cols = 10, brickGap = 4, brickTop = 60;

    public GamePanel(int w, int h) {
        this.WIDTH = w;
        this.HEIGHT = h;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        paddle = new Paddle(WIDTH / 2.0 - 50, HEIGHT - 40, 100, 12, 6);
        ball   = new Ball(WIDTH / 2.0, HEIGHT - 60, 8, 4, -4);
        makeBricks();

        timer = new Timer(1000 / 60, this); // ~60 FPS
        timer.start();
    }

    private void makeBricks() {
        int avail = WIDTH - 2 * 16;
        int bw = (avail - (cols - 1) * brickGap) / cols;
        int bh = 20;
        int startX = (WIDTH - (cols * bw + (cols - 1) * brickGap)) / 2;

        bricks = new Brick[rows * cols];
        int i = 0;
        for (int r = 0; r < rows; r++) {
            int y = brickTop + r * (bh + brickGap);
            for (int c = 0; c < cols; c++) {
                int x = startX + c * (bw + brickGap);
                bricks[i++] = new Brick(x, y, bw, bh);
            }
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        // paddle
        if (left)  paddle.move(-1, WIDTH);
        if (right) paddle.move( 1, WIDTH);

        // ball move
        ball.update();

        // walls
        if (ball.x - ball.r < 0)        { ball.x = ball.r;            ball.vx = -ball.vx; }
        if (ball.x + ball.r > WIDTH)    { ball.x = WIDTH - ball.r;    ball.vx = -ball.vx; }
        if (ball.y - ball.r < 0)        { ball.y = ball.r;            ball.vy = -ball.vy; }

        // bottom -> reset
        if (ball.y - ball.r > HEIGHT) {
            ball.x = WIDTH / 2.0; ball.y = HEIGHT - 60;
            ball.vx = 4; ball.vy = -4;
        }

        // paddle collision (simple)
        if (ball.getRect().intersects(paddle.getRect()) && ball.vy > 0) {
            ball.y = paddle.y - ball.r - 1;
            ball.vy = -ball.vy;
            if (left && !right)  ball.vx -= 1;
            if (right && !left)  ball.vx += 1;
        }

        // brick collision (very basic)
        for (int i = 0; i < bricks.length; i++) {
            Brick b = bricks[i];
            if (b == null) continue;
            if (ball.getRect().intersects(b.getRect())) {
                bricks[i] = null;
                ball.vy = -ball.vy;
                break;
            }
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.drawString("←/→ di chuyển. Bản basic, chưa có điểm/mạng.", 10, 20);

        for (Brick b : bricks) if (b != null) b.draw(g2);
        paddle.draw(g2);
        ball.draw(g2);
    }

    // input
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
    }
}