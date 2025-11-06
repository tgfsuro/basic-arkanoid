package game;

import game.mainhall.MainHall;

import javax.swing.JFrame;

public class GameWindow extends JFrame implements MainHall.Listener {

    private MainHall hall;  // giữ tham chiếu để stop nhạc sảnh khi chuyển màn

    public GameWindow() {
        setTitle("Basic Arkanoid");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        hall = new MainHall(800, 600, this);
        setContentPane(hall);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override public void onStartGame() {
        // DỨT ĐIỂM: tắt nhạc sảnh trước khi vào game
        if (hall != null) hall.stopHallMusic();

        GamePanel panel = new GamePanel(800, 600);
        setContentPane(panel);
        revalidate();
        panel.requestFocusInWindow();

        // không cần giữ hall nữa (tránh GC giữ tham chiếu)
        hall = null;
    }

    @Override public void onExitRequested() {
        dispose();
        System.exit(0);
    }
}
