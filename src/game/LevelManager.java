package game;

import game.objects.Brick;
import game.objects.HardBrick;
import game.objects.UnbreakableBrick;
import game.play.Pickup;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class LevelManager {
    private final int WIDTH, HEIGHT;
    public Brick[] bricks;
    private Image bg;
    private final Random rng = new Random();

    private final int rowsDefaultTop = 60;
    private final int gap = 4;

    public LevelManager(int w, int h) { this.WIDTH = w; this.HEIGHT = h; }

    public void load(int levelIndex) {
        // đọc layout (giống trước đây của bạn)
        List<String> lines = AssetLoader.tryReadLinesOrDefault("levels/level" + (levelIndex + 1) + ".txt",
                new String[]{
                        "1010101010","0101010101","1111111111","0101010101","1010101010"
                });

        int rows = lines.size();
        int cols = lines.get(0).length();

        int avail = WIDTH - 2 * 16;
        int bw = (avail - (cols - 1) * gap) / cols;
        int bh = 20;
        int startX = (WIDTH - (cols * bw + (cols - 1) * gap)) / 2;

        bricks = new Brick[rows * cols];
        int k = 0;

        for (int r = 0; r < rows; r++) {
            int y = rowsDefaultTop + r * (bh + gap);
            String row = lines.get(r);
            for (int c = 0; c < cols; c++) {
                int x = startX + c * (bw + gap);
                char ch = row.charAt(c);

                Brick b = null;
                // Ký hiệu:
                // '1' = thường ; '2' = hard ; '#' = unbreakable ; '0' = trống
                if (ch == '1') b = new Brick(x, y, bw, bh);
                else if (ch == '2') b = new HardBrick(x, y, bw, bh);
                else if (ch == '#') b = new UnbreakableBrick(x, y, bw, bh);
                else if (ch == '0') b = null;

                // Gắn powerup ngẫu nhiên cho gạch có thể phá
                if (b != null && !b.isUnbreakable() && rng.nextDouble() < 0.22) {
                    b.powerup = Pickup.Type.values()[rng.nextInt(Pickup.Type.values().length)];
                }

                bricks[k++] = b;
            }
        }

        // nền level như trước
        bg = AssetLoader.tryBackgroundForLevel(levelIndex + 1, WIDTH, HEIGHT);
    }

    public boolean cleared() {
        if (bricks == null) return true;
        for (Brick b : bricks) if (b != null && !b.isUnbreakable()) return false;
        return true;
    }

    public Image background() { return bg; }
}
