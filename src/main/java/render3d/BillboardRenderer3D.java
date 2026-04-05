package render3d;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public final class BillboardRenderer3D {

    private static final float SPRITE_WIDTH  = 1.0f;
    private static final float SPRITE_HEIGHT = 1.0f;

    private BillboardRenderer3D() {}

    public static void faceCamera(final Geometry geo,
                                  final float worldX, final float worldY,
                                  final float yOffset) {
        final Vector3f camPos = GameApplication.APP.getCamera().getLocation();
        final float    geoX   = worldX / 32f;
        final float    geoZ   = worldY / 32f;

        final float dx  = camPos.x - geoX;
        final float dz  = camPos.z - geoZ;
        final float yaw = FastMath.atan2(dx, dz);

        final Quaternion rot = new Quaternion();
        rot.fromAngles(0f, yaw, 0f);
        geo.setLocalRotation(rot);

        // Center horizontally: offset left by half width in local space before rotation
        // Center vertically: offset down by half height
        // We apply centering by translating the geometry origin after rotation
        // Quad origin is bottom-left, so shift left by WIDTH/2 and down by HEIGHT/2
        final float halfW = SPRITE_WIDTH  / 2f;
        final float halfH = SPRITE_HEIGHT / 2f;

        // Build the centered position: geoX/geoZ is character center,
        // yOffset is bob/shake, subtract halfH so sprite center aligns with character center
        final Vector3f pos = new Vector3f(geoX, yOffset - halfH, geoZ);

        // Apply local horizontal centering along the billboard's facing axis
        // right vector in world space for this yaw
        final float rightX = FastMath.cos(yaw);
        final float rightZ = -FastMath.sin(yaw);

        pos.x -= rightX * halfW;
        pos.z -= rightZ * halfW;

        geo.setLocalTranslation(pos);
    }
}