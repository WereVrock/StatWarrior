package entity.enemy;

import entity.AttackType;
import entity.ProjectileManager;
import entity.enemy.Enemy;
import java.util.List;

public final class EnemySpawner {

    private static final int TILE_SIZE = 32;

    private EnemySpawner() {}

    public static List<Enemy> spawn(final List<int[]> walkableTiles,
                                    final ProjectileManager projectileManager) {
        return List.of(
                build(walkableTiles, 0, "Enemy-Melee",   projectileManager, AttackType.MELEE),
                build(walkableTiles, 1, "Enemy-Ranged",  projectileManager, AttackType.RANGED),
                build(walkableTiles, 2, "Enemy-Charge",  projectileManager, AttackType.CHARGE),
                build(walkableTiles, 3, "Enemy-All",     projectileManager,
                        AttackType.MELEE, AttackType.RANGED, AttackType.CHARGE)
        );
    }

    private static Enemy build(final List<int[]> walkableTiles, final int tileIndex,
                                final String id, final ProjectileManager projectileManager,
                                final AttackType... types) {
        final int[] tile = walkableTiles.get(tileIndex);
        return new Enemy(tile[0], tile[1], TILE_SIZE, id, projectileManager,
                List.of(types));
    }
}