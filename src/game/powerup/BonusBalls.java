package game.powerup;

import game.GamePanel;

/** Thêm 2 quả bóng. */
public final class BonusBalls {
    private BonusBalls(){}
    public static void activate(GamePanel g){ g.spawnBonusBalls(2); }
}
