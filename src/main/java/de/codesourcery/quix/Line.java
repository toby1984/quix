package de.codesourcery.quix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

public class Line
{
    private static final float EPSILON = 0.00001f;

    public Node node0 = new Node();
    public Node node1 = new Node();

    public float m;

    public Line(Node n0, Node n1)
    {
        this.node0 = n0;
        this.node1 = n1;
        updateIntercept();
    }

    public Line shrink()
    {
        final Vec2 a = node0.toVec2();
        final Vec2 b = node1.toVec2();
        final Line result = new Line(node0.copy(),node1.copy());
        if ( result.isHorizontal() ) {
            result.leftNode().x++;
            result.rightNode().x--;
        } else if ( result.isVertical() ) {
            result.topNode().y++;
            result.bottomNode().y--;
        } else {
            Vec2 dirAB = b.copy().subtract( a );
            Vec2 dirBA = a.copy().subtract( b );

            Vec2 newA = new Vec2();
            Vec2 newB =  new Vec2();

            while( true )
            {
                dirAB = dirAB.scl( 0.99f );
                dirBA = dirBA.scl( 0.99f );

                newB.set(a).add( dirAB );
                newA.set(b).add( dirBA );

                int dxa = Math.abs( (int) newA.x - (int) a.x );
                int dya = Math.abs( (int) newA.y - (int) a.y );
                int dxb = Math.abs( (int) newB.x - (int) b.x );
                int dyb = Math.abs( (int) newB.x - (int) b.x );

                if ( dxa >= 1 && dya >= 1 & dxb >= 1 && dyb != 1 ) {
                    break;
                }
            }
            result.node0.set( newA );
            result.node1.set( newB );
        }
        return result;
    }
    public Line(Point p0, Point p1) {
        this(p0.x,p0.y,p1.x,p1.y);
    }

    private Line(Line other) {
        this.node0 = other.node0;
        this.node1 = other.node1;
        this.m = other.m;
    }

    public Node getOther(Node n) {
        if ( n == node0 ) {
            return node1;
        }
        if ( n == node1 ) {
            return node0;
        }
        throw new IllegalArgumentException( "Node "+n+" is no part of line "+this);
    }
    public boolean isEndpoint(int x,int y) {
        return ( this.x0() == x && this.y0() == y ) ||( this.x1() == x && this.y1() == y );
    }

    public boolean isLeftEndpoint(int x,int y) {
        if ( x0() < x1() ) {
            return x0() == x && y0() == y;
        }
        return x1() == x && y1() == y;
    }

    public boolean isRightEndpoint(int x,int y) {
        if ( x0() > x1() ) {
            return x0() == x && y0() == y;
        }
        return x1() == x && y1() == y;
    }

    public boolean isTopEndpoint(int x,int y) {
        if ( y0() < y1() ) {
            return x0() == x && y0() == y;
        }
        return x1() == x && y1() == y;
    }

    public boolean isBottomEndpoint(int x,int y) {
        if ( y0() > y1() ) {
            return x0() == x && y0() == y;
        }
        return x1() == x && y1() == y;
    }

    /**
     * Creates a copy WITHOUT copying the line's nodes.
     *
     * @return
     */
    public Line shallowCopy() {
        return new Line(this);
    }

    public Line(int x0, int y0, int x1, int y1)
    {
        set(x0,y0,x1,y1);
    }

    public Line(int x0, int y0, Node n)
    {
        this( new Node(x0,y0) , n );
    }

    public void updateIntercept()
    {
        if ( ! isVertical() ) // prevent division by zero
        {
            m = ( y1() - y0() ) / (float) ( x1() - x0() );
        } else {
            m = 0;
        }
    }

    public void set(int x0, int y0, int x1, int y1)
    {
        node0.set(x0,y0);
        node1.set(x1,y1);
        updateIntercept();
    }

    public boolean sameStart(Line other) {
        return this.x0() == other.x0() && this.y0() == other.y0();
    }

    public boolean sameEnd(Line other) {
        return this.x1() == other.x1() && this.y1() == other.y1();
    }

    public boolean shareEndpoint(Line other) {
        return (this.hasEndpoint(other.node0) || this.hasEndpoint(other.node1));
    }

    public boolean hasEndpoint(int x,int y) {
        return ( this.x0() == x && this.y0() == y) ||
               ( this.x1() == x && this.y1() == y);
    }

    public boolean hasEndpoints(Node n1, Node n2)
    {
        if ( node0 == n1 ) {
            return node1 == n2;
        }
        if ( node0 == n2 ) {
            return node1 == n1;
        }
        return false;
    }

