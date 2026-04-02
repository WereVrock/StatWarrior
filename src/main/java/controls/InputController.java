package controls;

public interface InputController {

    boolean isUpPressed();
    boolean isDownPressed();
    boolean isLeftPressed();
    boolean isRightPressed();

    // Generic button access
    boolean isButtonPressed(String button);

    // Axis values (-1 to 1)
    float getAxis(String axis);
}