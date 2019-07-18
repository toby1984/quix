package de.codesourcery.quix;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

public class Line
{
    private static final float EPSILON = 0.00001f;

    public Node node0;
    public int x0,y0;

    public Node node1;
    public int x1,y1;

    public float m;

    public Line(Point p0, Point p1) {
        this(p0.x,p0.y,p1.x,p1.y);
    }

    private Line(Line other) {
        this.node0 = other.node0;
        this.node1 = other.node1;
        this.x0 = other.x0;
        this.y0 = other.y0;
        this.x1 = other.x1;
        this.y1 = other.y1;
        this.m = other.m;
    }

    public boolean isEndpoint(int x,int y) {
        return (this.x0 == x && this.y0 == y ) ||(this.x1 == x && this.y1 == y );
    }

    public boolean isLeftEndpoint(int x,int y) {
        if ( x0 < x1 ) {
            return x0 == x && y0 == y;
        }
        return x1 == x && y1 == y;
    }

    public boolean isRightEndpoint(int x,int y) {
        if ( x0 > x1 ) {
            return x0 == x && y0 == y;
        }
        return x1 == x && y1 == y;
    }

    public boolean isTopEndpoint(int x,int y) {
        if ( y0 < y1 ) {
            return x0 == x && y0 == y;
        }
        return x1 == x && y1 == y;
    }

    public boolean isBottomEndpoint(int x,int y) {
        if ( y0 > y1 ) {
            return x0 == x && y0 == y;
        }
        return x1 == x && y1 == y;
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

    protected void updateIntercept()
    {
        if ( ! isVertical() ) // prevent division by zero
        {
            m = ( y1 - y0 ) / (float) ( x1 - x0 );
        } else {
            m = 0;
        }
    }

    public void setStart(int x0,int y0) {
        this.x0 = x0;
        this.y0 = y0;
        updateIntercept();
    }

    public void setEnd(int x1,int y1) {
        this.x1 = x1;
        this.y1 = y1;
        updateIntercept();
    }

    public void set(int x0, int y0, int x1, int y1)
    {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;

        updateIntercept();
    }

    public boolean sameStart(Line other) {
        return this.x0 == other.x0 && this.y0 == other.y0;
    }

    public boolean sameEnd(Line other) {
        return this.x1 == other.x1 && this.y1 == other.y1;
    }

    public boolean shareEndpoint(Line other) {
        return sameStart( other ) || sameEnd( other );
    }

    public boolean hasEndpoint(int x,int y) {
        return (this.x0 == x && this.y0 == y) ||
               (this.x1 == x && this.y1 == y);
    }

    @Override
    public String toString()
    {
        String orientation = isVertical() ? "vertical" : isHorizontal() ? "horizontal" : "";
        return orientation+" line ("+x0+","+y0+") -> ("+x1+","+y1+"), m = "+m;
    }

    public boolean contains(int cx,int cy)
    {
        if ( isVertical() ) {
            return cx == x0 && cy >= minY() && cy <= maxY();
        } else if ( isHorizontal() ) {
            return cy == y0 && cx >= minX() && cx <= maxX();
        }

        final int ax = this.x0;
        final int ay = this.y0;

        final int bx = this.x1;
        final int by = this.y1;

        final float AB = (float) sqrt(( 0 -ay)*( 0 -ay)+( 0 -by)*( 0 -by)+( 0 -cy)*( 0 -cy));
        final float AP = (float) sqrt((ax-ay)*(ax-ay)+(bx-by)*(bx-by)+(cx-cy)*(cx-cy));
        final float PB = (float) sqrt(( 0 -ax)*( 0 -ax)+( 0 -bx)*( 0 -bx)+( 0 -cx)*( 0 -cx));
        return Math.abs( AB - AP + PB ) < EPSILON;
    }

    public int minX() {
        return min(x0,x1);
    }

    public int maxX() {
        return max(x0,x1);
    }

    public int minY() {
        return min(y0,y1);
    }

    public int maxY() {
        return max(y0,y1);
    }

    public void setLeftNode(Node node) {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call setLeftNode() on non-horizontal line " + this );
        }
        if ( x0 < x1 ) {
            node.set( x0,y0 );
            this.node0 = node;
        } else {
            node.set( x1,y1 );
            this.node1 = node;
        }
        node.setRight(this);
    }

