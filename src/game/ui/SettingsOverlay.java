package game.ui;

import java.awt.*;

/** Vẽ overlay Settings + 3 nút (Music toggle / Main menu / Back). */
public class SettingsOverlay {

    public final Rectangle btnMusicToggle = new Rectangle();
    public final Rectangle btnMainMenu    = new Rectangle();
    public final Rectangle btnBack        = new Rectangle();

    public void draw(Graphics2D g2, boolean musicEnabled, int panelW, int panelH) {
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(0, 0, panelW, panelH);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String title = "SETTINGS";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (panelW - tw) / 2, panelH / 3);

        int bw = 260, bh = 36, gap = 14;
        int cx = (panelW - bw) / 2, cy = panelH / 2;

        btnMusicToggle.setBounds(cx, cy, bw, bh);
        drawBtn(g2, btnMusicToggle, musicEnabled ? "Music: ON (pause/resume)" : "Music: OFF");

        btnMainMenu.setBounds(cx, cy + bh + gap, bw, bh);
        drawBtn(g2, btnMainMenu, "Return to MAIN MENU");

        btnBack.setBounds(cx, cy + 2 * (bh + gap), bw, bh);
        drawBtn(g2, btnBack, "Back");
    }

    private void drawBtn(Graphics2D g2, Rectangle r, String text) {
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        int tw = g2.getFontMetrics().stringWidth(text), th = g2.getFontMetrics().getAscent();
        g2.drawString(text, r.x + (r.width - tw) / 2, r.y + (r.height + th) / 2 - 3);
    }
}
