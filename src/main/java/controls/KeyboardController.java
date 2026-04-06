// ===== controls/KeyboardController.java =====
package controls;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public final class KeyboardController extends KeyAdapter implements InputController {

    private final Set<Integer> keys = new HashSet<>();

    @Override
    public void update() {}

    @Override public boolean isUpPressed()    { return keys.contains(KeyEvent.VK_W); }
    @Override public boolean isDownPressed()  { return keys.contains(KeyEvent.VK_S); }
    @Override public boolean isLeftPressed()  { return keys.contains(KeyEvent.VK_A); }
    @Override public boolean isRightPressed() { return keys.contains(KeyEvent.VK_D); }

    @Override
    public boolean isButtonPressed(final String button) {
        return switch (button) {
            case "A"          -> keys.contains(KeyEvent.VK_J);
            case "B"          -> keys.contains(KeyEvent.VK_K);
            case "X"          -> keys.contains(KeyEvent.VK_L);
            case "Y"          -> keys.contains(KeyEvent.VK_I);
            case "LB"         -> keys.contains(KeyEvent.VK_Q);
            case "RB"         -> keys.contains(KeyEvent.VK_E);
            case "LT"         -> keys.contains(KeyEvent.VK_SHIFT);
            case "RT"         -> keys.contains(KeyEvent.VK_SPACE);
            case "LS"         -> keys.contains(KeyEvent.VK_F);
            case "RS"         -> keys.contains(KeyEvent.VK_R);
            case "START"      -> keys.contains(KeyEvent.VK_ESCAPE);
            case "BACK"       -> keys.contains(KeyEvent.VK_TAB);
            case "DPAD_UP"    -> keys.contains(KeyEvent.VK_UP);
            case "DPAD_DOWN"  -> keys.contains(KeyEvent.VK_DOWN);
            case "DPAD_LEFT"  -> keys.contains(KeyEvent.VK_LEFT);
            case "DPAD_RIGHT" -> keys.contains(KeyEvent.VK_RIGHT);
            default           -> false;
        };
    }

    @Override
    public float getAxis(final String axis) {
        return switch (axis) {
            case "x"  -> keys.contains(KeyEvent.VK_D) ? 1f :
                         keys.contains(KeyEvent.VK_A) ? -1f : 0f;
            case "y"  -> keys.contains(KeyEvent.VK_W) ? 1f :
                         keys.contains(KeyEvent.VK_S) ? -1f : 0f;
            case "rx" -> keys.contains(KeyEvent.VK_RIGHT) ? 1f :
                         keys.contains(KeyEvent.VK_LEFT)  ? -1f : 0f;
            case "ry" -> keys.contains(KeyEvent.VK_UP)   ? 1f :
                         keys.contains(KeyEvent.VK_DOWN)  ? -1f : 0f;
            default   -> 0f;
        };
    }

    @Override public void keyPressed(final KeyEvent e)  { keys.add(e.getKeyCode());    }
    @Override public void keyReleased(final KeyEvent e) { keys.remove(e.getKeyCode()); }
}