package pathfinding;

public final class Node {

    public final int x, y;
    public final Node parent;
    public final float g, h;

    public Node(final int x, final int y, final Node parent, final float g, final float h) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.g = g;
        this.h = h;
    }

    public float f() {
        return g + h;
    }
}