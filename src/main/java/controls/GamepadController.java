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
        MANAGER.update();
        state = MANAGER.getState(index);
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
        return state != null && applyDeadzone(state.leftStickY) > 0.5f;
    }

    @Override
    public boolean isDownPressed() {
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
            // Face buttons
            case "A" -> state.a;
            case "B" -> state.b;
            case "X" -> state.x;
            case "Y" -> state.y;

            // Bumpers
            case "LB" -> state.lb;
            case "RB" -> state.rb;

            // Stick clicks
            case "LS" -> state.leftStickClick;
            case "RS" -> state.rightStickClick;

            // Start / Back
            case "START" -> state.start;
            case "BACK" -> state.back;

            // D-pad
            case "DPAD_UP"    -> state.dpadUp;
            case "DPAD_DOWN"  -> state.dpadDown;
            case "DPAD_LEFT"  -> state.dpadLeft;
            case "DPAD_RIGHT" -> state.dpadRight;

            default -> false;
        };
    }

    // =========================
    // Axes
    // =========================

    @Override
    public float getAxis(String axis) {
        if (state == null) return 0f;

        return switch (axis) {
            case "x"  -> applyDeadzone(state.leftStickX);
            case "y"  -> applyDeadzone(state.leftStickY);
            case "rx" -> applyDeadzone(state.rightStickX);
            case "ry" -> applyDeadzone(state.rightStickY);
            default   -> 0f;
        };
    }
}