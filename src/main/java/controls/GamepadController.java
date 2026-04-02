package controls;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

public final class GamepadController implements InputController {

    private static final float DEADZONE = 0.2f;

    private static final ControllerManager MANAGER = new ControllerManager();

    static {
        MANAGER.initSDLGamepad();
    }

    private final int index;

    private ControllerState state;

    public GamepadController(final int index) {
        this.index = index;
    }

    @Override
    public void update() {
        MANAGER.update(); // 🔥 REQUIRED in this version
        state = MANAGER.getState(index); // 🔥 correct API
    }

    public boolean isConnected() {
        return state != null && state.isConnected;
    }

    private float applyDeadzone(float value) {
        return Math.abs(value) < DEADZONE ? 0f : value;
    }

    // =========================
    // Movement
    // =========================

    @Override
    public boolean isUpPressed() {
        // 🔁 inverted
        return state != null && applyDeadzone(state.leftStickY) > 0.5f;
    }

    @Override
    public boolean isDownPressed() {
        // 🔁 inverted
        return state != null && applyDeadzone(state.leftStickY) < -0.5f;
    }

    @Override
    public boolean isLeftPressed() {
        return state != null && applyDeadzone(state.leftStickX) < -0.5f;
    }

    @Override
    public boolean isRightPressed() {
        return state != null && applyDeadzone(state.leftStickX) > 0.5f;
    }

    // =========================
    // Buttons
    // =========================

    @Override
    public boolean isButtonPressed(String button) {
        if (state == null) return false;

        return switch (button) {
            case "A" -> state.a;
            case "B" -> state.b;
            case "X" -> state.x;
            case "Y" -> state.y;
            case "LB" -> state.lb;
            case "RB" -> state.rb;
            case "START" -> state.start;
            default -> false;
        };
    }

    @Override
    public float getAxis(String axis) {
        if (state == null) return 0f;

        return switch (axis) {
            case "x" -> applyDeadzone(state.leftStickX);
            case "y" -> applyDeadzone(state.leftStickY);
            case "rx" -> applyDeadzone(state.rightStickX);
            case "ry" -> applyDeadzone(state.rightStickY);
            default -> 0f;
        };
    }
}