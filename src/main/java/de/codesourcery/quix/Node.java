package de.codesourcery.quix;

public class Node
{
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
        return "Node{" +
                   "x=" + x +
                   ", y=" + y +
                   ", up=" + up +
                   ", left=" + left +
                   ", right=" + right +
                   ", down=" + down +
                   '}';
    }
}