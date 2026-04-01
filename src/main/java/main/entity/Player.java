package entity;

import dungeon.DungeonCell;
import main.Main;

import java.awt.Graphics;
import java.awt.Color;

public final class Player {

    private static final Color COLOR = Color.RED;

    private int x; // grid x
    private int y; // grid y

    public Player(final int startX, final int startY) {
        this.x = startX;
        this.y = startY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(final int dx, final int dy) {
        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int newX = x + dx;
        final int newY = y + dy;

        if (newY >= 0 && newY < grid.length &&
            newX >= 0 && newX < grid[0].length &&
            grid[newY][newX].isActive()) {
            x = newX;
            y = newY;
        }
    }

    public void render(final Graphics g, final int tileSize, final int offsetX, final int offsetY) {
        g.setColor(COLOR);
        g.fillOval(
                x * tileSize - offsetX,
                y * tileSize - offsetY,
                tileSize,
                tileSize
        );
    }
}