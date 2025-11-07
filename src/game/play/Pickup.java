package game.play;

import java.awt.*;

public class Pickup {
    public enum Type { EXPAND, BONUS_BALLS, LAZER, GUN }

    public double x, y;
    public int size = 18;
    public double vy = 2.0;
    public Type type;

    public static Pickup randomAt(double x, double y) {
        Pickup p = new Pickup(); p.x = x; p.y = y;
        p.type = Type.values()[(int)(Math.random() * 4)];
        return p;
    }

    public static Pickup at(double x, double y, Type t) {
        Pickup p = new Pickup(); p.x = x; p.y = y; p.type = t; return p;
    }

    public Rectangle getRect() {
        return new Rectangle((int)(x - size/2), (int)(y - size/2), size, size);
    }

    public void draw(Graphics2D g2) {
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
        String s = switch (type) {
            case EXPAND -> "E";
            case BONUS_BALLS -> "B";
            case LAZER -> "L";
            case GUN -> "G";
        };
        g2.drawString(s, (int)x - 4, (int)y + 4);
    }
}
