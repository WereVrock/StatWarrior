// ===== render3d/EnemyRenderer3D.java =====
package render3d;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import entity.AttackType;
import entity.enemy.Enemy;
import entity.enemy.EnemyManager;
import main.Main;

import java.util.ArrayList;
import java.util.List;

public final class EnemyRenderer3D {

    private static final float  SPRITE_WIDTH      = 1.0f;
    private static final float  SPRITE_HEIGHT     = 1.0f;
    private static final String TEXTURE_MELEE     = "Textures/sprites/enemy_melee.png";
    private static final String TEXTURE_RANGED    = "Textures/sprites/enemy_ranged.png";
    private static final String TEXTURE_CHARGE    = "Textures/sprites/enemy_charge.png";
    private static final String TEXTURE_ALL       = "Textures/sprites/enemy_all.png";

    /** Max backward tilt in radians when the enemy dies (falls onto its back). */
    private static final float DEATH_TILT_MAX_ANGLE = FastMath.HALF_PI; // 90°

    /**
     * How far away from the player the enemy lands when it dies, in world units.
     * The sprite's fall pivot is shifted away from the player by this amount.
     */
    private static final float DEATH_JUMP_DISTANCE = 5.55f;

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
                geo.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                continue;
            }

            geo.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);

            if (enemy.getHealth().isDead()) {
                positionDeadSprite(geo, enemy);
            } else {
                BillboardRenderer3D.faceCamera(geo, enemy.getX(), enemy.getY(),
                        enemy.getBobOffset());
                final Vector3f pos = geo.getLocalTranslation().clone();
                pos.x += enemy.getShakeOffset();
                geo.setLocalTranslation(pos);
            }
        }
    }

    /**
     * Rotates the sprite around its bottom edge so it falls onto its back,
     * shifted away from the player so it appears to jump/fly backward on death.
     */
    private static void positionDeadSprite(final Geometry geo, final Enemy enemy) {
        final float tilt = enemy.getHealth().getDeathTiltFraction() * DEATH_TILT_MAX_ANGLE;

        // Enemy world center
        final float enemyWorldX = enemy.getX() / dungeon.Dungeon.TILE_SIZE + SPRITE_WIDTH  / 2f;
        final float enemyWorldZ = enemy.getY() / dungeon.Dungeon.TILE_SIZE + SPRITE_HEIGHT / 2f;

        // Player world center
        final float playerWorldX = Main.PLAYER.centerX() / 32f;
        final float playerWorldZ = Main.PLAYER.centerY() / 32f;

        // Direction from player to enemy (normalized, horizontal)
        final float toEnemyX = enemyWorldX - playerWorldX;
        final float toEnemyZ = enemyWorldZ - playerWorldZ;
        final float toEnemyLen = (float) Math.sqrt(toEnemyX * toEnemyX + toEnemyZ * toEnemyZ);
        final float normAwayX = toEnemyLen > 0.001f ? toEnemyX / toEnemyLen : 0f;
        final float normAwayZ = toEnemyLen > 0.001f ? toEnemyZ / toEnemyLen : 1f;

        // Shift the fall pivot away from the player, scaled by tilt progress
        // so the enemy slides/jumps away as it falls
        final float progress  = enemy.getHealth().getDeathTiltFraction();
        final float offsetX   = normAwayX * DEATH_JUMP_DISTANCE * progress;
        final float offsetZ   = normAwayZ * DEATH_JUMP_DISTANCE * progress;

        final float pivotX = enemyWorldX - SPRITE_WIDTH / 2f + offsetX;
        final float pivotZ = enemyWorldZ + offsetZ;

        geo.setLocalTranslation(pivotX, 0f, pivotZ);

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