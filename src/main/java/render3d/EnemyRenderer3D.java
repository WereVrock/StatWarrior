package render3d;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import entity.Enemy;
import entity.EnemyManager;
import entity.EnemyState;
import main.Main;

import java.util.ArrayList;
import java.util.List;

public final class EnemyRenderer3D {

    private static final float RADIUS        = 0.3f;
    private static final float HEIGHT        = 1.0f;
    private static final int   RADIAL_SAMPLES = 16;

    private static final ColorRGBA COLOR_WANDER = ColorRGBA.Green;
    private static final ColorRGBA COLOR_CHASE  = ColorRGBA.Yellow;
    private static final ColorRGBA COLOR_ATTACK = ColorRGBA.Red;

    private static final List<Geometry> geos = new ArrayList<>();
    private static final List<Material> mats = new ArrayList<>();

    private EnemyRenderer3D() {}

    public static void init(final EnemyManager manager) {
        for (int i = 0; i < manager.getEnemies().size(); i++) {
            final Cylinder cylinder = new Cylinder(2, RADIAL_SAMPLES, RADIUS, HEIGHT, true);
            final Geometry geo = new Geometry("Enemy_" + i, cylinder);
            geo.rotate((float) Math.PI / 2f, 0f, 0f);

            final Material mat = new Material(
                    GameApplication.APP.getAssetManager(),
                    "Common/MatDefs/Misc/Unshaded.j3md"
            );
            mat.setColor("Color", COLOR_WANDER);

            geo.setMaterial(mat);
            GameApplication.APP.getRootNode().attachChild(geo);

            geos.add(geo);
            mats.add(mat);
        }
    }

    public static void update(final EnemyManager manager) {
        final List<Enemy> enemies = manager.getEnemies();

        for (int i = 0; i < enemies.size(); i++) {
            final Enemy enemy = enemies.get(i);
            final Geometry geo = geos.get(i);
            final Material mat = mats.get(i);

            geo.setLocalTranslation(
                    enemy.getX() / 32f,
                    HEIGHT / 2f,
                    enemy.getY() / 32f
            );

            final ColorRGBA color = stateColor(enemy.getState());
            mat.setColor("Color", color);
        }
    }

    private static ColorRGBA stateColor(final EnemyState state) {
        return switch (state) {
            case WANDER -> COLOR_WANDER;
            case CHASE  -> COLOR_CHASE;
            case ATTACK -> COLOR_ATTACK;
        };
    }
}