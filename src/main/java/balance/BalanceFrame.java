package balance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class BalanceFrame extends JFrame {

    private final JTextField accelerationField = new JTextField();
    private final JTextField maxSpeedField = new JTextField();
    private final JTextField frictionField = new JTextField();

    public BalanceFrame() {
        setTitle("Balance Editor");
        setSize(300, 200);
        setLayout(new GridLayout(5, 2));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new JLabel("Acceleration:"));
        add(accelerationField);

        add(new JLabel("Max Speed:"));
        add(maxSpeedField);

        add(new JLabel("Friction:"));
        add(frictionField);

        JButton applyButton = new JButton("Apply");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        JButton revertButton = new JButton("Revert");

        add(applyButton);
        add(saveButton);
        add(loadButton);
        add(revertButton);

        refreshFields();

        applyButton.addActionListener(this::applyChanges);

        saveButton.addActionListener(e -> {
            if (applyChangesSafe()) {
                Balance.save();
            }
        });

        loadButton.addActionListener(e -> {
            Balance.load();
            refreshFields();
        });

        revertButton.addActionListener(e -> {
            Balance.revert();
            refreshFields();
        });

        setVisible(true);
    }

    private void applyChanges(ActionEvent e) {
        applyChangesSafe();
    }

    private boolean applyChangesSafe() {
        try {
            Balance.PLAYER_ACCELERATION = Float.parseFloat(accelerationField.getText());
            Balance.PLAYER_MAX_SPEED = Float.parseFloat(maxSpeedField.getText());
            Balance.PLAYER_FRICTION = Float.parseFloat(frictionField.getText());
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input");
            return false;
        }
    }

    private void refreshFields() {
        accelerationField.setText(String.valueOf(Balance.PLAYER_ACCELERATION));
        maxSpeedField.setText(String.valueOf(Balance.PLAYER_MAX_SPEED));
        frictionField.setText(String.valueOf(Balance.PLAYER_FRICTION));
    }
}