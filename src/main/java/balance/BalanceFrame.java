package balance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BalanceFrame extends JFrame {

    private final Map<Field, JTextField> fieldInputs = new LinkedHashMap<>();

    public BalanceFrame() {
        setTitle("Balance Editor");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(0, 2));
        buildFields(fieldsPanel);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 4));

        JButton applyButton = new JButton("Apply");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        JButton revertButton = new JButton("Revert");

        buttonsPanel.add(applyButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(loadButton);
        buttonsPanel.add(revertButton);

        add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        refreshFields();

        applyButton.addActionListener(this::applyChanges);

        saveButton.addActionListener(e -> {
            if (applyChangesSafe()) {
                BalanceStorage.save(Balance.class, Balance.SAVE_PATH);
            }
        });

        loadButton.addActionListener(e -> {
            BalanceStorage.load(Balance.class, Balance.SAVE_PATH);
            refreshFields();
        });

        revertButton.addActionListener(e -> {
            BalanceStorage.load(Balance.class, Balance.SAVE_PATH);
            refreshFields();
        });

        setVisible(true);
    }

    private void buildFields(JPanel panel) {
        for (Field field : Balance.class.getDeclaredFields()) {
            int mods = field.getModifiers();

            if (Modifier.isStatic(mods) &&
                Modifier.isPublic(mods) &&
                !Modifier.isFinal(mods)) {

                field.setAccessible(true);

                JLabel label = new JLabel(formatFieldName(field.getName()));
                JTextField textField = new JTextField();

                fieldInputs.put(field, textField);

                panel.add(label);
                panel.add(textField);
            }
        }
    }

    private String formatFieldName(String name) {
        return name.replace("_", " ").toLowerCase();
    }

    private void applyChanges(ActionEvent e) {
        applyChangesSafe();
    }

    private boolean applyChangesSafe() {
        try {
            for (Map.Entry<Field, JTextField> entry : fieldInputs.entrySet()) {
                Field field = entry.getKey();
                JTextField input = entry.getValue();

                Object casted = ReflectionUtil.castValue(
                        field.getType(),
                        parseInput(field.getType(), input.getText())
                );

                field.set(null, casted);
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid input");
            return false;
        }
    }

    private Object parseInput(Class<?> type, String text) {
        if (type == int.class) return Integer.parseInt(text);
        if (type == float.class) return Float.parseFloat(text);
        if (type == double.class) return Double.parseDouble(text);
        if (type == boolean.class) return Boolean.parseBoolean(text);
        if (type == String.class) return text;

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    private void refreshFields() {
        for (Map.Entry<Field, JTextField> entry : fieldInputs.entrySet()) {
            try {
                Object value = entry.getKey().get(null);
                entry.getValue().setText(String.valueOf(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}