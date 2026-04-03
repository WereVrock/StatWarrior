package render3d;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import dungeon.DungeonCell;
import main.Main;

public final class DungeonRenderer3D {

    private static final float TILE_SIZE   = 1f;
    private static final float WALL_HEIGHT = 6f;
    private static final float WALL_HALF_H = WALL_HEIGHT / 2f;
    private static final float FLOOR_HALF_H = 0.1f;
    private static final float HALF        = 0.5f;
    private static final float WALL_THIN   = 0.05f;

    private static final String FLOOR_TEXTURE = "Textures/floor_brick.png";
    private static final String WALL_TEXTURE  = "Textures/wall_brick.png";

    private DungeonRenderer3D() {}

    public static void renderDungeon() {
        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int rows = grid.length;
        final int cols = grid[0].length;

        final Material floorMat = createTexturedMaterial(FLOOR_TEXTURE);
        final Material wallMat  = createTexturedMaterial(WALL_TEXTURE);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (!grid[y][x].isActive()) continue;

                renderFloor(x, y, floorMat);

                if (y == 0 || !grid[y - 1][x].isActive()) {
                    renderWallFace(x, y, Direction.NORTH, wallMat);
                }
                if (y == rows - 1 || !grid[y + 1][x].isActive()) {
                    renderWallFace(x, y, Direction.SOUTH, wallMat);
                }
                if (x == 0 || !grid[y][x - 1].isActive()) {
                    renderWallFace(x, y, Direction.WEST, wallMat);
                }
                if (x == cols - 1 || !grid[y][x + 1].isActive()) {
                    renderWallFace(x, y, Direction.EAST, wallMat);
                }
            }
        }
    }

    private enum Direction { NORTH, SOUTH, EAST, WEST }

    private static void renderWallFace(int x, int y, Direction dir, Material mat) {
        float cx = x * TILE_SIZE;
        float cy = y * TILE_SIZE;

        float tx, tz, hw, hd;

        switch (dir) {
            case NORTH -> { tx = cx;        tz = cy - HALF; hw = HALF; hd = WALL_THIN; }
            case SOUTH -> { tx = cx;        tz = cy + HALF; hw = HALF; hd = WALL_THIN; }
            case WEST  -> { tx = cx - HALF; tz = cy;        hw = WALL_THIN; hd = HALF; }
            case EAST  -> { tx = cx + HALF; tz = cy;        hw = WALL_THIN; hd = HALF; }
            default    -> throw new IllegalArgumentException("Unknown direction");
        }

        Box box = new Box(hw, WALL_HALF_H, hd);
        Geometry geo = new Geometry("Wall_" + x + "_" + y + "_" + dir, box);
        geo.setLocalTranslation(tx, WALL_HALF_H, tz);
        geo.setMaterial(mat);
        GameApplication.APP.getRootNode().attachChild(geo);
    }

    private static void renderFloor(int x, int y, Material mat) {
        Box box = new Box(HALF, FLOOR_HALF_H, HALF);
        Geometry geo = new Geometry("Floor_" + x + "_" + y, box);
        geo.setLocalTranslation(x * TILE_SIZE, 0f, y * TILE_SIZE);
        geo.setMaterial(mat);
        GameApplication.APP.getRootNode().attachChild(geo);
    }

    private static Material createTexturedMaterial(String texturePath) {
        Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        Texture tex = GameApplication.APP.getAssetManager().loadTexture(texturePath);
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);
        return mat;
    }
}