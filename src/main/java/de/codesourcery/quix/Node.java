package de.codesourcery.quix;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Node
{
    private static long ID = 0;

    public final long id=ID++;
    public int x,y;

    public Line up;
    public Line left;
    public Line right;
    public Line down;

    public Node() {
    }

    public Node(int x, int y)
    {
        set(x,y);
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
        return "Node[ "+id+" ] = {"+directions+"}";
    }
}