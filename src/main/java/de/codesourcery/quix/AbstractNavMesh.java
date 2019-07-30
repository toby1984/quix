package de.codesourcery.quix;

public abstract class AbstractNavMesh implements NavMesh
{
    @Override
    public Vec2 getCoordinates(int nodeId)
    {
        final Node n = Node.get( nodeId );
        return new Vec2(n.x, n.y);
    }

    private int dst2(int nodeStart,int nodeEnd)
    {
        final Node a = Node.get(nodeStart);
        final Node b = Node.get(nodeEnd);
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        return dx * dx + dy * dy;
    }

    @Override
    public int calcH(int nodeStart, int nodeEnd)
    {
        return dst2(nodeStart, nodeEnd );
    }

    @Override
    public int calcG(int nodeA, int nodeB)
    {
        return dst2(nodeA, nodeB );
    }
}
