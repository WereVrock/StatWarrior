package ui;

import entity.Player;

public final class Camera {

    private final int screenWidth;
    private final int screenHeight;
    private final int tileSize;

    private int offsetX;
    private int offsetY;

    public Camera(final int screenWidth, final int screenHeight, final int tileSize) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    public void update(final Player player) {
        final int playerPixelX = player.getX() * tileSize;
        final int playerPixelY = player.getY() * tileSize;

        offsetX = playerPixelX - screenWidth / 2 + tileSize / 2;
        offsetY = playerPixelY - screenHeight / 2 + tileSize / 2;

        if (offsetX < 0) offsetX = 0;
        if (offsetY < 0) offsetY = 0;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
}