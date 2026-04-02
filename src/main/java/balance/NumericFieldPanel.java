package balance;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.DecimalFormat;

public final class NumericFieldPanel extends JPanel {

    private static final double STEP_SMALL = 0.1;
    private static final double STEP_BIG = 1.0;

    private static final int TIMER_DELAY = 120;

    private final JTextField field;
    private final Class<?> type;

    private final JCheckBox autoApplyCheck;
    private Timer holdTimer;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public NumericFieldPanel(Class<?> type) {
        this.type = type;
        this.field = new JTextField();
        this.autoApplyCheck = new JCheckBox();

        setLayout(new BorderLayout());

        ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericFilter(type));

        JPanel buttons = new JPanel(new GridLayout(2, 2));

        buttons.add(createButton("+1", STEP_BIG));
        buttons.add(createButton("-1", -STEP_BIG));
        buttons.add(createButton("+0.1", STEP_SMALL));
        buttons.add(createButton("-0.1", -STEP_SMALL));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(buttons, BorderLayout.CENTER);
        rightPanel.add(autoApplyCheck, BorderLayout.SOUTH);

        add(field, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createButton(String label, double delta) {
        JButton button = new JButton(label);

        // CLICK
        button.addActionListener(e -> adjustValue(delta));

        // HOLD (only if checkbox is ticked)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!autoApplyCheck.isSelected()) return;

                holdTimer = new Timer(TIMER_DELAY, ev -> adjustValue(delta));
                holdTimer.start();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (holdTimer != null) {
                    holdTimer.stop();
                }
            }
        });

        return button;
    }

    private void adjustValue(double delta) {
        try {
            double current = Double.parseDouble(field.getText());
            double newValue = current + delta;

            if (type == int.class || type == long.class) {
                field.setText(String.valueOf((long) newValue));
            } else {
                field.setText(formatDouble(newValue));
            }

        } catch (Exception ignored) {
            field.setText("0");
        }
    }

    private String formatDouble(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public void setValue(Object value) {
        if (value instanceof Number && !(type == int.class || type == long.class)) {
            field.setText(formatDouble(((Number) value).doubleValue()));
        } else {
            field.setText(String.valueOf(value));
        }
    }

    public String getText() {
        return field.getText();
    }

    // === FILTER CLASS ===
    private static final class NumericFilter extends DocumentFilter {

        private final Class<?> type;

        public NumericFilter(Class<?> type) {
            this.type = type;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {

            if (isValid(fb, offset, 0, string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            if (isValid(fb, offset, length, text)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValid(FilterBypass fb, int offset, int length, String text) {
            try {
                String old = fb.getDocument().getText(0, fb.getDocument().getLength());
                StringBuilder sb = new StringBuilder(old);
                sb.replace(offset, offset + length, text);

                String result = sb.toString();

                if (result.isEmpty() || result.equals("-") || result.equals(".")) {
                    return true;
                }

                if (type == int.class) {
                    Integer.parseInt(result);
                } else if (type == long.class) {
                    Long.parseLong(result);
                } else {
                    // decimal limit check
                    if (result.contains(".")) {
                        int decimals = result.substring(result.indexOf('.') + 1).length();
                        if (decimals > 2) return false;
                    }

                    Double.parseDouble(result);
                }

                return true;

            } catch (Exception e) {
                return false;
            }
        }
    }
}