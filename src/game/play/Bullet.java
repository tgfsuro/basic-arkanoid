package game.play;

import java.awt.*;

public class Bullet {
    public int x, y, vy;
    public Bullet(int x, int y, int vy) { this.x = x; this.y = y; this.vy = vy; }
    public Rectangle getRect() { return new Rectangle(x - 2, y - 8, 4, 8); }
}
