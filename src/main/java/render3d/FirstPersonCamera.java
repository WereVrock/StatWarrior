package render3d;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import main.Main;

public final class FirstPersonCamera {

    private static final float ROTATE_SPEED = 2.0f;
    private static final float PITCH_MIN = -80f;
    private static final float PITCH_MAX = 80f;
    private static final float EYE_HEIGHT = 1.85f;
    private static final float LOOK_DIST = 5f;

    private float yaw;
    private float pitch;

    private Camera cam;

    public FirstPersonCamera() {
        yaw = 0f;
        pitch = 0f;
    }

    public void init(final Camera cam) {
        this.cam = cam;
    }

    public void update() {
        handleRotation();
        applyCamera();
    }

    private void handleRotation() {
        final float rx = Main.CONTROLLER.getAxis("rx");
        final float ry = Main.CONTROLLER.getAxis("ry");

        yaw -= rx * ROTATE_SPEED * 0.016f;

        final float min = FastMath.DEG_TO_RAD * PITCH_MIN;
        final float max = FastMath.DEG_TO_RAD * PITCH_MAX;

        pitch = FastMath.clamp(
                pitch + ry * ROTATE_SPEED * 0.016f,
                min,
                max
        );
    }

    private void applyCamera() {
        final float playerX = (Main.PLAYER.centerX()) / dungeon.Dungeon.TILE_SIZE;
        final float playerZ = (Main.PLAYER.centerY()) / dungeon.Dungeon.TILE_SIZE;

        final Vector3f eyePos = new Vector3f(playerX, EYE_HEIGHT, playerZ);

        final float cosPitch = FastMath.cos(pitch);

        final Vector3f dir = new Vector3f(
                FastMath.sin(yaw) * cosPitch,
                FastMath.sin(pitch),
                FastMath.cos(yaw) * cosPitch
        );

        final Vector3f lookAt = eyePos.add(dir.mult(LOOK_DIST));

        cam.setLocation(eyePos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    public float getYaw() {
        return yaw;
    }
}
