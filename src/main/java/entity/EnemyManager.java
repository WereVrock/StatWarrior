package entity;

import dungeon.DungeonCell;
import main.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class EnemyManager {

    private static final int TILE_SIZE    = 32;
    private static final int ENEMY_COUNT  = 2;

    private final List<Enemy> enemies;

    public EnemyManager() {
        enemies = spawnEnemies();
    }

    private static List<Enemy> spawnEnemies() {
        final List<int[]> walkable = collectWalkableTiles();
        Collections.shuffle(walkable, new Random());

        final List<Enemy> result = new ArrayList<>();
        for (int i = 0; i < ENEMY_COUNT && i < walkable.size(); i++) {
            final int[] tile = walkable.get(i);
            result.add(new Enemy(tile[0], tile[1], TILE_SIZE));
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
        for (final Enemy enemy : enemies) {
            enemy.update(tpf);
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}