// ===== render3d/ThirdPersonCamera.java =====
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
    private static final float LERP_SPEED      = 0.15f;

    // First-person double-press detection
    private static final float DOUBLE_PRESS_WINDOW = 0.35f;

    private float yaw;
    private float pitch;
    private Camera cam;

    private boolean shoulderRight = true;
    private boolean lastRSState   = false;

    private boolean shifting     = false;
    private float   shoulderLerp = 1f;
    private float   lerpTarget   = 1f;

    private CameraMode mode = CameraMode.THIRD_PERSON;

    // Double-press tracking for RS
    private float   lastRSReleaseTime  = -999f;
    private float   timeSinceStart     = 0f;
    private boolean firstPersonAllowed = false;

    public ThirdPersonCamera() {
        yaw   = 0f;
        pitch = FastMath.DEG_TO_RAD * PITCH_DEFAULT;
    }

    public void init(final Camera cam) {
        this.cam = cam;
        applyCamera();
    }

    public void setFirstPersonAllowed(final boolean allowed) {
        firstPersonAllowed = allowed;
        if (!allowed && mode == CameraMode.FIRST_PERSON) {
            mode = CameraMode.THIRD_PERSON;
        }
    }

    public boolean isFirstPersonAllowed() {
        return firstPersonAllowed;
    }

    public CameraMode getMode() {
        return mode;
    }

    public void update(final Camera cam) {
        timeSinceStart += 0.016f;
        handleRotation();
        handleRSPress();
        updateShoulderLerp();
        applyCamera();
    }

    private void handleRotation() {
        final float rx = Main.CONTROLLER.getAxis("rx");
        final float ry = Main.CONTROLLER.getAxis("ry");

        yaw -= rx * ROTATE_SPEED * 0.016f;

        final float pitchMinRad = FastMath.DEG_TO_RAD * PITCH_MIN;
        final float pitchMaxRad = FastMath.DEG_TO_RAD * PITCH_MAX;
        pitch = FastMath.clamp(pitch + ry * ROTATE_SPEED * 0.016f, pitchMinRad, pitchMaxRad);
    }

    private void handleRSPress() {
        final boolean rsPressed = Main.CONTROLLER.isButtonPressed("RS");

        if (rsPressed && !lastRSState) {
            // Button just pressed
            if (mode == CameraMode.FIRST_PERSON) {
                // Any single press returns to third person
                mode = CameraMode.THIRD_PERSON;
            } else if (firstPersonAllowed) {
                // Check double-press window
                final float timeSinceLastRelease = timeSinceStart - lastRSReleaseTime;
                if (timeSinceLastRelease <= DOUBLE_PRESS_WINDOW) {
                    mode = CameraMode.FIRST_PERSON;
                } else {
                    // Single press: shoulder toggle
                    shifting     = true;
                    lerpTarget   = shoulderRight ? 0f : 1f;
                    shoulderRight = !shoulderRight;
                }
            } else {
                // First person not allowed: shoulder toggle only
                shifting     = true;
                lerpTarget   = shoulderRight ? 0f : 1f;
                shoulderRight = !shoulderRight;
            }
        }

        if (!rsPressed && lastRSState) {
            // Button just released — record time for double-press detection
            lastRSReleaseTime = timeSinceStart;
        }

        lastRSState = rsPressed;
    }

    private void updateShoulderLerp() {
        if (shifting) {
            shoulderLerp += (lerpTarget - shoulderLerp) * LERP_SPEED;
            if (Math.abs(shoulderLerp - lerpTarget) < 0.01f) {
                shoulderLerp = lerpTarget;
                shifting     = false;
            }
        } else {
            shoulderLerp = lerpTarget;
        }
    }

    private void applyCamera() {
        if (mode == CameraMode.FIRST_PERSON) {
            applyFirstPerson();
        } else {
            applyThirdPerson();
        }
    }

    private void applyThirdPerson() {
        final Vector3f camPos = calculateThirdPersonPos();
        final Vector3f lookAt = calculateLookAt();
        cam.setLocation(camPos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    private void applyFirstPerson() {
        final float px = Main.PLAYER.getX() / 32f;
        final float pz = Main.PLAYER.getY() / 32f;

        // Eye height slightly below sprite top
        final float eyeHeight = 0.85f;
        final Vector3f eyePos = new Vector3f(px, eyeHeight, pz);

        final float lookDist = 5f;
        final Vector3f lookAt = new Vector3f(
                px + FastMath.sin(yaw) * lookDist,
                eyeHeight,
                pz + FastMath.cos(yaw) * lookDist
        );

        cam.setLocation(eyePos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    private Vector3f calculateThirdPersonPos() {
        final float px = Main.PLAYER.getX() / 32f;
        final float pz = Main.PLAYER.getY() / 32f;

        final Vector3f right = new Vector3f(FastMath.cos(yaw), 0f, -FastMath.sin(yaw));

        final float horizontalDist = FastMath.cos(pitch) * DISTANCE;
        final float verticalDist   = FastMath.sin(pitch) * DISTANCE;

        final Vector3f basePos = new Vector3f(
                px - FastMath.sin(yaw) * horizontalDist,
                verticalDist,
                pz - FastMath.cos(yaw) * horizontalDist
        );

        final float offset = SHOULDER_OFFSET * (2f * shoulderLerp - 1f);
        basePos.addLocal(right.mult(offset));

        return basePos;
    }

    private Vector3f calculateLookAt() {
        final float px = Main.PLAYER.getX() / 32f;
        final float pz = Main.PLAYER.getY() / 32f;

        final Vector3f right  = new Vector3f(FastMath.cos(yaw), 0f, -FastMath.sin(yaw));
        final Vector3f lookAt = new Vector3f(px, LOOK_AT_HEIGHT, pz);

        final float offset = SHOULDER_OFFSET * (2f * shoulderLerp - 1f);
        lookAt.addLocal(right.mult(offset));

        return lookAt;
    }

    public float getYaw() {
        return yaw;
    }
}