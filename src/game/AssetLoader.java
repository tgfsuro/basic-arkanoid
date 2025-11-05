package game;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Load ảnh/âm thanh/text từ classpath (resources) + FALLBACK đọc file trực tiếp. */
public final class AssetLoader {
    private static final Map<String, BufferedImage> IMG_CACHE = new HashMap<>();
    private static final Map<String, Clip> SND_CACHE = new HashMap<>();

    private AssetLoader() {}

    // ----- mở InputStream: ưu tiên classpath, sau đó dò các thư mục phổ biến -----
    private static InputStream open(String resPath) throws IOException {
        // 1) classpath
        InputStream in = AssetLoader.class.getClassLoader().getResourceAsStream(resPath);
        if (in != null) return in;

        // 2) fallback đường file (chạy thẳng từ IDE)
        String[] roots = {
                "src/resources/",           // dự án IntelliJ đơn giản
                "resources/",               // nếu content root là resources
                "src/main/resources/",      // cấu trúc Maven/Gradle
                ""                          // thử nguyên resPath luôn
        };
        for (String root : roots) {
            File f = new File(root + resPath);
            if (f.exists() && f.isFile()) {
                return new FileInputStream(f);
            }
        }
        throw new FileNotFoundException("Resource not found anywhere: " + resPath);
    }

    // ---------- Images ----------
    public static BufferedImage image(String resPath) {
        BufferedImage cached = IMG_CACHE.get(resPath);
        if (cached != null) return cached;
        try (InputStream in = open(resPath)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) throw new IOException("ImageIO.read returned null for: " + resPath);
            IMG_CACHE.put(resPath, img);
            return img;
        } catch (IOException e) {
            throw new UncheckedIOException("load image fail: " + resPath, e);
        }
    }

    public static Image scaled(String resPath, int w, int h) {
        return image(resPath).getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    // ---------- Sounds (WAV) ----------
    public static Clip sound(String resPath) {
        Clip cached = SND_CACHE.get(resPath);
        if (cached != null) return cached;
        try (InputStream raw = open(resPath);
             AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            SND_CACHE.put(resPath, clip);
            return clip;
        } catch (Exception e) {
            throw new RuntimeException("load sound fail: " + resPath, e);
        }
    }

    public static void playOnce(String resPath) {
        try {
            Clip c = sound(resPath);
            if (c.isRunning()) c.stop();
            c.setFramePosition(0);
            c.start();
        } catch (RuntimeException ignore) {}
    }

    public static Clip loop(String resPath) {
        try {
            Clip c = sound(resPath);
            if (c.isRunning()) c.stop();
            c.setFramePosition(0);
            c.loop(Clip.LOOP_CONTINUOUSLY);
            return c;
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ---------- Text (levels) ----------
    public static java.util.List<String> readLines(String resPath) {
        try (InputStream in = open(resPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            java.util.List<String> lines = new ArrayList<>();
            for (String s; (s = br.readLine()) != null; ) {
                s = s.strip();
                if (!s.isEmpty()) lines.add(s);
            }
            return lines;
        } catch (IOException e) {
            throw new UncheckedIOException("read text fail: " + resPath, e);
        }
    }
}
