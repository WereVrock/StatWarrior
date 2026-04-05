package render3d;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import entity.AttackType;
import entity.Enemy;
import entity.EnemyManager;

import java.util.ArrayList;
import java.util.List;

public final class EnemyRenderer3D {

    private static final float  WIDTH          = 1.0f;
    private static final float  HEIGHT         = 1.0f;
    private static final float  Y_BASE         = HEIGHT / 2f;
    private static final String TEXTURE_MELEE  = "Textures/sprites/enemy_melee.png";
    private static final String TEXTURE_RANGED = "Textures/sprites/enemy_ranged.png";
    private static final String TEXTURE_CHARGE = "Textures/sprites/enemy_charge.png";
    private static final String TEXTURE_ALL    = "Textures/sprites/enemy_all.png";

    private static final List<Geometry> geos = new ArrayList<>();

    private EnemyRenderer3D() {}

    public static void init(final EnemyManager manager) {
        for (final Enemy enemy : manager.getEnemies()) {
            final Quad     quad = new Quad(WIDTH, HEIGHT);
            final Geometry geo  = new Geometry("Enemy_" + enemy.getAttackTypes(), quad);
            final Material mat  = new Material(
                    GameApplication.APP.getAssetManager(),
                    "Common/MatDefs/Misc/Unshaded.j3md"
            );
            final Texture tex = GameApplication.APP.getAssetManager()
                    .loadTexture(texturePath(enemy.getAttackTypes()));
            mat.setTexture("ColorMap", tex);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            geo.setMaterial(mat);
            GameApplication.APP.getRootNode().attachChild(geo);
            geos.add(geo);
        }
    }

    public static void update(final EnemyManager manager) {
        final List<Enemy> enemies = manager.getEnemies();
        for (int i = 0; i < enemies.size(); i++) {
            final Enemy    enemy  = enemies.get(i);
            final Geometry geo    = geos.get(i);

            BillboardRenderer3D.faceCamera(geo, enemy.getX(), enemy.getY(),
                    Y_BASE + enemy.getBobOffset());

            final com.jme3.math.Vector3f pos = geo.getLocalTranslation().clone();
            pos.x += enemy.getShakeOffset();
            geo.setLocalTranslation(pos);
        }
    }

    private static String texturePath(final List<AttackType> types) {
        if (types.size() > 1)                    return TEXTURE_ALL;
        return switch (types.get(0)) {
            case MELEE  -> TEXTURE_MELEE;
            case RANGED -> TEXTURE_RANGED;
            case CHARGE -> TEXTURE_CHARGE;
        };
    }
}