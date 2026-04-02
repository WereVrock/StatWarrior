package controls;

public interface InputController {

    void update(); // 🔥 NEW: called once per frame

    boolean isUpPressed();
    boolean isDownPressed();
    boolean isLeftPressed();
    boolean isRightPressed();

    boolean isButtonPressed(String button);

    float getAxis(String axis);
}