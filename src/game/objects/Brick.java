package game.objects;

import game.play.Pickup;
import java.awt.*;

/** Brick cơ bản: có thể gán máu (hp), chế độ bất tử và powerup sẵn có. */
public class Brick {
    public int x, y, w, h;

    /** hp <= 0 => vỡ ; unbreakable = true thì không bao giờ vỡ. */
    protected int hp = 1;
    protected boolean unbreakable = false;

    /** Powerup gắn sẵn trong gạch (nếu có) — để vẽ logo & rơi ra khi vỡ. */
    public Pickup.Type powerup = null;

    public Brick(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    /** Trả về true nếu sau cú va chạm, gạch đã vỡ. */
    public boolean hit() {
        if (unbreakable) return false;
        hp--;
        return hp <= 0;
    }

    public boolean isUnbreakable() { return unbreakable; }
    public int getHp() { return hp; }

    public Rectangle getRect() { return new Rectangle(x, y, w, h); }

    /** Màu gạch cơ bản (xanh dương nhạt). Override ở subclass nếu cần. */
    protected Color baseColor() { return new Color(120, 200, 255); }

    /** Vẽ gạch + logo powerup (nếu có). */
    public void draw(Graphics2D g2) {
        // thân
        g2.setColor(baseColor());
        g2.fillRoundRect(x, y, w, h, 6, 6);

        // viền nhẹ
        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawRoundRect(x, y, w, h, 6, 6);

        // hiệu ứng bóng cho unbreakable
        if (unbreakable) {
            Paint old = g2.getPaint();
            g2.setPaint(new GradientPaint(x, y, new Color(255,255,255,120),
                    x, y + h/2f, new Color(255,255,255,0)));
            g2.fillRoundRect(x+2, y+2, w-4, h/2, 6, 6);
            g2.setPaint(old);
        }

        // logo powerup (E/B/L/G)
        if (powerup != null) {
            String s = switch (powerup) {
                case EXPAND -> "E";
                case BONUS_BALLS -> "B";
                case LAZER -> "L";
                case GUN -> "G";
            };
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(Color.BLACK);
            int tw = g2.getFontMetrics().stringWidth(s);
            int th = g2.getFontMetrics().getAscent();
            g2.drawString(s, x + (w - tw)/2, y + (h + th)/2 - 2);
        }
    }
}
