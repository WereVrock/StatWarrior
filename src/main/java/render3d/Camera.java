package render3d;

import entity.Player;

public final class Camera {

    private final int screenWidth;
    private final int screenHeight;
    private final int tileSize;

    private float offsetX;
    private float offsetY;

    private static final float LERP = 0.1f; // smoothing factor

    public Camera(final int screenWidth, final int screenHeight, final int tileSize) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;
    }

    public void update(Player player) {
        float targetX = player.getX() - screenWidth / 2f + tileSize / 2f;
        float targetY = player.getY() - screenHeight / 2f + tileSize / 2f;

        // Smoothly interpolate
        offsetX += (targetX - offsetX) * LERP;
        offsetY += (targetY - offsetY) * LERP;
    }

    public int getOffsetX() { return Math.round(offsetX); }
    public int getOffsetY() { return Math.round(offsetY); }
}