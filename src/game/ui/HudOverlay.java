package game.ui;

import java.awt.*;

/** Vẽ HUD (Score/Lives/Level) + các nút góc phải (pause/home/settings). */
public class HudOverlay {

    private final Rectangle pauseBtn = new Rectangle();
    private final Rectangle homeBtn  = new Rectangle();
    private final Rectangle settingsBtn = new Rectangle();

    private final int btnW = 26, btnH = 26, btnPad = 10;

    public Rectangle pauseBtn()    { return pauseBtn; }
    public Rectangle homeBtn()     { return homeBtn; }
    public Rectangle settingsBtn() { return settingsBtn; }

    public void drawTopHUD(Graphics2D g2, int score, int lives, String levelText) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2.drawString("Score: " + score, 12, 20);
        g2.drawString("Lives: " + Math.max(0, lives), 120, 20);
        g2.drawString("Level: " + levelText, 200, 20);
    }

    public void drawTopButtons(Graphics2D g2, int panelW, int panelH, Image pauseIcon, Image homeIcon, Image gearIcon) {
        int px = panelW - btnW - btnPad;
        int py = 8;

        if (pauseIcon != null) g2.drawImage(pauseIcon, px, py, null);
        else { g2.setColor(Color.LIGHT_GRAY); g2.fillRect(px+4,py+3,6,20); g2.fillRect(px+16,py+3,6,20); }
        pauseBtn.setBounds(px, py, btnW, btnH);

        int hx = px - btnW - 8;
        if (homeIcon != null) g2.drawImage(homeIcon, hx, py, null);
        else { g2.setColor(Color.LIGHT_GRAY); int[] xs={hx+3,hx+13,hx+23,hx+23,hx+3}; int[] ys={py+14,py+4,py+14,py+24,py+24}; g2.fillPolygon(xs,ys,5); }
        homeBtn.setBounds(hx, py, btnW, btnH);

        int sx = hx - btnW - 8;
        if (gearIcon != null) g2.drawImage(gearIcon, sx, py, null);
        else { g2.setColor(Color.LIGHT_GRAY); g2.drawOval(sx+4,py+4,18,18); g2.drawLine(sx+13,py+4,sx+13,py+22); g2.drawLine(sx+4,py+13,sx+22,py+13); }
        settingsBtn.setBounds(sx, py, btnW, btnH);
    }
}
