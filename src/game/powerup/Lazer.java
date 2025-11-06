package game.powerup;

import game.GamePanel;

/** Bắn 1 tia laser xuyên thấu, chớp tắt nhanh. */
public final class Lazer {
    private Lazer(){}
    public static void activate(GamePanel g){ g.fireLaser(); }
}
