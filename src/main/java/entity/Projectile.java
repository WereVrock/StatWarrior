package entity;

import main.Main;

public final class Projectile {

    private float x, y;
    private final float vx, vy;
    private float lifetime;
    private boolean dead;

    private static final int TILE_SIZE = 32;

    public Projectile(final float startX, final float startY,
                      final float dirX,   final float dirY,
                      final float speed,  final float lifetime) {
        this.x        = startX;
        this.y        = startY;
        this.vx       = dirX * speed;
        this.vy       = dirY * speed;
        this.lifetime = lifetime;
        this.dead     = false;
    }

    public void update(final float tpf) {
        if (dead) return;

        lifetime -= tpf;
        if (lifetime <= 0f) {
            dead = true;
            return;
        }

        x += vx * tpf * TILE_SIZE;
        y += vy * tpf * TILE_SIZE;

        if (hitsWall()) {
            dead = true;
        }
    }

    public boolean hitsPlayer() {
        if (dead) return false;

        final float px   = Main.PLAYER.getX() + TILE_SIZE / 2f;
        final float py   = Main.PLAYER.getY() + TILE_SIZE / 2f;
        final float dx   = px - x;
        final float dy   = py - y;
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        return dist < TILE_SIZE * 0.6f;
    }

    private boolean hitsWall() {
        final int tx = (int)(x / TILE_SIZE);
        final int ty = (int)(y / TILE_SIZE);

        final var grid = Main.DUNGEON.getGrid();
        if (tx < 0 || ty < 0 || tx >= grid[0].length || ty >= grid.length) return true;
        return !grid[ty][tx].isActive();
    }

    public void kill()         { dead = true; }
    public boolean isDead()    { return dead; }
    public float getX()        { return x; }
    public float getY()        { return y; }
}