    public boolean hasEndpoint(Node n)
    {
        if ( n == null ) {
            throw new IllegalArgumentException("Node must not be NULL");
        }
        return this.node0 == n || this.node1 == n;
    }

    @Override
    public String toString()
    {
        String orientation = isVertical() ? "vertical" : isHorizontal() ? "horizontal" : "";
        return orientation+" line ("+ x0() +","+ y0() +") -> ("+ x1() +","+ y1() +"), m = "+m+", node0: "+node0+", node1: "+node1;
    }

    public boolean contains(int cx,int cy)
    {
        if ( isVertical() ) {
            return cx == x0() && cy >= minY() && cy <= maxY();
        } else if ( isHorizontal() ) {
            return cy == y0() && cx >= minX() && cx <= maxX();
        }

        final int ax = this.x0();
        final int ay = this.y0();

        final int bx = this.x1();
        final int by = this.y1();

        final float AB = (float) sqrt(( 0 -ay)*( 0 -ay)+( 0 -by)*( 0 -by)+( 0 -cy)*( 0 -cy));
        final float AP = (float) sqrt((ax-ay)*(ax-ay)+(bx-by)*(bx-by)+(cx-cy)*(cx-cy));
        final float PB = (float) sqrt(( 0 -ax)*( 0 -ax)+( 0 -bx)*( 0 -bx)+( 0 -cx)*( 0 -cx));
        return Math.abs( AB - AP + PB ) < EPSILON;
    }

    public int minX() {
        return min( x0(), x1() );
    }

    public int maxX() {
        return max( x0(), x1() );
    }

    public int minY() {
        return min( y0(), y1() );
    }

    public int maxY() {
        return max( y0(), y1() );
    }

