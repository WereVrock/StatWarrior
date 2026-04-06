// ===== render3d/EnemyRenderer3D.java =====
package render3d;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import entity.AttackType;
import entity.enemy.Enemy;
import entity.enemy.EnemyManager;

import java.util.ArrayList;
import java.util.List;

public final class EnemyRenderer3D {

    private static final float  SPRITE_WIDTH   = 1.0f;
    private static final float  SPRITE_HEIGHT  = 1.0f;
    private static final String TEXTURE_MELEE  = "Textures/sprites/enemy_melee.png";
    private static final String TEXTURE_RANGED = "Textures/sprites/enemy_ranged.png";
    private static final String TEXTURE_CHARGE = "Textures/sprites/enemy_charge.png";
    private static final String TEXTURE_ALL    = "Textures/sprites/enemy_all.png";

    /** Max backward tilt in radians when the enemy dies (falls onto its back). */
    private static final float DEATH_TILT_MAX_ANGLE = FastMath.HALF_PI; // 90°

    private static final List<Geometry> geos = new ArrayList<>();

    private EnemyRenderer3D() {}

    public static void init(final EnemyManager manager) {
        geos.clear();
        for (final Enemy enemy : manager.getEnemies()) {
            final Quad     quad = new Quad(SPRITE_WIDTH, SPRITE_HEIGHT);
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
            final Enemy    enemy = enemies.get(i);
            final Geometry geo   = geos.get(i);

            if (enemy.getHealth().isDeathAnimDone()) {
                // Hide sprite entirely once the fall animation has finished
                geo.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                continue;
            }

            geo.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);

            if (enemy.getHealth().isDead()) {
                // Play fall-back tilt: rotate around the local X axis (tip backward)
                positionDeadSprite(geo, enemy);
            } else {
                // Normal billboard facing camera
                BillboardRenderer3D.faceCamera(geo, enemy.getX(), enemy.getY(),
                        enemy.getBobOffset());
                // Apply attack shake on top
                final Vector3f pos = geo.getLocalTranslation().clone();
                pos.x += enemy.getShakeOffset();
                geo.setLocalTranslation(pos);
            }
        }
    }

    /**
     * Rotates the sprite around its bottom edge so it falls onto its back.
     * The pivot is at ground level (y=0) at the sprite's world position.
     */
    private static void positionDeadSprite(final Geometry geo, final Enemy enemy) {
        final float tilt = enemy.getHealth().getDeathTiltFraction() * DEATH_TILT_MAX_ANGLE;

        final float worldX = enemy.getX() / 32f + SPRITE_WIDTH  / 2f;
        final float worldZ = enemy.getY() / 32f + SPRITE_HEIGHT / 2f;

        // Tilt: the sprite rotates around the X axis (tips backward away from camera).
        // We keep the bottom edge anchored at y=0 by offsetting after rotation.
        // With rotation angle `tilt` around X:
        //   new top Y  = cos(tilt) * SPRITE_HEIGHT
        //   new top Z  = sin(tilt) * SPRITE_HEIGHT  (away from camera, positive Z = toward south)
        final float cosT = FastMath.cos(tilt);
        final float sinT = FastMath.sin(tilt);

        // Bottom-left corner stays at (worldX - W/2, 0, worldZ)
        // Quad origin is bottom-left, so translation = bottom-left world position
        geo.setLocalTranslation(
                worldX - SPRITE_WIDTH / 2f,
                0f,
                worldZ);

        // Rotate: tip the quad backward (negative X-axis rotation so top goes away)
        final Quaternion rot = new Quaternion();
        rot.fromAngles(-tilt, 0f, 0f);
        geo.setLocalRotation(rot);
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