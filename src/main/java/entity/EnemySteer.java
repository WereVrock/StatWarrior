package entity;

import balance.Balance;
import dungeon.DungeonCell;
import main.Main;

public final class EnemySteer {

    private static final int   SCAN_RADIUS        = 1;
    private static final float REPULSION_MIN_DIST = 0.8f; // fraction of tileSize — only repel when this close

    private final EnemyBody body;

    public EnemySteer(final EnemyBody body) {
        this.body = body;
    }

    public void seekTarget(final float targetWorldX, final float targetWorldY, final float speed) {
        final float dx   = targetWorldX - body.centerX();
        final float dy   = targetWorldY - body.centerY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 1f) return;

        body.accelerate(
                (dx / dist) * Balance.ENEMY_ACCELERATION,
                (dy / dist) * Balance.ENEMY_ACCELERATION
        );
        body.clampSpeed(speed);
    }

    public void applyWallRepulsion() {
        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int gridWidth  = grid[0].length;
        final int gridHeight = grid.length;

        final int cx = body.toTileX(body.centerX());
        final int cy = body.toTileY(body.centerY());

        final float threshold = body.getTileSize() * REPULSION_MIN_DIST;

        float repX = 0f;
        float repY = 0f;

        for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
            for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                if (dx == 0 && dy == 0) continue;

                final int tx = cx + dx;
                final int ty = cy + dy;

                if (tx < 0 || tx >= gridWidth || ty < 0 || ty >= gridHeight) continue;
                if (grid[ty][tx].isActive()) continue;

                final float wallWorldX = tx * body.getTileSize() + body.getTileSize() / 2f;
                final float wallWorldY = ty * body.getTileSize() + body.getTileSize() / 2f;

                final float toX  = body.centerX() - wallWorldX;
                final float toY  = body.centerY() - wallWorldY;
                final float dist = (float) Math.sqrt(toX * toX + toY * toY);

                if (dist > threshold || dist < 1f) continue;

                // Linear falloff — strong when touching, zero at threshold
                final float strength = Balance.ENEMY_WALL_REPULSION * (1f - dist / threshold);
                repX += (toX / dist) * strength;
                repY += (toY / dist) * strength;
            }
        }

        // Normalize repulsion so it can redirect seek but never overpower it alone
        final float repLen = (float) Math.sqrt(repX * repX + repY * repY);
        if (repLen > Balance.ENEMY_ACCELERATION) {
            repX = (repX / repLen) * Balance.ENEMY_ACCELERATION;
            repY = (repY / repLen) * Balance.ENEMY_ACCELERATION;
        }

        body.accelerate(repX, repY);
    }
}