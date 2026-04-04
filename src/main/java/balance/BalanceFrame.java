package balance;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BalanceFrame extends JFrame {

    private final Map<Field, JComponent> fieldInputs = new LinkedHashMap<>();
    private final JCheckBox autoApplyCheck = new JCheckBox("Auto Apply");

    public BalanceFrame() {
        setTitle("Balance Editor");
        setSize(500, 350);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(0, 2));
        buildFields(fieldsPanel);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 5)); // Updated grid layout for the extra button

        JButton applyButton = new JButton("Apply");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        JButton revertButton = new JButton("Revert");
        JButton copyButton = new JButton("Copy to Clipboard"); // New copy button

        buttonsPanel.add(applyButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(loadButton);
        buttonsPanel.add(revertButton);
        buttonsPanel.add(copyButton); // Added to panel

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(autoApplyCheck, BorderLayout.WEST);
        bottomPanel.add(buttonsPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        // Adjust scroll speed (default scroll speed is too slow)
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            JScrollBar bar = (JScrollBar) e.getSource();
            bar.setUnitIncrement(16); // Increase scroll speed
        });

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

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

        // Add action listener for copy button
        copyButton.addActionListener(e -> copyToClipboard());

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

                JComponent input;

                if (isNumeric(field.getType())) {
                    NumericFieldPanel numeric = new NumericFieldPanel(field.getType());

                    numeric.setOnValueChanged(() -> triggerAutoApply());

                    input = numeric;

                } else {
                    JTextField textField = new JTextField();

                    textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { triggerAutoApply(); }
                        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { triggerAutoApply(); }
                        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { triggerAutoApply(); }
                    });

                    input = textField;
                }

                fieldInputs.put(field, input);

                panel.add(label);
                panel.add(input);
            }
        }
    }

    private void triggerAutoApply() {
        if (!autoApplyCheck.isSelected()) return;

        SwingUtilities.invokeLater(() -> applyChangesSafe());
    }

    private boolean isNumeric(Class<?> type) {
        return type == int.class ||
               type == long.class ||
               type == float.class ||
               type == double.class;
    }

    private String formatFieldName(String name) {
        return name.replace("_", " ").toLowerCase();
    }

    private void applyChanges(ActionEvent e) {
        applyChangesSafe();
    }

    private boolean applyChangesSafe() {
        try {
            for (Map.Entry<Field, JComponent> entry : fieldInputs.entrySet()) {
                Field field = entry.getKey();
                JComponent comp = entry.getValue();

                String text;

                if (comp instanceof NumericFieldPanel) {
                    text = ((NumericFieldPanel) comp).getText();
                } else {
                    text = ((JTextField) comp).getText();
                }

                if (text == null || text.isEmpty() || text.equals("-") || text.equals(".")) {
                    return false;
                }

                Object casted = ReflectionUtil.castValue(
                        field.getType(),
                        parseInput(field.getType(), text)
                );

                field.set(null, casted);
            }
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    private Object parseInput(Class<?> type, String text) {
        if (type == int.class) return Integer.parseInt(text);
        if (type == long.class) return Long.parseLong(text);
        if (type == float.class) return Float.parseFloat(text);
        if (type == double.class) return Double.parseDouble(text);
        if (type == boolean.class) return Boolean.parseBoolean(text);
        if (type == String.class) return text;

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    private void refreshFields() {
        for (Map.Entry<Field, JComponent> entry : fieldInputs.entrySet()) {
            try {
                Object value = entry.getKey().get(null);
                JComponent comp = entry.getValue();

                if (comp instanceof NumericFieldPanel) {
                    ((NumericFieldPanel) comp).setValue(value);
                } else {
                    ((JTextField) comp).setText(String.valueOf(value));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // New method to copy field values to clipboard
    private void copyToClipboard() {
        StringBuilder clipboardContent = new StringBuilder();
        for (Map.Entry<Field, JComponent> entry : fieldInputs.entrySet()) {
            Field field = entry.getKey();
            try {
                String fieldName = field.getName();
                Object fieldValue = field.get(null);
                clipboardContent.append(fieldName).append("=").append(fieldValue).append("\n");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        StringSelection selection = new StringSelection(clipboardContent.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        JOptionPane.showMessageDialog(this, "Copied to clipboard!");
    }
}