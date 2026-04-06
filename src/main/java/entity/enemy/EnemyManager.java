package entity.enemy;

import entity.enemy.Enemy;
import dungeon.DungeonCell;
import entity.ProjectileManager;
import main.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class EnemyManager {

    private final List<Enemy>       enemies;
    private final ProjectileManager projectileManager;

    public EnemyManager() {
        projectileManager = new ProjectileManager();
        enemies           = spawnEnemies();
    }

    private List<Enemy> spawnEnemies() {
        final List<int[]> walkable = collectWalkableTiles();
        Collections.shuffle(walkable, new Random());
        return EnemySpawner.spawn(walkable, projectileManager);
    }

    private static List<int[]> collectWalkableTiles() {
        final DungeonCell[][] grid    = Main.DUNGEON.getGrid();
        final List<int[]>     walkable = new ArrayList<>();
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x].isActive()) walkable.add(new int[]{x, y});
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

    public ProjectileManager getProjectileManager() { return projectileManager; }
    public List<Enemy>       getEnemies()           { return enemies; }
}