    public void setLeftNode(Node node) {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call setLeftNode() on non-horizontal line " + this );
        }
        if ( x0() <= x1() ) {
            this.node0 = node;
        } else {
            this.node1 = node;
        }
        node.setRight(this);
    }

    public void setRightNode(Node node) {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call setRightNode() on non-horizontal line " + this );
        }
        if ( x0() <= x1() )
        {
            this.node1 = node;
        }
        else
        {
            this.node0 = node;
        }
        node.setLeft(this);
    }

    public void setTopNode(Node node) {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call setTopNode() on non-vertical line " + this );
        }
        if ( y0() <= y1() )
        {
            this.node0 = node;
        }
        else
        {
            this.node1 = node;
        }
        node.setDown(this);
    }

    public void setBottomNode(Node node)
    {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call setBottomNode() on non-vertical line " + this );
        }
        if ( y0() <= y1() )
        {
            this.node1 = node;
        }
        else
        {
            this.node0 = node;
        }
        node.setUp(this);
    }

    public Node leftNode()
    {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call leftNode() on non-horizontal line " + this );
        }
        return x0() < x1() ? node0 : node1;
    }

    public Node rightNode()
    {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call rightNode() on non-horizontal line " + this );
        }
        return x0() < x1() ? node1 : node0;
    }

    public Node topNode()
    {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call topNode() on non-vertical line " + this );
        }
        return y0() < y1() ? node0 : node1;
    }

    public Node bottomNode()
    {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call bottomNode() on non-vertical line " + this );
        }
        return y0() < y1() ? node1 : node0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ( obj instanceof Line)
        {
            final Line o = (Line) obj;
            return obj == this || this.x0() == o.x0() && this.y0() == o.y0() && this.x1() == o.x1() && this.y0() == o.y1();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( x0(), y0(), x1(), y1(), m );
    }

    public boolean intersects(Line other)
    {
        int thisX0 = this.x0();
        int thisY0 = this.y0();
        int thisX1 = this.x1();
        int thisY1 = this.y1();

        int otherX0 = other.x0();
        int otherY0 = other.y0();
        int otherX1 = other.x1();
        int otherY1 = other.y1();

        final boolean thisVertical = isVertical();
        final boolean otherVertical = other.isVertical();
        if ( thisVertical || otherVertical )
        {
            // these need special treatment to prevent division by zero
            if ( thisVertical == otherVertical ) { // both are vertical, check overlap on
                if ( thisX0 != otherX0 ) // cannot possibly overlap
                {
                    return false;
                }
                // check overlapping Y interval
                return max( thisY0, thisY1 ) >= min( otherY0, otherY1 );
            }
            // only one of the lines is vertical while the other is not
            final Line vertLine;
            final Line nonVertLine;
            if ( thisVertical ) {
                vertLine = this;
                nonVertLine = other;
            } else {
                vertLine = other;
                nonVertLine = this;
            }
            if ( min( nonVertLine.x0(), nonVertLine.x1() ) <= vertLine.x0() && max( nonVertLine.x0(), nonVertLine.x1() ) >= vertLine.x0() ) {
                return min( nonVertLine.y0(), nonVertLine.y1() ) >= min( vertLine.y0(), vertLine.y1() ) &&
                max( nonVertLine.y0(), nonVertLine.y1() ) <= max( vertLine.y0(), vertLine.y1() );
            }
            return false;
        }

        /*
         *
Segment1 = {(X1, Y1), (X2, Y2)}
Segment2 = {(X3, Y3), (X4, Y4)}
The X coordinate Xa of the potential point of intersection (Xa,Ya) must be contained in both interval I1 and I2, defined as follow :

I1 = [min(X1,X2), max(X1,X2)] // [min(this.x0,this.x1),max(this.x0,this.x1)]
I2 = [min(X3,X4), max(X3,X4)] // [min(other.x0,other.x1),max(other.x0,other.x1)]

* And we could say that Xa is included into interval Ia like so:

Ia = [max( min(X1,X2), min(X3,X4) ), min( max(X1,X2), max(X3,X4) )]
*
Now, we need to check that this interval Ia exists :
*/

        if ( max( thisX0, thisX1 ) < min( otherX0, otherX1 ) )
        {
            return false; // There is no mutual common X coordinate
        }
/*
So, we have two line formula, and a mutual interval. Your line formulas are:

f1(x) = A1*x + b1 = y
f2(x) = A2*x + b2 = y
As we got two points by segment, we are able to determine A1, A2, b1 and b2:
*/
        float slope1 = this.m;
        float slope2 = other.m;
        float b1 = thisY0 - slope1 * thisX0;
        float b2 = otherY0 - slope2 * otherX0;
/*
If the segments are parallel, then A1 == A2 :
*/
        if ( Math.abs( slope1 - slope2 ) < EPSILON )
        {
            // parallel, horizontal lines
            if ( this.y0() == other.y0() )
            {
                // check for overlapping X interval
                return max( thisX0, thisX1 ) >= min( otherX0, otherX1 );
            }
            return false;
        }
/*
A point (Xa,Ya) standing on both line must verify both formulas f1 and f2:
*/
        float Xa = ( b2 - b1 ) / ( slope1 - slope2 ); // Once again, pay attention to not dividing by zero
/*
The last thing to do is check that Xa is included into Ia:
*/
        if ( ( Xa < max( min( thisX0, thisX1 ), min( otherX0, otherX1 ) ) ) || ( Xa > min( max( thisX0, thisX1 ), max( otherX0, otherX1 ) ) ) )
        {
            return false; // intersection is out of bound
        }
        return true;
    }

    public boolean isAxisParallel()
    {
        return isHorizontal() || isVertical();
    }

    public boolean isHorizontal() {
        return y0() == y1();
    }

    public boolean isVertical() {
        return x0() == x1();
    }

    public void draw(Graphics2D gfx)
    {
        gfx.drawLine( x0(), y0(), x1(), y1() );

        final int radius = 12;
        if ( isAxisParallel() )
        {
            final Color current = gfx.getColor();
            final Node left = isHorizontal() ? leftNode() : topNode();
            if ( left != null )
            {
                gfx.setXORMode( Color.BLUE );
                gfx.drawArc( left.x - radius / 2, left.y - radius / 2, radius, radius, 0, 360 );
            }

            final Node right = isHorizontal() ? rightNode() : bottomNode() ;
            if ( right != null )
            {
                gfx.setXORMode( Color.MAGENTA );
                gfx.drawArc( right.x - radius / 2, right.y - radius / 2, radius, radius, 0, 360 );
            }
            gfx.setPaintMode();
            gfx.setColor(current);
        }
    }

    public Node getNodeForEndpoint(int x,int y) {
        if ( this.x0() == x & this.y0() == y) {
            return node0;
        }
        if ( this.x1() == x && this.y1() == y) {
            return node1;
        }
        return null;
    }

    public void assertValid() {

        if ( node0 != null ) {
            if ( node0.x != x0() || node0.y != y0() ) {
                throw new IllegalStateException("Line has broken node0: "+this);
            }
        }
        if ( node1 != null ) {
            if ( node1.x != x1() || node1.y != y1() ) {
                throw new IllegalStateException("Line has broken node1: "+this);
            }
        }
    }

    public int x0()
    {
        return node0.x;
    }

    public void setX0(int x) {
        node0.x = x;
    }

    public int y0()
    {
        return node0.y;
    }

    public void setY0(int y) {
        node0.y = y;
    }

    public int x1()
    {
        return node1.x;
    }

    public void setX1(int x) {
        node1.x = x;
    }

    public int y1()
    {
        return node1.y;
    }

    public void setY1(int y) {
        node1.y = y;
    }
}