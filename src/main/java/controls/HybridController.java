package controls;

public final class HybridController implements InputController {

    private final InputController keyboard;
    private final GamepadController gamepad;

    public HybridController(InputController keyboard, GamepadController gamepad) {
        this.keyboard = keyboard;
        this.gamepad = gamepad;
    }

    @Override
    public void update() {
        keyboard.update();
        gamepad.update();
    }

    private boolean useGamepad() {
        return gamepad.isConnected();
    }

    @Override
    public boolean isUpPressed() {
        return (useGamepad() && gamepad.isUpPressed()) || keyboard.isUpPressed();
    }

    @Override
    public boolean isDownPressed() {
        return (useGamepad() && gamepad.isDownPressed()) || keyboard.isDownPressed();
    }

    @Override
    public boolean isLeftPressed() {
        return (useGamepad() && gamepad.isLeftPressed()) || keyboard.isLeftPressed();
    }

    @Override
    public boolean isRightPressed() {
        return (useGamepad() && gamepad.isRightPressed()) || keyboard.isRightPressed();
    }

    @Override
    public boolean isButtonPressed(String button) {
        return (useGamepad() && gamepad.isButtonPressed(button)) ||
               keyboard.isButtonPressed(button);
    }

    @Override
    public float getAxis(String axis) {
        if (useGamepad()) {
            float val = gamepad.getAxis(axis);
            if (val != 0f) return val;
        }
        return keyboard.getAxis(axis);
    }
}