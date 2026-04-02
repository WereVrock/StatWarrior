package controls;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public final class KeyboardController extends KeyAdapter implements InputController {

    private final Set<Integer> keys = new HashSet<>();

    @Override
    public void update() {
        // nothing needed
    }

    @Override
    public boolean isUpPressed() { return keys.contains(KeyEvent.VK_W); }

    @Override
    public boolean isDownPressed() { return keys.contains(KeyEvent.VK_S); }

    @Override
    public boolean isLeftPressed() { return keys.contains(KeyEvent.VK_A); }

    @Override
    public boolean isRightPressed() { return keys.contains(KeyEvent.VK_D); }

    @Override
    public boolean isButtonPressed(String button) {
        return false;
    }

    @Override
    public float getAxis(String axis) {
        return 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove(e.getKeyCode());
    }
}