package game.objects;

import java.awt.*;

public class Paddle {
    public double x, y; // top-left
    public int w, h;
    public double speed;

    public Paddle(double x, double y, int w, int h, double speed) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.speed = speed;
    }

    public void move(int dir, int screenW) {
        x += dir * speed;
        if (x < 10) x = 10;
        if (x + w > screenW - 10) x = screenW - 10 - w;
    }

    public Rectangle getRect() { return new Rectangle((int)x, (int)y, w, h); }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(230,230,230));
        g2.fillRoundRect((int)x, (int)y, w, h, 10, 10);
    }
}