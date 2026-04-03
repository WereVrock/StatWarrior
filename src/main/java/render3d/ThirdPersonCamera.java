package render3d;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import main.Main;

public final class ThirdPersonCamera {

  private static final float distance        = 1.78f;
private static final float rotateSpeed    = 2.00f;
private static final float pitchMin       =  10.00f;
private static final float pitchMax       =  63.92f;
private static final float shoulderOffset = 0.54f;
private static final float lookAtHeight  = 1.02f;
private float pitchDefault = 25f;


    private float yaw;
    private float pitch;
    private Camera cam;

    public ThirdPersonCamera() {
        yaw   = 0f;
        pitch = FastMath.DEG_TO_RAD * pitchDefault;
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
        float rx = Main.CONTROLLER.getAxis("rx");
        float ry = Main.CONTROLLER.getAxis("ry");

        yaw -= rx * rotateSpeed * 0.016f;

        float pitchMinRad = FastMath.DEG_TO_RAD * pitchMin;
        float pitchMaxRad = FastMath.DEG_TO_RAD * pitchMax;
        pitch = FastMath.clamp(pitch + ry * rotateSpeed * 0.016f, pitchMinRad, pitchMaxRad);

        applyToCamera(cam, yaw, pitch);
    }

    private void applyToCamera(Camera cam, float yaw, float pitch) {
        float px = Main.PLAYER.getX() / 32f;
        float pz = Main.PLAYER.getY() / 32f;

        Vector3f right = new Vector3f(FastMath.cos(yaw), 0f, -FastMath.sin(yaw));

        float horizontalDist = FastMath.cos(pitch) * distance;
        float verticalDist   = FastMath.sin(pitch) * distance;

        Vector3f camPos = new Vector3f(
                px - FastMath.sin(yaw) * horizontalDist,
                verticalDist,
                pz - FastMath.cos(yaw) * horizontalDist
        );

        Vector3f lookAt = new Vector3f(px, lookAtHeight, pz);

        Vector3f offset = right.mult(shoulderOffset);
        camPos.addLocal(offset);
        lookAt.addLocal(offset);

        cam.setLocation(camPos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    public float getYaw() {
        return yaw;
    }
}