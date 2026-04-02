package ui;

import javax.swing.JFrame;

public final class GameFrame extends JFrame {

    private static final String TITLE = "Modular Dungeon";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public GameFrame() {
        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        add(new GamePanel());

        setVisible(true);
    }
}