package de.codesourcery.quix;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Poly
{
    public static int ID = 0;

    public final List<Node> vertices = new ArrayList<>();
    public final List<Line> edges = new ArrayList<>();

    public int id = Poly.ID++;

    public void draw(Graphics2D gfx)
    {
        for (int i = 0, edgesSize = edges.size(); i < edgesSize; i++)
        {
            draw( edges.get( i ), gfx );
        }
    }

    @Override
    public String toString()
    {
        return "Poly #"+id+": "+edges.stream().map( x -> asString(x.node0)+" -> "+asString(x.node1) ).collect( Collectors.joining(" | " ) );
    }

    private static String asString(Node n) {
        return "("+n.x+","+n.y+")";
    }

    public Poly add(Line l)
    {
        this.edges.add(l);
        if ( ! vertices.contains( l.node0 ) ) {
            vertices.add( l.node0 );
        }
        if ( ! vertices.contains( l.node1 ) ) {
            vertices.add( l.node1 );
        }
        return this;
    }

    private void draw(Line e, Graphics2D gfx)
    {
        gfx.drawLine( e.node0.x, e.node0.y, e.node1.x, e.node1.y );
    }

    public List<Poly> triangulate()
    {
        if ( edges.size() < 3 ) {
            throw new UnsupportedOperationException( "Need at least 3 edges" );
        }

        List<Node> nodes = Poly.getPoints( edges );
        System.out.println("TRIANGULATE:");
        nodes.forEach(  n -> System.out.println( n ) );

        final List<Poly> result = new ArrayList<>();
        if ( ! triangulateClockwise( nodes, result ) )
        {
            if ( ! triangulateCounterClockwise( nodes, result ) ) {
                throw new RuntimeException("triangulation failed");
            }
        }
        if ( nodes.size() == 3 )
        {
            final Node p0 = nodes.get(0);
            final Node p1 = nodes.get(1);
            final Node p2 = nodes.get(2);
            final Poly poly = new Poly()
                    .add( new Line(p0,p1))
                    .add( new Line(p1,p2) )
                    .add( new Line(p2,p0) );
            result.add( poly );
        }
        return result;
    }

    private boolean triangulateCounterClockwise(List<Node> nodes,List<Poly> result)
    {
outer:
        while(true)
        {
            for (int i = nodes.size()-1; i >= 0 && nodes.size() > 3; i--)
            {
                final Node p0 = nodes.get(i);
                final Node p1 = nodes.get(i-1);
                final Node p2 = nodes.get(i-2);

                int l = ((p0.x - p1.x) * (p2.y - p1.y) - (p0.y - p1.y) * (p2.x - p1.x));
                if ( l < 0 )
                {
                    final Line shrunk = new Line(p0,p2).shrink();
                    if ( isInsidePoly( shrunk, edges ))
                    {
                        // we found an 'ear', remove it
                        final Poly poly = new Poly()
                                .add( new Line(p2,p1))
                                .add( new Line(p1,p0) )
                                .add( new Line(p0,p2) );
                        result.add( poly );
                        nodes.remove(i-1);
                        continue outer;
                    }
                }
            }
            if ( nodes.size() > 3 ) {
                return false;
            }
            break;
        }
        return true;
    }

    private boolean triangulateClockwise(List<Node> nodes,List<Poly> result)
    {
outer:
        while(true)
        {
            for (int i = 0; (i+2) < nodes.size() && nodes.size() > 3; i++)
            {
                final Node p0 = nodes.get(i);
                final Node p1 = nodes.get(i+1);
                final Node p2 = nodes.get(i+2);

                int l = ((p0.x - p1.x) * (p2.y - p1.y) - (p0.y - p1.y) * (p2.x - p1.x));
                if ( l < 0 )
                {
                    final Line shrunk = new Line(p0,p2).shrink();
                    if ( isInsidePoly( shrunk, edges ))
                    {
                        // we found an 'ear', remove it
                        final Poly poly = new Poly()
                                .add( new Line(p0,p1))
                                .add( new Line(p1,p2) )
                                .add( new Line(p2,p0) );
                        result.add( poly );
                        nodes.remove(i+1);
                        continue outer;
                    }
                }
            }
            if ( nodes.size() > 3 ) {
                return false;
            }
            break;
        }
        return true;
    }

    private boolean isInsidePoly(Line e, List<Line> edges)
    {
        System.out.println("Checking "+e);
        // make sure the line does not intersect with any of the other edges
        for ( Line l : edges )
        {
            if ( e.intersects( l ) ) {
                System.out.println("--- INTERSECTS "+l);
                return false;
            }
        }

        // make sure both points are inside
        // the polygon
        final Rectangle bb = calculateBoundingBox( edges );

        System.out.println("Bounding box: "+bb);
        if ( ! contains( e.node0.x, e.node0.y, bb, edges )  ) {
            System.out.println("--- Outside BB for node0: "+e.node0);
            return false;
        }
        if ( ! contains( e.node1.x, e.node1.y, bb, edges )  ) {
            System.out.println("--- Outside BB for node1: "+e.node1);
            return false;
        }
        return true;
    }

    private static int getIntersectionCount(Line line, List<Line> edges)
    {
        int count = 0;
        for ( Line l : edges )
        {
            if ( l.intersects( line ) ) {
                count++;
            }
        }
        return count;
    }

    public boolean contains(int x,int y) {
        final boolean result = contains( x, y, calculateBoundingBox( this.edges ), this.edges );
        System.out.println( "CONTAINS = "+result+" FOR "+this );
        return result;
    }

    public static boolean contains(int x,int y,Rectangle bb,List<Line> edges)
    {
        if ( ! bb.contains( x,y ) ) {
            System.out.println("OUTSIDE BOUNDING BOX");
            return false;
        }
        Line ray = new Line( bb.x - 1000, y, x, y );
        System.out.println("RAY: "+ray);

        int intersectionCount = getIntersectionCount( ray, edges );
        System.out.println("--- Intersection count "+intersectionCount+" for p0: ("+x+","+y+")");
        if ( (intersectionCount % 2) == 0 ) { // 0 or even number of intersections -> outside
            return false;
        }
        return true;
    }

    public Rectangle getBoundingBox() {
        return calculateBoundingBox( this.edges );
    }

    private static Rectangle calculateBoundingBox(List<Line> edges)
    {
        int xmin=0,xmax=0;
        int ymin=0,ymax=0;
        for (int i = 0, edgesSize = edges.size(); i < edgesSize; i++)
        {
            final Line l = edges.get( i );
            if ( i == 0 )
            {
                xmin = Math.min( l.node0.x, l.node1.x );
                ymin = Math.min( l.node0.y, l.node1.y );
                xmax = Math.max( l.node0.x, l.node1.x );
                ymax = Math.max( l.node0.y, l.node1.y );
            }
            else
            {
                xmin = Math.min( xmin, l.node1.x );
                ymin = Math.min( ymin, l.node1.y );
                xmax = Math.max( xmax, l.node1.x );
                ymax = Math.max( ymax, l.node1.y );
            }
        }
        final int w = xmax - xmin;
        final int h = ymax - ymin;
        if ( w < 0 || h < 0 ) {
            throw new IllegalStateException("BB side negative ?");
        }
        return new Rectangle( xmin, ymin, w, h );
    }

    public static void assertValidPolygon(List<Line> tmp)
    {
        if ( ! isValidPolygon(tmp ) ) {
            throw new IllegalStateException("Not a closed polygon with at least 3 points:\n\n"+Line.toShortString(tmp));
        }
    }

    /**
     * Checks whether a set of lines forms a closed polygon and
     * each of the lines is connected to it's predecessor.
     *
     * @param lines
     * @return
     */
    public static boolean isValidPolygon(List<Line> lines)
    {
        if ( lines.size() < 3 ) {
            return false;
        }
        Node first = lines.get(0).node0;
        Node previous = lines.get(0).node1;
        for (int i = 1; i < lines.size() ; i++)
        {
            Line line = lines.get(i);
            if ( line.node0 != previous ) {
                return false;
            }
            previous = line.node1;
        }
        if ( lines.get( lines.size()-1 ).node1 != first ) {
            return false;
        }
        return true;
    }

    public static List<Node> getPoints(List<Line> lines) {
        List<Node> result = new ArrayList<>();
        for ( int i = 0, len = lines.size() ; i < len ; i++ )
        {
            Node n = lines.get(i).node0;
            if ( ! result.contains( n ) ) {
                result.add( n );
            }
            n = lines.get(i).node1;
            if ( ! result.contains( n ) ) {
                result.add( n );
            }

        }
        return result;
    }
}
