// ===== render3d/PauseMenu.java =====
package render3d;

import javax.swing.*;
import java.awt.*;

/**
 * Pause / start menu shown as a JFrame overlay.
 * Wired to: Continue, Restart, Exit, and Options (First Person toggle).
 */
public final class PauseMenu extends JFrame {

    private static final String TITLE = "Menu";

    private final Runnable onContinue;
    private final Runnable onRestart;

    public PauseMenu(final Runnable onContinue, final Runnable onRestart) {
        super(TITLE);
        this.onContinue = onContinue;
        this.onRestart  = onRestart;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(final java.awt.event.WindowEvent e) {
                AppLifecycle.exit();
            }
        });

        buildUI();
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void buildUI() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        final JButton continueBtn = new JButton("Continue");
        final JButton restartBtn  = new JButton("Restart");
        final JButton optionsBtn  = new JButton("Options");
        final JButton exitBtn     = new JButton("Exit");

        continueBtn.addActionListener(e -> {
            setVisible(false);
            onContinue.run();
        });

        restartBtn.addActionListener(e -> {
            setVisible(false);
            onRestart.run();
        });

        optionsBtn.addActionListener(e -> openOptions());

        exitBtn.addActionListener(e -> AppLifecycle.exit());

        panel.add(continueBtn);
        panel.add(restartBtn);
        panel.add(optionsBtn);
        panel.add(exitBtn);

        add(panel);
    }

    private void openOptions() {
        final OptionsMenu options = new OptionsMenu(this);
        options.setVisible(true);
    }
}