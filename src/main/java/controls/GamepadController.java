package controls;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.util.HashMap;
import java.util.Map;

public final class GamepadController implements InputController {

    private final Controller controller;

    private final Map<String, Float> axes = new HashMap<>();
    private final Map<String, Boolean> buttons = new HashMap<>();

    private static final float DEADZONE = 0.2f;

    public GamepadController() {
        controller = findController();
    }

    private Controller findController() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

        for (Controller c : controllers) {
            if (c.getType() == Controller.Type.GAMEPAD ||
                c.getType() == Controller.Type.STICK) {
                return c;
            }
        }

        throw new IllegalStateException("No gamepad found");
    }

    private void poll() {
        if (!controller.poll()) return;

        for (Component component : controller.getComponents()) {
            String name = component.getIdentifier().getName();
            float value = component.getPollData();

            if (component.isAnalog()) {
                axes.put(name, applyDeadzone(value));
            } else {
                buttons.put(name, value == 1.0f);
            }
        }
    }

    private float applyDeadzone(float value) {
        return Math.abs(value) < DEADZONE ? 0f : value;
    }

    // =========================
    // Movement (uses left stick)
    // =========================

    @Override
    public boolean isUpPressed() {
        poll();
        return getAxis("y") < -0.5f;
    }

    @Override
    public boolean isDownPressed() {
        poll();
        return getAxis("y") > 0.5f;
    }

    @Override
    public boolean isLeftPressed() {
        poll();
        return getAxis("x") < -0.5f;
    }

    @Override
    public boolean isRightPressed() {
        poll();
        return getAxis("x") > 0.5f;
    }

    // =========================
    // Generic access
    // =========================

    @Override
    public boolean isButtonPressed(String button) {
        poll();
        return buttons.getOrDefault(button, false);
    }

    @Override
    public float getAxis(String axis) {
        poll();
        return axes.getOrDefault(axis, 0f);
    }
}