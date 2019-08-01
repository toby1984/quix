package de.codesourcery.quix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineCollection implements ICollisionCheck
{
    protected final Line tmp = new Line(0,0,0,0);

    public List<Line> lines = new ArrayList<>();

    public Color lineColor = Color.WHITE;

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

    public void draw(Graphics2D gfx)
    {
        gfx.setColor( lineColor );
        for ( Line l : lines )
        {
            l.assertValid(); // TODO: Remove debug code
            l.draw( gfx );
        }
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
        // make sure each line is connected to the next
        Line first = lines.get( 0 );
        Line last = lines.get( lines.size() -1 );

        if (! last.shareEndpoint( first ) ) {
            lines.forEach( l -> System.err.println( l ) );
            throw new IllegalStateException( "Lines do not form a closed loop" );
        }

        for ( int i = 1 ; i < lines.size() ; i++) {
            Line previous  = lines.get( i-1 );
            Line current = lines.get( i );
            if (! previous.shareEndpoint( current ) ) {
                throw new IllegalStateException( "Lines do not form a closed loop" );
            }
        }

        final Poly poly = new Poly();
        final Map<Integer,Node> vertices = new HashMap<>();

        // clone vertices and find geometric center
        // for sorting points
        final Node center = new Node(0,0);
        int count = 0;

        for ( Line l : lines ) {
            Node vertex1 = vertices.get( l.node0.id );
            if ( vertex1 == null ) {
                vertex1 = new Node( l.node0.x, l.node0.y );
                vertices.put( l.node0.id, vertex1 );
                poly.vertices.add( vertex1 );
                count++;
                center.add( vertex1 );
            }
            Node vertex2 = vertices.get( l.node1.id );
            if ( vertex2 == null ) {
                vertex2 = new Node( l.node1.x, l.node1.y );
                vertices.put( l.node1.id, vertex2 );
                poly.vertices.add( vertex2 );
                count++;
                center.add( vertex2 );
            }
        }
        center.divideBy( count );

        // sort clock-wise
        poly.vertices.sort( (a,b) -> {
            float ang0 = center.angleInDegrees( a );
            float ang1 = center.angleInDegrees( b );
            return Float.compare( ang0, ang1 );
        });

        // add edges
        for ( int i = 0, len = poly.vertices.size() ; i< len ; i++ )
        {
            Node a = poly.vertices.get(i);
            Node b = (i+1) < len ? poly.vertices.get(i+1) : poly.vertices.get(0);
            poly.edges.add( new Line(a,b) );
        }
        return poly;
    }
}