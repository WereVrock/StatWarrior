package render3d.worldRender;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import entity.Projectile;
import entity.ProjectileManager;

import java.util.ArrayList;
import java.util.List;
import render3d.GameApplication;

public final class ProjectileRenderer3D {

    private static final float RADIUS         = 0.15f;
    private static final int   SAMPLES        = 8;
    private static final float HEIGHT_OFFSET  = 0.5f;
    private static final ColorRGBA COLOR      = ColorRGBA.Orange;

    private static final List<Geometry> pool    = new ArrayList<>();
    private static final List<Geometry> active  = new ArrayList<>();

    private ProjectileRenderer3D() {}

    public static void update(final ProjectileManager manager) {
        final List<Projectile> projectiles = manager.getProjectiles();

        // Return all active geos to pool
        for (final Geometry geo : active) {
            geo.removeFromParent();
        }
        active.clear();

        // Place one geo per live projectile
        for (final Projectile p : projectiles) {
            final Geometry geo = acquireGeo();
            geo.setLocalTranslation(p.getX() / 32f, HEIGHT_OFFSET, p.getY() / 32f);
            GameApplication.APP.getRootNode().attachChild(geo);
            active.add(geo);
        }
    }

    private static Geometry acquireGeo() {
        if (!pool.isEmpty()) {
            return pool.remove(pool.size() - 1);
        }

        final Sphere   sphere = new Sphere(SAMPLES, SAMPLES, RADIUS);
        final Geometry geo    = new Geometry("Projectile", sphere);
        final Material mat    = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", COLOR);
        geo.setMaterial(mat);
        return geo;
    }
}