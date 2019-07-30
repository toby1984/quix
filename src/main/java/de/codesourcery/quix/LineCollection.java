package de.codesourcery.quix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class LineCollection implements ICollisionCheck
{
    protected final Line tmp = new Line(0,0,0,0);

    public final List<Line> lines = new ArrayList<>();

    public Color lineColor = Color.WHITE;
    public Color fillColor = Color.RED;
    public boolean drawFilled = false;

    // bounding box
    int xmin,xmax;
    int ymin,ymax;

    public LineCollection()
    {
    }

    public LineCollection(boolean drawFilled)
    {
        this.drawFilled = drawFilled;
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
        if ( ! isEmpty() && drawFilled )
        {
            final Line last = lastLine();
            if ( last.x1() != line.x0() || last.y1() != line.y0() ) {
                throw new IllegalArgumentException( "Refusing to add line "+line+" that is not connected to "+last );
            }
        }
        this.lines.add(line);

        if ( lines.size() == 3 )
        {
            final Line line1 = lines.get( 0 );
            final Line line2 = lines.get( 1 );
            final Line line3 = lines.get( 2 );

            xmin = Math.min( line1.minX(), Math.min( line2.minX(), line3.minX() ) );
            xmax = Math.min( line1.maxX(), Math.max( line2.maxX(), line3.maxX() ) );

            ymin = Math.min( line1.minY(), Math.min( line2.minY(), line3.minY() ) );
            ymax = Math.min( line1.maxY(), Math.max( line2.maxY(), line3.maxY() ) );
        }
        else if ( lines.size() > 3 )
        {
            xmin = Math.min( xmin, line.minX() );
            xmax = Math.max( xmax, line.maxX() );

            ymin = Math.min( ymin, line.minY() );
            ymax = Math.max( ymax, line.maxY() );
        }
    }

    public Line firstLine() {
        return lines.get(0);
    }

    public Line lastLine() {
        return lines.get( lines.size() - 1 );
    }

    public void draw(Graphics2D gfx)
    {
        if ( isClosedPolygon() )
        {
            final int[] x = new int[lines.size()];
            final int[] y = new int[lines.size()];
            for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
            {
                final Line line = lines.get( i );
                line.assertValid(); // TODO: Remove debug code
                x[i] = line.x0();
                y[i] = line.y0();
            }
            gfx.setColor( fillColor );
            gfx.fillPolygon( x,y,x.length );
            gfx.setColor( lineColor );
            gfx.drawPolygon( x,y,x.length);
        } else {
            gfx.setColor( lineColor );
            for ( Line l : lines )
            {
                l.assertValid(); // TODO: Remove debug code
                l.draw( gfx );
            }
        }
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public boolean isClosedPolygon() {

        if ( isEmpty() )
        {
            return false;
        }
        final Line first = firstLine();
        final Line last = lastLine();
        return last.x1() == first.x0() && last.y1() == first.y0();
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
}