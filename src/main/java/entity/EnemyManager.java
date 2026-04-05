package entity;

import dungeon.DungeonCell;
import main.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class EnemyManager {

    private static final int TILE_SIZE   = 32;
    private static final int ENEMY_COUNT = 2;

    private final List<Enemy>       enemies;
    private final ProjectileManager projectileManager;

    public EnemyManager() {
        projectileManager = new ProjectileManager();
        enemies           = spawnEnemies();
    }

    private List<Enemy> spawnEnemies() {
        final List<int[]>  walkable = collectWalkableTiles();
        final AttackType[] types    = AttackType.values();
        Collections.shuffle(walkable, new Random());

        final List<Enemy> result = new ArrayList<>();
        for (int i = 0; i < ENEMY_COUNT && i < walkable.size(); i++) {
            final int[]      tile       = walkable.get(i);
            final AttackType attackType = types[i % types.length];

            result.add(new Enemy(
                    tile[0],
                    tile[1],
                    TILE_SIZE,
                    "Enemy-" + (i + 1),
                    attackType,
                    projectileManager
            ));
        }
        return result;
    }

    private static List<int[]> collectWalkableTiles() {
        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final List<int[]> walkable = new ArrayList<>();

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x].isActive()) {
                    walkable.add(new int[]{x, y});
                }
            }
        }
        return walkable;
    }

    public void update(final float tpf) {
        projectileManager.update(tpf);

        for (final Enemy enemy : enemies) {
            enemy.update(tpf);
        }
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}