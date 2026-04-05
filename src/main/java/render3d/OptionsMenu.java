// ===== render3d/OptionsMenu.java =====
package render3d;

import main.Main;

import javax.swing.*;
import java.awt.*;

/**
 * Options submenu. Currently exposes: First Person Allowed toggle.
 */
public final class OptionsMenu extends JFrame {

    public OptionsMenu(final JFrame parent) {
        super("Options");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        final boolean current = Main.THIRD_PERSON_CAMERA.isFirstPersonAllowed();
        final JCheckBox fpCheck = new JCheckBox("First Person Allowed", current);
        fpCheck.addActionListener(e ->
                Main.THIRD_PERSON_CAMERA.setFirstPersonAllowed(fpCheck.isSelected()));

        final JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        panel.add(fpCheck);
        panel.add(closeBtn);
        add(panel);
    }
}