    public void setRightNode(Node node) {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call setRightNode() on non-horizontal line " + this );
        }
        if ( x0 < x1 )
        {
            node.set( x1,y1 );
            this.node1 = node;
        }
        else
        {
            node.set( x0,y0 );
            this.node0 = node;
        }
        node.setLeft(this);
    }

    public void setTopNode(Node node) {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call setTopNode() on non-vertical line " + this );
        }
        if ( y0 < y1 )
        {
            node.set( x0,y0 );
            this.node0 = node;
        }
        else
        {
            node.set( x1,y1 );
            this.node1 = node;
        }
        node.setDown(this);
    }

    public void setBottomNode(Node node)
    {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call setBottomNode() on non-vertical line " + this );
        }
        if ( y0 < y1 )
        {
            node.set( x1,y1 );
            this.node1 = node;
        }
        else
        {
            node.set( x0,y0 );
            this.node0 = node;
        }
        node.setUp(this);
    }

    public Node leftNode()
    {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call leftNode() on non-horizontal line " + this );
        }
        if ( x0 < x1 ) {
            return node0;
        }
        return node1;
    }

    public Node rightNode()
    {
        if ( ! isHorizontal() ) {
            throw new UnsupportedOperationException( "Must not call rightNode() on non-horizontal line " + this );
        }
        if ( x0 < x1 ) {
            return node1;
        }
        return node0;
    }

    public Node topNode()
    {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call topNode() on non-vertical line " + this );
        }
        if ( y0 < y1 ) {
            return node0;
        }
        return node1;
    }

    public Node bottomNode()
    {
        if ( ! isVertical() ) {
            throw new UnsupportedOperationException( "Must not call bottomNode() on non-vertical line " + this );
        }
        if ( y0 < y1 ) {
            return node1;
        }
        return node0;
    }

    public float length()
    {
        int dx = x1 - x0;
        int dy = y1 - y0;
        return (float) Math.sqrt( dx*dx + dy*dy );
    }

    @Override
    public boolean equals(Object obj)
    {
        if ( obj instanceof Line)
        {
            final Line o = (Line) obj;
            return obj == this || this.x0 == o.x0 && this.y0 == o.y0 && this.x1 == o.x1 && this.y0 == o.y1;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( x0, y0, x1, y1, m );
    }

    public boolean intersects(Line other)
    {
        int thisX0 = this.x0;
        int thisY0 = this.y0;
        int thisX1 = this.x1;
        int thisY1 = this.y1;

        int otherX0 = other.x0;
        int otherY0 = other.y0;
        int otherX1 = other.x1;
        int otherY1 = other.y1;

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
            if ( min(nonVertLine.x0, nonVertLine.x1) <= vertLine.x0 && max(nonVertLine.x0,nonVertLine.x1) >= vertLine.x0 ) {
                return min(nonVertLine.y0,nonVertLine.y1) >= min(vertLine.y0,vertLine.y1) &&
                max(nonVertLine.y0,nonVertLine.y1) <= max(vertLine.y0,vertLine.y1);
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
            if ( this.y0 == other.y0 )
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
        return y0 == y1;
    }

    public boolean isVertical() {
        return x0 == x1;
    }

    public void draw(Graphics2D gfx)
    {
        gfx.drawLine( x0,y0,x1,y1 );
    }

    public Node getNodeForEndpoint(int x,int y) {
        if (this.x0 == x & this.y0 == y) {
            return node0;
        }
        if (this.x1 == x && this.y1 == y) {
            return node1;
        }
        return null;
    }
}