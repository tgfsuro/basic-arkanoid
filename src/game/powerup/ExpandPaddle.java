package game.powerup;

import game.GamePanel;

/** Mở rộng paddle 10s. */
public final class ExpandPaddle {
    private ExpandPaddle(){}
    public static void activate(GamePanel g){ g.activateExpandPaddle(10_000); }
}
