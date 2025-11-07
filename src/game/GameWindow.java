package game;

import javax.swing.JFrame;

public class GameWindow extends JFrame {
    public GameWindow() { this(false); }

    /** @param startLevelNow true = vào thẳng Level 1 (serve) ngay khi mở cửa sổ */
    public GameWindow(boolean startLevelNow) {
        setTitle("Basic Arkanoid");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel(800, 600);

        // *** chuẩn bị level trước khi gắn panel lên frame & show ***
        if (startLevelNow) {
            panel.prepareLevel1FromHall(); // bricks + background đã sẵn sàng, PAUSE + serving
        }

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
