// ===== render3d/BillboardRenderer3D.java =====
package render3d.worldRender;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import render3d.GameApplication;

public final class BillboardRenderer3D {

    private static final float SPRITE_WIDTH  = 1.0f;
    private static final float SPRITE_HEIGHT = 1.0f;

    // Top surface of the floor box: box is centered at y=0, half-height=0.1, so top = 0.1*2 = 0.2
    private static final float FLOOR_TOP_Y   = 0.2f;

    private BillboardRenderer3D() {}

    public static void faceCamera(final Geometry geo,
                                  final float worldX, final float worldY,
                                  final float bobOffset) {
        final Vector3f camPos = GameApplication.APP.getCamera().getLocation();
        final float    geoX   = worldX / 32f;
        final float    geoZ   = worldY / 32f;

        final float dx  = camPos.x - geoX;
        final float dz  = camPos.z - geoZ;
        final float yaw = FastMath.atan2(dx, dz);

        final Quaternion rot = new Quaternion();
        rot.fromAngles(0f, yaw, 0f);
        geo.setLocalRotation(rot);

        // Sprite bottom sits exactly on floor top; bob shifts it up from there
        final float bottomY = FLOOR_TOP_Y + bobOffset;
        final float halfW   = SPRITE_WIDTH / 2f;

        final Vector3f pos = new Vector3f(geoX, bottomY, geoZ);

        // Shift left by half-width along the billboard's facing axis so sprite is centered
        final float rightX = FastMath.cos(yaw);
        final float rightZ = -FastMath.sin(yaw);
        pos.x -= rightX * halfW;
        pos.z -= rightZ * halfW;

        geo.setLocalTranslation(pos);
    }
}