package ui;

import controls.HybridController;
import main.Main;

import javax.swing.*;
import java.awt.*;

public final class GamePanel extends JPanel {

    public GamePanel() {
        setFocusable(true);
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(800, 600));

        if (Main.CONTROLLER instanceof HybridController hybrid) {
            addKeyListener(hybrid.getKeyboard());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 🔥 Just dispatch rendering
        Main.DUNGEON.render(g);
        Main.PLAYER.render(g);
    }
}