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
    private static final float LERP_SPEED      = 0.15f; // Only for shoulder shift

    private float yaw;
    private float pitch;
    private Camera cam;

    private boolean shoulderRight = true;
    private boolean lastRSState   = false;

    private boolean shifting      = false;
    private float shoulderLerp    = 0f; // 0 -> left, 1 -> right
    private float lerpStart       = 0f; // starting value when RS pressed
    private float lerpTarget      = 1f; // target value when RS pressed

    public ThirdPersonCamera() {
        yaw = 0f;
        pitch = FastMath.DEG_TO_RAD * PITCH_DEFAULT;
        shoulderLerp = 1f; // start on right shoulder
    }

    public void init(Camera cam) {
        this.cam = cam;
        updateCameraInstant();
    }

    public void refresh() {
        if (cam == null) return;
        GameApplication.APP.enqueue(() -> cam.setLocation(calculateCameraPos()));
    }

    public void update(Camera cam) {
        handleRotation();
        handleShoulderToggle();
        updateShoulderLerp();
        applyCamera();
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
        if (rsPressed && !lastRSState) {
            shifting = true;
            lerpStart = shoulderLerp;
            lerpTarget = shoulderRight ? 0f : 1f; // toggle
            shoulderRight = !shoulderRight;
        }
        lastRSState = rsPressed;
    }

    private void updateShoulderLerp() {
        if (shifting) {
            shoulderLerp += (lerpTarget - shoulderLerp) * LERP_SPEED;
            if (Math.abs(shoulderLerp - lerpTarget) < 0.01f) {
                shoulderLerp = lerpTarget;
                shifting = false;
            }
        } else {
            shoulderLerp = lerpTarget; // instant when not shifting
        }
    }

    private void applyCamera() {
        Vector3f camPos = calculateCameraPos();
        Vector3f lookAt = calculateLookAt();

        cam.setLocation(camPos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    private Vector3f calculateCameraPos() {
        float px = Main.PLAYER.getX() / 32f;
        float pz = Main.PLAYER.getY() / 32f;

        Vector3f right = new Vector3f(FastMath.cos(yaw), 0f, -FastMath.sin(yaw));

        float horizontalDist = FastMath.cos(pitch) * DISTANCE;
        float verticalDist   = FastMath.sin(pitch) * DISTANCE;

        Vector3f basePos = new Vector3f(
                px - FastMath.sin(yaw) * horizontalDist,
                verticalDist,
                pz - FastMath.cos(yaw) * horizontalDist
        );

        float offset = SHOULDER_OFFSET * (2f * shoulderLerp - 1f); // -1 -> left, 1 -> right
        basePos.addLocal(right.mult(offset));

        return basePos;
    }

    private Vector3f calculateLookAt() {
        float px = Main.PLAYER.getX() / 32f;
        float pz = Main.PLAYER.getY() / 32f;

        Vector3f right = new Vector3f(FastMath.cos(yaw), 0f, -FastMath.sin(yaw));
        Vector3f lookAt = new Vector3f(px, LOOK_AT_HEIGHT, pz);

        float offset = SHOULDER_OFFSET * (2f * shoulderLerp - 1f);
        lookAt.addLocal(right.mult(offset));

        return lookAt;
    }

    private void updateCameraInstant() {
        cam.setLocation(calculateCameraPos());
        cam.lookAt(calculateLookAt(), Vector3f.UNIT_Y);
    }

    public float getYaw() {
        return yaw;
    }
}