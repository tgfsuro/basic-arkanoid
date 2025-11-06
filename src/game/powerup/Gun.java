package game.powerup;

import game.GamePanel;

/** Súng bắn đạn trong 5s. */
public final class Gun {
    private Gun(){}
    public static void activate(GamePanel g){ g.enableGun(5_000); }
}
