package game.objects;

import java.awt.*;

/** Gạch cứng: cần 2 hit để vỡ. Màu xanh lá. */
public class HardBrick extends Brick {

    public HardBrick(int x, int y, int w, int h) {
        super(x, y, w, h);
        this.hp = 2;
    }

    @Override
    protected Color baseColor() { return new Color(80, 200, 120); } // xanh lá
}
