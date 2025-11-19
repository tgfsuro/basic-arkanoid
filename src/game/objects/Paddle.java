/** di chuyển, vẽ bằng skin ảnh*/
package game.objects;

import game.AssetLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Paddle có thể hiển thị bằng ảnh skin hoặc fallback vẽ hình chữ nhật. */
public class Paddle {
    public double x, y; // top-left
    public int w, h;
    public double speed;

    // Ảnh skin (có thể null) – ưu tiên dùng Image chung để nhận từ store
    private Image skinImg;

    public Paddle(double x, double y, int w, int h, double speed) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.speed = speed;
    }

    /** Gán skin từ đường dẫn (classpath hoặc file). Nếu lỗi → để null. */
    public void setSkinPath(String path) {
        try {
            BufferedImage img = AssetLoader.image(path);
            this.skinImg = img;
        } catch (Throwable ignore) {
            this.skinImg = null;
        }
    }

    /** Gán skin trực tiếp bằng Image (lấy từ PaddleSkinStore). */
    public void setSkinImage(Image img) {
        this.skinImg = img;
    }

    public void move(int dir, int screenW) {
        x += dir * speed;
        if (x < 10) x = 10;
        if (x + w > screenW - 10) x = screenW - 10 - w;
    }

    public Rectangle getRect() { return new Rectangle((int)x, (int)y, w, h); }

    public void draw(Graphics2D g2) {
        if (skinImg != null) {
            // Vẽ ảnh theo kích thước w×h hiện tại
            g2.drawImage(skinImg, (int)x, (int)y, w, h, null);
            return;
        }
        // Fallback: thanh trắng bo tròn
        g2.setColor(new Color(230,230,230));
        g2.fillRoundRect((int)x, (int)y, w, h, 10, 10);
    }
}
