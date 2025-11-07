package game.objects;

import java.awt.*;

/** Gạch bất tử: không thể phá. Màu xám + bóng. */
public class UnbreakableBrick extends Brick {

    public UnbreakableBrick(int x, int y, int w, int h) {
        super(x, y, w, h);
        this.unbreakable = true;
        this.hp = Integer.MAX_VALUE;
    }

    @Override
    protected Color baseColor() { return new Color(160, 160, 170); } // xám
}
