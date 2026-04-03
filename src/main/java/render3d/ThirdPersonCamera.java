package render3d;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import main.Main;

public final class ThirdPersonCamera {

    private static final float DISTANCE        = 1.78f;
    private static final float ROTATE_SPEED    = 2.00f;
    private static final float PITCH_MIN       = 10.00f;
    private static final float PITCH_MAX       = 63.92f;
    private static final float SHOULDER_OFFSET = 0.54f;
    private static final float LOOK_AT_HEIGHT  = 1.02f;
    private static final float PITCH_DEFAULT   = 25f;

    private float yaw;
    private float pitch;
    private Camera cam;

    private boolean shoulderRight = true;   // current shoulder side
    private boolean lastRSState   = false;  // previous RS state for edge detection

    public ThirdPersonCamera() {
        yaw   = 0f;
        pitch = FastMath.DEG_TO_RAD * PITCH_DEFAULT;
    }

    public void init(Camera cam) {
        this.cam = cam;
        update(cam);
    }

    public void refresh() {
        if (cam == null) return;
        GameApplication.APP.enqueue(() -> applyToCamera(cam, yaw, pitch));
    }

    public void update(Camera cam) {
        handleRotation();
        handleShoulderToggle();
        applyToCamera(cam, yaw, pitch);
    }

    private void handleRotation() {
        float rx = Main.CONTROLLER.getAxis("rx");
        float ry = Main.CONTROLLER.getAxis("ry");

        yaw -= rx * ROTATE_SPEED * 0.016f;

        float pitchMinRad = FastMath.DEG_TO_RAD * PITCH_MIN;
        float pitchMaxRad = FastMath.DEG_TO_RAD * PITCH_MAX;
        pitch = FastMath.clamp(pitch + ry * ROTATE_SPEED * 0.016f, pitchMinRad, pitchMaxRad);
    }

    private void handleShoulderToggle() {
        boolean rsPressed = Main.CONTROLLER.isButtonPressed("RS");

        // Edge detection: toggle only when RS transitions from unpressed → pressed
        if (rsPressed && !lastRSState) {
            shoulderRight = !shoulderRight;
        }

        lastRSState = rsPressed;
    }

    private void applyToCamera(Camera cam, float yaw, float pitch) {
        float px = Main.PLAYER.getX() / 32f;
        float pz = Main.PLAYER.getY() / 32f;

        Vector3f right = new Vector3f(FastMath.cos(yaw), 0f, -FastMath.sin(yaw));

        float horizontalDist = FastMath.cos(pitch) * DISTANCE;
        float verticalDist   = FastMath.sin(pitch) * DISTANCE;

        Vector3f camPos = new Vector3f(
                px - FastMath.sin(yaw) * horizontalDist,
                verticalDist,
                pz - FastMath.cos(yaw) * horizontalDist
        );

        Vector3f lookAt = new Vector3f(px, LOOK_AT_HEIGHT, pz);

        float offsetMultiplier = shoulderRight ? 1f : -1f;
        Vector3f offset = right.mult(SHOULDER_OFFSET * offsetMultiplier);

        camPos.addLocal(offset);
        lookAt.addLocal(offset);

        cam.setLocation(camPos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    public float getYaw() {
        return yaw;
    }
}