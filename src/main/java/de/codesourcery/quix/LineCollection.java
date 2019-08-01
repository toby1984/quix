package de.codesourcery.quix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LineCollection implements ICollisionCheck
{
    protected final Line tmp = new Line(0,0,0,0);

    public List<Line> lines = new ArrayList<>();

    public Color lineColor = Color.WHITE;
    public Color fillColor = Color.WHITE;

    public LineCollection()
    {
    }

    public Line findLine(Node n0, Node n1)
    {
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
        {
            Line l = lines.get(i);
            if (l.hasEndpoints(n0, n1))
            {
                return l;
            }
        }
        return null;
    }

    public void clear() {
        lines.clear();
    }

    @Override
    public boolean isBorder(Line tmp)
    {
        throw new UnsupportedOperationException("isBorder()");
    }

    @Override
    public Line getLine(int x, int y) {
        for (int i = 0, playfieldLinesSize = lines.size(); i < playfieldLinesSize; i++)
        {
            final Line l = lines.get( i );
            if ( l.contains( x, y ) )
            {
                return l;
            }
        }
        return null;
    }

    @Override
    public Line intersects(Line line)
    {
        for (int i = 0, playfieldLinesSize = lines.size(); i < playfieldLinesSize; i++)
        {
            final Line l = lines.get( i );
            if ( l.intersects(line ) )
            {
                return l;
            }
        }
        return null;
    }

    @Override
    public Line intersects(int x0, int y0, int x1, int y1)
    {
        tmp.set(x0,y0,x1,y1);
        for (int i = 0, playfieldLinesSize = lines.size(); i < playfieldLinesSize; i++)
        {
            final Line l = lines.get( i );
            if ( l.intersects(tmp) )
            {
                return l;
            }
        }
        return null;
    }

    public int size() {
        return lines.size();
    }

    public boolean isNotEmpty() {
        return ! lines.isEmpty();
    }

    public Line get(int idx) {
        return lines.get(idx);
    }

    // contains() check based on identity (not equals())
    public boolean containsIdentity(Line line)
    {
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
        {
            Line l = lines.get(i);
            if (l == line)
            {
                return true;
            }
        }
        return false;
    }

    public void addAll(Collection<Line> col)
    {
        col.forEach(this::add);
    }

    public void add(Line line)
    {
        this.lines.add(line);
    }

    public void draw(Graphics2D gfx,boolean fillIfPossible)
    {
        if ( fillIfPossible && Poly.isValidPolygon(this.lines) )
        {
            gfx.setColor( fillColor );

            final int x[] = new int[lines.size()];
            final int y[] = new int[lines.size()];
            int ptr = 0;
            Node first = lines.get(0).node0;
            x[ptr] = first.x;
            y[ptr++] = first.y;

            for (int i = 1, linesSize = lines.size(); i < linesSize; i++)
            {
                final Node p = lines.get(i).node0;
                x[ptr]=p.x;
                x[ptr++]=p.y;
            }
            gfx.fillPolygon(x,y,x.length);
        }
        gfx.setColor( lineColor );
        for ( Line l : lines )
        {
            l.assertValid(); // TODO: Remove debug code
            l.draw( gfx );
        }
    }
    public void draw(Graphics2D gfx)
    {
        draw(gfx,false);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    @Override
    public Node split(Line line, int xSplit, int ySplit)
    {
        return splitLine(this,line,xSplit,ySplit);
    }

    public static Node splitLine(LineCollection collection,Line line, int xSplit, int ySplit)
    {
        Node middle;

        if ( line.isHorizontal() )
        {
            if ( line.isLeftEndpoint(xSplit,ySplit ) ) {
                return line.leftNode();
            }
            if ( line.isRightEndpoint(xSplit,ySplit ) ) {
                return line.rightNode();
            }

            System.out.println("Splitting "+line+"...");
            middle = new Node(xSplit,ySplit);
            final Node right = line.rightNode();

            final Line newLine = new Line(middle, right );
            right.setLeft( newLine );
            middle.setRight( newLine );
            line.setRightNode( middle );
            collection.add( newLine );
            System.out.println("new line: "+newLine);
        }
        else if ( line.isVertical() )
        {
            if ( line.isTopEndpoint(xSplit,ySplit ) ) {
                return line.topNode();
            }  if ( line.isBottomEndpoint(xSplit,ySplit ) ) {
            return line.bottomNode();
        }
            System.out.println("Splitting "+line+"...");
            middle = new Node(xSplit,ySplit);
            final Node bottom = line.bottomNode();
            final Line newLine = new Line(middle, bottom);
            line.setBottomNode( middle );
            middle.setDown( newLine );
            bottom.setUp( newLine );
            collection.add( newLine );
        } else {
            throw new IllegalStateException("Line is neither vertical nor horizontal? "+line);
        }
        return middle;
    }

    public Poly toPolygon() {

        if ( lines.size() < 3 ) {
            throw new IllegalStateException("Less than 3 lines, cannot convert to a polygon");
        }
        System.out.println("toPolygon(): \n"+Line.toShortString(lines));

        // swap line endpoints if necessary
        // so that for each line node0 is node1 of the
        // previous line
        final List<Node> nodeList = new ArrayList<>();
        nodeList.add( lines.get(0).node0.copy() );
        Node previous = lines.get(0).node1;

        Node center = lines.get(0).node0.copy();

        for (int i = 1, linesSize = lines.size(); i < linesSize; i++)
        {
            Line l = lines.get(i);
            if ( l.node0 != previous ) {
                l.swapEndpoints();
            }
            nodeList.add( l.node0 );
            center.add( l.node0 );
            previous = l.node1;
        }

        // sort clock-wise around center point
        center.divideBy(lines.size() );

        nodeList.sort( (a,b) -> -cmp(a,b,center) );

        center.divideBy( lines.size() );
        System.out.println("Center: "+center);

        // setup polygon
        final Poly poly = new Poly();
        for ( int i = 1, len = nodeList.size() ; i< len ; i++ ) {
            poly.add( new Line(nodeList.get(i-1),nodeList.get(i)) );
        }
        poly.add(new Line(nodeList.get(nodeList.size() - 1), nodeList.get(0)));
        Poly.assertValidPolygon(poly.edges);
        return poly;
    }

    private int cmp(Node a, Node b, Node center)
    {
        if ( a.matches(b) ) {
            return 0;
        }
        // a.x >= center.x && b.x < center.x
        if (a.x - center.x >= 0 && b.x - center.x < 0)
        {
            return -1;
        }
        // a.x < center.x && b.x >= center.x
        if (a.x - center.x < 0 && b.x - center.x >= 0)
        {
            return 1;
        }

        // a.x == center.x && b.x == center.x
        if (a.x - center.x == 0 && b.x - center.x == 0)
        {
            // a.y >= center.y || b.y >= center.y
            if (a.y - center.y >= 0 || b.y - center.y >= 0)
            {
                return a.y > b.y ? -1 : 1;
            }
            return b.y > a.y ? -1 : 1;
        }

        // compute the cross product of vectors (center -> a) x (center -> b)
        int det = (a.x - center.x) * (b.y - center.y) - (b.x - center.x) * (a.y - center.y);
        if (det < 0)
        {
            return -1;
        }
        if (det > 0)
        {
            return 1;
        }

        // points a and b are on the same line from the center
        // check which point is closer to the center
        int d1 = (a.x - center.x) * (a.x - center.x) + (a.y - center.y) * (a.y - center.y);
        int d2 = (b.x - center.x) * (b.x - center.x) + (b.y - center.y) * (b.y - center.y);
        return d1 > d2 ? -1 : 1;
    }
}