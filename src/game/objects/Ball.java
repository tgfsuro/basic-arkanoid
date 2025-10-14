package game.objects;

import java.awt.*;

public class Ball {
    public double x, y;   // center
    public double vx, vy; // velocity
    public int r;

    public Ball(double x, double y, int r, double vx, double vy) {
        this.x = x; this.y = y; this.r = r; this.vx = vx; this.vy = vy;
    }

    public void update() { x += vx; y += vy; }

    public Rectangle getRect() {
        return new Rectangle((int)(x - r), (int)(y - r), r*2, r*2);
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillOval((int)(x - r), (int)(y - r), r*2, r*2);
    }
}