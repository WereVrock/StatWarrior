package render3d;

import com.jme3.renderer.Camera;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A standalone Swing JFrame that exposes all camera frustum and FOV values
 * as interactive sliders. Each slider has editable min/max fields and applies
 * changes to the JME camera instantly.
 *
 * Start from GameApplication via:  CameraDebugPanel.open(() -> cam);
 */
public final class CameraDebugPanel extends JFrame {

    // -------------------------------------------------------------------------
    // Defaults / initial slider ranges
    // -------------------------------------------------------------------------
    private static final float DEFAULT_NEAR_MIN   = 0.01f;
    private static final float DEFAULT_NEAR_MAX   = 5f;
    private static final float DEFAULT_FAR_MIN    = 10f;
    private static final float DEFAULT_FAR_MAX    = 2000f;
    private static final float DEFAULT_FOV_MIN    = 10f;
    private static final float DEFAULT_FOV_MAX    = 150f;
    private static final float DEFAULT_LEFT_MIN   = -5f;
    private static final float DEFAULT_LEFT_MAX   = 0f;
    private static final float DEFAULT_RIGHT_MIN  = 0f;
    private static final float DEFAULT_RIGHT_MAX  = 5f;
    private static final float DEFAULT_TOP_MIN    = 0f;
    private static final float DEFAULT_TOP_MAX    = 5f;
    private static final float DEFAULT_BOTTOM_MIN = -5f;
    private static final float DEFAULT_BOTTOM_MAX = 0f;

    private static final int  SLIDER_RESOLUTION = 10_000;
    private static final int  PANEL_WIDTH       = 520;
    private static final int  PANEL_HEIGHT      = 620;
    private static final Font MONO_FONT         = new Font("Monospaced", Font.PLAIN, 12);

    // -------------------------------------------------------------------------
    // Camera supplier (called on every access so we always use the live instance)
    // -------------------------------------------------------------------------
    public interface CameraSupplier {
        Camera get();
    }

    private final CameraSupplier    cameraSupplier;
    private final List<CameraParam> params = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------
    public static void open(final CameraSupplier supplier) {
        SwingUtilities.invokeLater(() -> new CameraDebugPanel(supplier).setVisible(true));
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------
    private CameraDebugPanel(final CameraSupplier supplier) {
        super("Camera Debug");
        this.cameraSupplier = supplier;

        // Prevent this window from stealing focus from or minimizing the game
        setFocusableWindowState(false);
        setAutoRequestFocus(false);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(PANEL_WIDTH, PANEL_HEIGHT);
        setResizable(true);
        setAlwaysOnTop(true);

        final JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        buildParams();
        for (final CameraParam p : params) {
            root.add(p.buildRow());
            root.add(Box.createVerticalStrut(4));
        }

        root.add(Box.createVerticalStrut(8));
        root.add(buildShowValuesButton());

        final JScrollPane scroll = new JScrollPane(root);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll);
    }

    private void buildParams() {
        params.add(new CameraParam("Frustum Near",
                DEFAULT_NEAR_MIN, DEFAULT_NEAR_MAX,
                cam -> cam.getFrustumNear(),
                (cam, v) -> cam.setFrustumNear(v)));

        params.add(new CameraParam("Frustum Far",
                DEFAULT_FAR_MIN, DEFAULT_FAR_MAX,
                cam -> cam.getFrustumFar(),
                (cam, v) -> cam.setFrustumFar(v)));

        params.add(new CameraParam("Frustum Left",
                DEFAULT_LEFT_MIN, DEFAULT_LEFT_MAX,
                cam -> cam.getFrustumLeft(),
                (cam, v) -> cam.setFrustumLeft(v)));

        params.add(new CameraParam("Frustum Right",
                DEFAULT_RIGHT_MIN, DEFAULT_RIGHT_MAX,
                cam -> cam.getFrustumRight(),
                (cam, v) -> cam.setFrustumRight(v)));

        params.add(new CameraParam("Frustum Top",
                DEFAULT_TOP_MIN, DEFAULT_TOP_MAX,
                cam -> cam.getFrustumTop(),
                (cam, v) -> cam.setFrustumTop(v)));

        params.add(new CameraParam("Frustum Bottom",
                DEFAULT_BOTTOM_MIN, DEFAULT_BOTTOM_MAX,
                cam -> cam.getFrustumBottom(),
                (cam, v) -> cam.setFrustumBottom(v)));

        params.add(new CameraParam("Field of View (°)",
                DEFAULT_FOV_MIN, DEFAULT_FOV_MAX,
                cam -> cam.getFov(),
                (cam, v) -> {
                    final float aspect = (float) cam.getWidth() / cam.getHeight();
                    cam.setFrustumPerspective(v, aspect, cam.getFrustumNear(), cam.getFrustumFar());
                }));
    }

    private JComponent buildShowValuesButton() {
        final JButton btn = new JButton("Show All Current Values");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusable(false);
        btn.addActionListener(e -> showValuesDialog());
        return btn;
    }

