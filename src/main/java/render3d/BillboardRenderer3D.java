package render3d;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public final class BillboardRenderer3D {

    private BillboardRenderer3D() {}

    public static void faceCamera(final Geometry geo,
                                   final float worldX, final float worldY,
                                   final float yOffset) {
        final Vector3f camPos  = GameApplication.APP.getCamera().getLocation();
        final float    geoX    = worldX / 32f;
        final float    geoZ    = worldY / 32f;

        // Yaw only — horizontal facing toward camera, stays upright
        final float    dx      = camPos.x - geoX;
        final float    dz      = camPos.z - geoZ;
        final float    yaw     = FastMath.atan2(dx, dz);

        final Quaternion rot = new Quaternion();
        rot.fromAngles(0f, yaw, 0f);

        geo.setLocalRotation(rot);
        geo.setLocalTranslation(geoX, yOffset, geoZ);
    }
}