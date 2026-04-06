// ===== render3d/DungeonRenderer3D.java =====
package render3d;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import dungeon.DungeonCell;
import main.Main;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public final class DungeonRenderer3D {

    private static final float TILE_SIZE    = 1f;
    private static final float WALL_HEIGHT  = 6f;
    private static final float FLOOR_HALF_H = 0.1f;
    private static final float HALF         = 0.5f;

    private static final float WALL_TEX_SCALE  = 1.0f;
    private static final float FLOOR_TEX_SCALE = 1.0f;

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

                if (y == 0 || !grid[y - 1][x].isActive())
                    renderWallFace(x, y, Direction.NORTH, wallMat);
                if (y == rows - 1 || !grid[y + 1][x].isActive())
                    renderWallFace(x, y, Direction.SOUTH, wallMat);
                if (x == 0 || !grid[y][x - 1].isActive())
                    renderWallFace(x, y, Direction.WEST, wallMat);
                if (x == cols - 1 || !grid[y][x + 1].isActive())
                    renderWallFace(x, y, Direction.EAST, wallMat);
            }
        }
    }

    private enum Direction { NORTH, SOUTH, EAST, WEST }

    /**
     * Each wall face is a single quad placed exactly at the tile boundary.
     * No inset — faces share the tile edge exactly so corners have zero gap.
     *
     * NORTH: face at z = tileCenter - HALF  (north edge of tile)
     * SOUTH: face at z = tileCenter + HALF
     * WEST:  face at x = tileCenter - HALF
     * EAST:  face at x = tileCenter + HALF
     *
     * The quad spans 1 tile wide and WALL_HEIGHT tall.
     * right vector determines which world axis the width runs along.
     */
    private static void renderWallFace(final int x, final int y,
                                       final Direction dir,
                                       final Material mat) {
        final float cx = x * TILE_SIZE;
        final float cy = y * TILE_SIZE;

        // Face center X/Z and the right-axis (width direction in world space)
        final float faceCX, faceCZ;
        final float rightX, rightZ;

        switch (dir) {
            case NORTH -> { faceCX = cx;        faceCZ = cy - HALF; rightX =  1f; rightZ =  0f; }
            case SOUTH -> { faceCX = cx;        faceCZ = cy + HALF; rightX = -1f; rightZ =  0f; }
            case WEST  -> { faceCX = cx - HALF; faceCZ = cy;        rightX =  0f; rightZ = -1f; }
            case EAST  -> { faceCX = cx + HALF; faceCZ = cy;        rightX =  0f; rightZ =  1f; }
            default -> throw new IllegalArgumentException("Unknown direction");
        }

        final float halfH  = WALL_HEIGHT / 2f;
        final float uMax   = TILE_SIZE   * WALL_TEX_SCALE;
        final float vMax   = WALL_HEIGHT * WALL_TEX_SCALE;

        // 4 verts: bottom-left, bottom-right, top-right, top-left
        final float[] pos = {
            faceCX - rightX * HALF,  0f,        faceCZ - rightZ * HALF,
            faceCX + rightX * HALF,  0f,        faceCZ + rightZ * HALF,
            faceCX + rightX * HALF,  WALL_HEIGHT, faceCZ + rightZ * HALF,
            faceCX - rightX * HALF,  WALL_HEIGHT, faceCZ - rightZ * HALF,
        };

        final float[] uvs = {
            0f,    0f,
            uMax,  0f,
            uMax,  vMax,
            0f,    vMax,
        };

        // Normal faces inward toward the dungeon (toward the open side)
        final float nX = -rightZ;
        final float nZ =  rightX;
        final float[] norms = {
            nX, 0f, nZ,
            nX, 0f, nZ,
            nX, 0f, nZ,
            nX, 0f, nZ,
        };

        final short[] idx = { 0, 1, 2, 0, 2, 3 };

        final Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(pos));
        mesh.setBuffer(VertexBuffer.Type.Normal,   3, BufferUtils.createFloatBuffer(norms));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(uvs));
        mesh.setBuffer(VertexBuffer.Type.Index,    3, BufferUtils.createShortBuffer(idx));
        mesh.updateBound();

        final Geometry geo = new Geometry("Wall_" + x + "_" + y + "_" + dir, mesh);
        geo.setMaterial(mat);
        GameApplication.APP.getRootNode().attachChild(geo);
    }

    private static void renderFloor(final int x, final int y, final Material mat) {
        final float cx  = x * TILE_SIZE;
        final float cy  = y * TILE_SIZE;
        final float top = FLOOR_HALF_H * 2f;
        final float u   = FLOOR_TEX_SCALE;

        final float[] pos = {
            cx - HALF, top, cy - HALF,
            cx + HALF, top, cy - HALF,
            cx + HALF, top, cy + HALF,
            cx - HALF, top, cy + HALF,
        };
        final float[] uvs   = { 0f, 0f,  u, 0f,  u, u,  0f, u };
        final float[] norms = {
            0f, 1f, 0f,  0f, 1f, 0f,
            0f, 1f, 0f,  0f, 1f, 0f,
        };
        final short[] idx = { 0, 1, 2, 0, 2, 3 };

        final Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(pos));
        mesh.setBuffer(VertexBuffer.Type.Normal,   3, BufferUtils.createFloatBuffer(norms));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(uvs));
        mesh.setBuffer(VertexBuffer.Type.Index,    3, BufferUtils.createShortBuffer(idx));
        mesh.updateBound();

        final Geometry geo = new Geometry("Floor_" + x + "_" + y, mesh);
        geo.setMaterial(mat);
        GameApplication.APP.getRootNode().attachChild(geo);
    }

    private static Material createTexturedMaterial(final String texturePath) {
        final Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        final Texture tex = GameApplication.APP.getAssetManager().loadTexture(texturePath);
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);
        return mat;
    }
}