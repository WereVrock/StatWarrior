package render3d;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import main.Main;

public final class ThirdPersonCamera {

    private static final float DISTANCE     = 6f;
    private static final float ROTATE_SPEED = 2f;

    private static final float PITCH_MIN    = FastMath.DEG_TO_RAD * 10f;
    private static final float PITCH_MAX    = FastMath.DEG_TO_RAD * 80f;
    private static final boolean INVERT_PITCH = false;

    private float yaw   = 0f;
    private float pitch = FastMath.DEG_TO_RAD * 35f;

    public void init(Camera cam) {
        update(cam);
    }

    public void update(Camera cam) {
        float rx = Main.CONTROLLER.getAxis("rx");
        float ry = Main.CONTROLLER.getAxis("ry");

        yaw -= rx * ROTATE_SPEED * 0.016f;

        float pitchDelta = ry * ROTATE_SPEED * 0.016f;
        if (INVERT_PITCH) pitchDelta = -pitchDelta;
        pitch = FastMath.clamp(pitch + pitchDelta, PITCH_MIN, PITCH_MAX);

        float px = Main.PLAYER.getX() / 32f;
        float pz = Main.PLAYER.getY() / 32f;

        float horizontalDist = FastMath.cos(pitch) * DISTANCE;
        float verticalDist   = FastMath.sin(pitch) * DISTANCE;

        float camX = px - FastMath.sin(yaw) * horizontalDist;
        float camZ = pz - FastMath.cos(yaw) * horizontalDist;
        float camY = verticalDist;

        Vector3f camPos    = new Vector3f(camX, camY, camZ);
        Vector3f playerPos = new Vector3f(px, 0.5f, pz);

        cam.setLocation(camPos);
        cam.lookAt(playerPos, Vector3f.UNIT_Y);
    }

    public float getYaw() {
        return yaw;
    }
}