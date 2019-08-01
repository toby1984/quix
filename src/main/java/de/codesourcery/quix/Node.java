package de.codesourcery.quix;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Node
{
    private static int ID = 0;

    public final int id=ID++;
    public int x,y;

    public Line up;
    public Line left;
    public Line right;
    public Line down;

    public static Int2ObjectMap<Node> ALL_NODES =
        new Int2ObjectArrayMap<>();

    public Node() {
        ALL_NODES.put(id,this);
    }

    public static Node of(int x,int y) {
        return new Node(x,y);
    }

    public Vec2 toVec2() {
        return new Vec2(this);
    }

    public static Node get(int nodeId) {
        return ALL_NODES.get( nodeId );
    }

    public Node(Node other) {
        this();
        this.x = other.x;
        this.y = other.y;
    }

    public Node copy() {
        return new Node(this);
    }

    public boolean matches(Node other) {
        return this.x == other.x && this.y == other.y;
    }

    public Node(int x, int y)
    {
        this();
        set(x,y);
    }

    public void set(Vec2 v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
    }

    public Line getExit(Direction dir,Direction ignoredDirection)
    {
        switch(dir) {
            case LEFT: return Direction.LEFT != ignoredDirection ? left : null;
            case RIGHT: return Direction.RIGHT != ignoredDirection ? right : null;
            case UP: return Direction.UP != ignoredDirection ? up: null;
            case DOWN: return Direction.DOWN != ignoredDirection ? down : null;
            default:
                throw new RuntimeException( "Unexpected value: " + dir );
        }
    }

    public int getExitCount(Direction ignored)
    {
        int result = 0;
        if ( up != null && Direction.UP != ignored ) { result++; }
        if ( down != null && Direction.DOWN != ignored ) { result++; }
        if ( left != null && Direction.LEFT != ignored ) { result++; }
        if ( right != null && Direction.RIGHT != ignored ) { result++; }
        return result;
    }

    public Node add(Direction dir) {
        this.x += dir.dx;
        this.y += dir.dy;
        return this;
    }

    public Node add(Node other) {
        this.x+=other.x;
        this.y+=other.y;
        return this;
    }

    public float dst(int x,int y) {
        return (float) Math.sqrt( dst2(x,y) );
    }

    public int dst2(int x,int y)
    {
        int dx = this.x - x;
        int dy = this.y - y;
        return dx*dx + dy*dy;
    }


    public void set(int x,int y) {
        this.x = x;
        this.y = y;
    }

    public void setLeft(Line line) {
        if ( ! line.hasEndpoint( x,y ) ) {
            throw new IllegalArgumentException( "Line "+line+" does not connect to "+this );
        }
        if ( ! line.isHorizontal() ) {
            throw new IllegalArgumentException( "Line "+line+" needs to be horizontal but isn't");
        }
        left = line;
    }

    public void setRight(Line line) {
        if ( ! line.hasEndpoint( x,y ) ) {
            throw new IllegalArgumentException( "Line "+line+" does not connect to "+this );
        }
        if ( ! line.isHorizontal() ) {
            throw new IllegalArgumentException( "Line "+line+"must be horizontal" );
        }
        right = line;
    }

    public void setUp(Line line) {
        if ( ! line.hasEndpoint( x,y ) ) {
            throw new IllegalArgumentException( "Line "+line+" does not connect to "+this );
        }
        if ( ! line.isVertical() ) {
            throw new IllegalArgumentException( "Line "+line+" must be vertical" );
        }
        up = line;
    }

    public void visitDirections(BiConsumer<Direction,Line> visitor)
    {
        if ( left != null ) {
            visitor.accept(Direction.LEFT, left );
        }
        if ( right != null ) {
            visitor.accept(Direction.RIGHT, right );
        }
        if ( up != null ) {
            visitor.accept(Direction.UP, up );
        }
        if ( down  != null ) {
            visitor.accept(Direction.DOWN, down );
        }
    }

    public void setDown(Line line) {
        if ( ! line.hasEndpoint( x,y ) ) {
            throw new IllegalArgumentException( "Line "+line+" does not connect to "+this );
        }
        if ( ! line.isVertical() ) {
            throw new IllegalArgumentException( "Line "+line+" must be vertical" );
        }
        down = line;
    }

    @Override
    public String toString()
    {
        final boolean hasLeft = left != null;
        final boolean hasRight = right != null;
        final boolean hasUp = up != null;
        final boolean hasDown = down != null;
        final String directions = List.of( hasLeft?"LEFT":"", hasRight?"RIGHT":"",
                hasUp?"UP":"", hasDown?"DOWN":"").stream().filter( x -> x.length()>0 ).collect( Collectors.joining(","));
        return "Node[ "+id+" , ("+x+","+y+") ] = {"+directions+"}";
    }

    public float angleInDegrees(Node other)
    {
        int dx = other.x - this.x;
        int dy = other.y - this.y;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        return angle < 0 ? angle + 360 : angle;
    }

      public Node divideBy(int value) {
        x /= value;
        y /= value;
        return this;
    }
}