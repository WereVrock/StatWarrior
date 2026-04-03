//package render3d;
//
//import main.Main;
//
//import javax.swing.*;
//import java.awt.*;
//
//public final class CameraTunerFrame extends JFrame {
//
//    public CameraTunerFrame() {
//        setTitle("Camera Tuner");
//        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        setAlwaysOnTop(true);
//        setLayout(new BorderLayout(8, 8));
//
//        JPanel sliders = new JPanel(new GridLayout(0, 1, 4, 4));
//        JTextArea output = new JTextArea(8, 40);
//        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//        output.setEditable(false);
//
//        ThirdPersonCamera cam = Main.THIRD_PERSON_CAMERA;
//
//        addSlider(sliders, output, "DISTANCE",        cam.distance,        0f,  20f, cam, "distance");
//        addSlider(sliders, output, "ROTATE_SPEED",    cam.rotateSpeed,     0f,  10f, cam, "rotateSpeed");
//        addSlider(sliders, output, "PITCH default°",  cam.pitchDefault,    1f,  89f, cam, "pitchDefault");
//        addSlider(sliders, output, "PITCH_MIN°",      cam.pitchMin,        1f,  89f, cam, "pitchMin");
//        addSlider(sliders, output, "PITCH_MAX°",      cam.pitchMax,        1f,  89f, cam, "pitchMax");
//        addSlider(sliders, output, "SHOULDER_OFFSET", cam.shoulderOffset, -5f,   5f, cam, "shoulderOffset");
//        addSlider(sliders, output, "LOOK_AT_HEIGHT",  cam.lookAtHeight,   -2f,   6f, cam, "lookAtHeight");
//
//        JButton copy = new JButton("Copy");
//        copy.addActionListener(e -> {
//            output.selectAll();
//            output.copy();
//        });
//
//        updateOutput(output, cam);
//
//        add(sliders, BorderLayout.CENTER);
//        JPanel bottom = new JPanel(new BorderLayout(4, 4));
//        bottom.add(new JScrollPane(output), BorderLayout.CENTER);
//        bottom.add(copy, BorderLayout.SOUTH);
//        add(bottom, BorderLayout.SOUTH);
//
//        pack();
//        setLocationRelativeTo(null);
//        setVisible(true);
//    }
//
//    private void addSlider(JPanel panel, JTextArea output, String label,
//                           float initial, float min, float max,
//                           ThirdPersonCamera cam, String field) {
//        int steps = 1000;
//        int initTick = Math.round((initial - min) / (max - min) * steps);
//
//        JLabel lbl = new JLabel(label + ": " + fmt(initial));
//        JSlider slider = new JSlider(0, steps, initTick);
//
//        slider.addChangeListener(e -> {
//            float v = min + (slider.getValue() / (float) steps) * (max - min);
//            lbl.setText(label + ": " + fmt(v));
//            setField(cam, field, v);
//            updateOutput(output, cam);
//            cam.refresh();
//        });
//
//        JPanel row = new JPanel(new BorderLayout(6, 0));
//        row.add(lbl, BorderLayout.WEST);
//        row.add(slider, BorderLayout.CENTER);
//        panel.add(row);
//    }
//
//    private void setField(ThirdPersonCamera cam, String field, float value) {
//        switch (field) {
//            case "distance"       -> cam.distance       = value;
//            case "rotateSpeed"    -> cam.rotateSpeed    = value;
//            case "pitchDefault"   -> cam.pitchDefault   = value;
//            case "pitchMin"       -> cam.pitchMin       = value;
//            case "pitchMax"       -> cam.pitchMax       = value;
//            case "shoulderOffset" -> cam.shoulderOffset = value;
//            case "lookAtHeight"   -> cam.lookAtHeight   = value;
//        }
//    }
//
//    private void updateOutput(JTextArea output, ThirdPersonCamera cam) {
//        output.setText(
//            "private static final float DISTANCE        = " + fmt(cam.distance)       + "f;\n" +
//            "private static final float ROTATE_SPEED    = " + fmt(cam.rotateSpeed)    + "f;\n" +
//            "private static final float PITCH_MIN       = FastMath.DEG_TO_RAD * " + fmt(cam.pitchMin)     + "f;\n" +
//            "private static final float PITCH_MAX       = FastMath.DEG_TO_RAD * " + fmt(cam.pitchMax)     + "f;\n" +
//            "private static final float SHOULDER_OFFSET = " + fmt(cam.shoulderOffset) + "f;\n" +
//            "private static final float LOOK_AT_HEIGHT  = " + fmt(cam.lookAtHeight)   + "f;\n" +
//            "private float pitch = FastMath.DEG_TO_RAD * " + fmt(cam.pitchDefault)    + "f;\n"
//        );
//    }
//
//    private String fmt(float v) {
//        return String.format("%.2f", v);
//    }
//}