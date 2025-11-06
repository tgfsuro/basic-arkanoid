package game;

import game.objects.Brick;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Load level (bricks + background), cung cấp helper về kích thước gạch. */
public final class LevelManager {
    public final int WIDTH, HEIGHT;
    public final int brickGap = 4, brickTop = 60;

    public int rows, cols;
    public Brick[] bricks;
    private Image background; // đã scale = WIDTHxHEIGHT

    // path ảnh nền (ưu tiên theo thứ tự)
    private static final String[] BG_PATTERNS = {
            "backgrounds/level%d.png", "backgrounds/level%d.jpg", "backgrounds/level%d.jpeg",
            "backgrounds/Map%d.png",   "backgrounds/Map%d.jpg",   "backgrounds/Map%d.jpeg"
    };

    // fallback nếu chưa có file level
    private static final String[][] DEFAULT_LEVELS = {
            {"1010101010","0101010101","1111111111","0101010101","1010101010"},
            {"1111111111","1111111111","1111111111","0000000000","0000000000"},
            {"1110011110","1100000011","1001111001","1100000011","0111111110"},
            {"1111111111","1000000001","1011111101","1010000101","1111111111"},
            {"0001111000","0011111100","0111111110","0011111100","0001111000"}
    };

    public LevelManager(int w, int h) { this.WIDTH = w; this.HEIGHT = h; }

    public void load(int levelIndex) {
        List<String> lines = readLevelFile(levelIndex);
        rows = lines.size();
        cols = lines.get(0).length();

        int avail = WIDTH - 2 * 16;
        int bw = (avail - (cols - 1) * brickGap) / cols;
        int bh = 20;
        int startX = (WIDTH - (cols * bw + (cols - 1) * brickGap)) / 2;

        bricks = new Brick[rows * cols];
        int k = 0;
        for (int r = 0; r < rows; r++) {
            String row = lines.get(r);
            int y = brickTop + r * (bh + brickGap);
            for (int c = 0; c < cols; c++) {
                int x = startX + c * (bw + brickGap);
                bricks[k++] = (row.charAt(c) == '1') ? new Brick(x, y, bw, bh) : null;
            }
        }

        background = loadBackground(levelIndex + 1);
    }

    public Image background() { return background; }

    public boolean cleared() {
        for (Brick b : bricks) if (b != null) return false;
        return true;
    }

    // ---------- helpers ----------
    private List<String> readLevelFile(int index0) {
        String res = "levels/level" + (index0 + 1) + ".txt";
        try {
            return AssetLoader.readLines(res);
        } catch (Exception ignore) {
            return new ArrayList<>(Arrays.asList(DEFAULT_LEVELS[index0 % DEFAULT_LEVELS.length]));
        }
    }

    private Image loadBackground(int n) {
        // 1) classpath
        for (String pat : BG_PATTERNS) {
            String p = String.format(pat, n);
            try { return AssetLoader.scaled(p, WIDTH, HEIGHT); } catch (Exception ignored) {}
        }
        // 2) filesystem (src/resources,...)
        String[] roots = { "src/resources/", "resources/", "src/main/resources/" };
        for (String root : roots) {
            for (String pat : BG_PATTERNS) {
                String p = root + String.format(pat, n);
                try {
                    File f = new File(p);
                    if (f.exists())
                        return ImageIO.read(f).getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