    private void showValuesDialog() {
        final Camera cam = cameraSupplier.get();
        if (cam == null) {
            JOptionPane.showMessageDialog(this, "Camera not available yet.", "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final JTextArea area = new JTextArea(buildValuesSnapshot(cam));
        area.setFont(MONO_FONT);
        area.setEditable(false);
        area.setBackground(Color.DARK_GRAY);
        area.setForeground(Color.GREEN);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(area),
                "Camera Values Snapshot",
                JOptionPane.PLAIN_MESSAGE);
    }

    private static String buildValuesSnapshot(final Camera cam) {
        return String.format(
                "Frustum Near   : %.6f%n" +
                "Frustum Far    : %.6f%n" +
                "Frustum Left   : %.6f%n" +
                "Frustum Right  : %.6f%n" +
                "Frustum Top    : %.6f%n" +
                "Frustum Bottom : %.6f%n" +
                "Field of View  : %.6f%n" +
                "Width          : %d%n"   +
                "Height         : %d%n"   +
                "Aspect         : %.6f%n",
                cam.getFrustumNear(),
                cam.getFrustumFar(),
                cam.getFrustumLeft(),
                cam.getFrustumRight(),
                cam.getFrustumTop(),
                cam.getFrustumBottom(),
                cam.getFov(),
                cam.getWidth(),
                cam.getHeight(),
                (float) cam.getWidth() / cam.getHeight()
        );
    }

    // =========================================================================
    // CameraParam — one row: label | min field | slider | max field | current value
    // =========================================================================
    private final class CameraParam {

        interface Getter { float get(Camera cam); }
        interface Setter { void  set(Camera cam, float value); }

        private final String label;
        private final Getter getter;
        private final Setter setter;

        private float rangeMin;
        private float rangeMax;

        private JSlider    slider;
        private JLabel     currentLabel;
        private JTextField minField;
        private JTextField maxField;

        CameraParam(final String label,
                    final float defaultMin, final float defaultMax,
                    final Getter getter,    final Setter setter) {
            this.label    = label;
            this.rangeMin = defaultMin;
            this.rangeMax = defaultMax;
            this.getter   = getter;
            this.setter   = setter;
        }

        JPanel buildRow() {
            final JPanel row = new JPanel(new BorderLayout(4, 2));
            row.setBorder(new TitledBorder(label));

            slider = new JSlider(0, SLIDER_RESOLUTION, valueToSlider(readCurrentValue()));
            slider.setFocusable(false);
            slider.addChangeListener(e -> onSliderMoved());

            minField = new JTextField(String.valueOf(rangeMin), 5);
            maxField = new JTextField(String.valueOf(rangeMax), 5);
            minField.setFont(MONO_FONT);
            maxField.setFont(MONO_FONT);
            minField.setFocusable(false);
            maxField.setFocusable(false);
            minField.addActionListener(e -> onRangeFieldChanged());
            maxField.addActionListener(e -> onRangeFieldChanged());

            currentLabel = new JLabel(formatValue(readCurrentValue()));
            currentLabel.setFont(MONO_FONT);
            currentLabel.setPreferredSize(new Dimension(90, 20));
            currentLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            final JPanel sliderRow = new JPanel(new BorderLayout(4, 0));
            sliderRow.add(minField, BorderLayout.WEST);
            sliderRow.add(slider,   BorderLayout.CENTER);
            sliderRow.add(maxField, BorderLayout.EAST);

            row.add(sliderRow,    BorderLayout.CENTER);
            row.add(currentLabel, BorderLayout.EAST);

            return row;
        }

        private void onSliderMoved() {
            final float value = sliderToValue(slider.getValue());
            currentLabel.setText(formatValue(value));
            applySafe(value);
        }

        private void onRangeFieldChanged() {
            try {
                rangeMin = Float.parseFloat(minField.getText().trim());
                rangeMax = Float.parseFloat(maxField.getText().trim());
                if (rangeMin >= rangeMax) return;
                slider.setValue(valueToSlider(readCurrentValue()));
            } catch (final NumberFormatException ignored) {}
        }

        private void applySafe(final float value) {
            final Camera cam = cameraSupplier.get();
            if (cam == null) return;
            setter.set(cam, value);
        }

        private float readCurrentValue() {
            final Camera cam = cameraSupplier.get();
            if (cam == null) return rangeMin;
            return getter.get(cam);
        }

        private int valueToSlider(final float value) {
            final float clamped = Math.max(rangeMin, Math.min(rangeMax, value));
            return Math.round((clamped - rangeMin) / (rangeMax - rangeMin) * SLIDER_RESOLUTION);
        }

        private float sliderToValue(final int sliderPos) {
            return rangeMin + (sliderPos / (float) SLIDER_RESOLUTION) * (rangeMax - rangeMin);
        }

        private static String formatValue(final float v) {
            return String.format("%.4f", v);
        }
    }
}