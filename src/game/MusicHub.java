package game;

import game.AssetLoader.Music;

/** Quản lý nhạc: menu và từng level; tiện toggle ON/OFF & pause/resume. */
public final class MusicHub {
    private Music current;
    private boolean enabled = true;

    // nhạc sảnh
    private static final String MENU_ABS =
            "C:\\Users\\Admin\\Downloads\\basic-arkanoid\\src\\resources\\sounds\\music.mp3"; // sảnh chính
    private static final String MENU_CP = "sounds/music.mp3";

    // thư mục nhạc theo level
    private static final String LEVEL_DIR_ABS =
            "C:\\Users\\Admin\\Downloads\\basic-arkanoid\\src\\resources\\sounds\\soundsoflevel";

    public void playMenu() {
        stop();
        if (!enabled) return;
        current = AssetLoader.loopMusicFromFile(MENU_ABS);
        if (current == null) current = AssetLoader.loopMusicFromResource(MENU_CP);
    }

    public void playLevel(int levelIndex0) {
        stop();
        if (!enabled) return;
        int n = levelIndex0 + 1;
        String abs = LEVEL_DIR_ABS + "\\music" + n + ".mp3";
        current = AssetLoader.loopMusicFromFile(abs);
        if (current == null) {
            String cp = "sounds/soundsoflevel/music" + n + ".mp3";
            current = AssetLoader.loopMusicFromResource(cp);
        }
        if (current == null) { // PCM fallback
            String[] pcm = {
                    "sounds/soundsoflevel/music" + n + ".wav",
                    "sounds/soundsoflevel/music" + n + ".aiff",
                    "sounds/soundsoflevel/music" + n + ".au"
            };
            for (String p : pcm) {
                current = AssetLoader.loopMusicFromResource(p);
                if (current != null) break;
            }
        }
    }

    public void pause()  { if (current != null) current.pause(); }
    public void resume() { if (current != null && enabled) current.resume(); }
    public void stop()   { if (current != null) { current.stop(); current = null; } }

    public void setEnabled(boolean on, boolean isMenu, int levelIndex0) {
        enabled = on;
        if (!on) { pause(); return; }
        // vừa bật ON lại → resume/khởi động lại bài đúng ngữ cảnh
        if (current != null) { current.resume(); return; }
        if (isMenu) playMenu(); else playLevel(levelIndex0);
    }
    public boolean isEnabled() { return enabled; }
}
