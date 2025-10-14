package game.objects;

import java.awt.*;

public class Brick {
    public int x, y, w, h;

    public Brick(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    public Rectangle getRect() { return new Rectangle(x, y, w, h); }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(120, 200, 255));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(0,0,0,60));
        g2.drawRect(x, y, w, h);
    }
}