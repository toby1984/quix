package de.codesourcery.quix;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class TriangulateTest extends JFrame
{
    private static final boolean TEST_POLY_CONTAINS = false;

    private static final boolean DRAW_OUTLINE_FIRST = true;
    private static final boolean DRAW_POLYS = true;
    private static final boolean DRAW_OUTLINE = true;

    private static final boolean DRAW_TRIANGLE_BB = false;
    private static final boolean DRAW_HIGHLIGHTED_LINE = false;

    public TriangulateTest() throws HeadlessException
    {
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        final JPanel panel = new JPanel() {

            private final LineCollection lines = new LineCollection();
            private final List<Poly> polys = new ArrayList<>();

            private Node firstPoint;
            private Node previousPoint;

            private Poly highlightedPoly;
            private Line highlighted;

            {
                setFocusable( true );

                addMouseMotionListener( new MouseAdapter()
                {
                    @Override
                    public void mouseMoved(MouseEvent e)
                    {
                        final Vec2 p = new Vec2(e.getX(), e.getY() );
                        Line closest = null;
                        float dst2 = 0;
                        for ( Line l : lines.lines ) {
                            final float tmp = LinAlg.dist2ToLineSegment( l, p );
                            if ( closest == null || tmp < dst2 ) {
                                closest = l;
                                dst2 = tmp;
                            }
                        }
                        Poly newPoly = null;
                        if ( TEST_POLY_CONTAINS )
                        {
                            for (Poly poly : polys)
                            {
                                if ( poly.contains( e.getX(), e.getY() ) )
                                {
                                    if ( newPoly != null )
                                    {
                                        System.err.println( "More than one polygon contains the point " + e );
                                    }
                                    newPoly = poly;
                                    System.out.println( "HOVERING OVER " + newPoly );
                                }
                                for (Line l : poly.edges)
                                {
                                    final float tmp = LinAlg.dist2ToLineSegment( l, p );
                                    if ( closest == null || tmp < dst2 )
                                    {
                                        closest = l;
                                        dst2 = tmp;
                                    }
                                }
                            }
                        }
                        if ( highlighted != closest || newPoly != highlightedPoly)
                        {
                            highlighted = closest;
                            highlightedPoly = newPoly;
                            repaint();
                        }
                    }
                } );
                addMouseListener( new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if ( firstPoint == null ) {
                            clearLines();
                            firstPoint = previousPoint = new Node(e.getX(), e.getY());
                        } else {
                            final Node node = new Node( e.getX(), e.getY() );
                            final Line newLine = new Line( previousPoint, node );
                            System.out.println("ADDED: "+newLine);
                            lines.add( newLine );
                            previousPoint = node;
                        }
                        repaint();
                    }
                } );
                addKeyListener( new KeyAdapter()
                {
                    @Override
                    public void keyReleased(KeyEvent e)
                    {
                        if ( e.getKeyCode() == KeyEvent.VK_T )
                        {
                            Node p0 = Node.of(206,51);
                            Node p1 = Node.of(336,207);
                            Node p2 = Node.of(421,130);
                            Node p3 = Node.of(420,299);
                            Node p4 = Node.of(185,239);

                            clearLines();

                            lines.add( new Line(p0,p1 ) );
                            lines.add( new Line(p1,p2 ) );
                            lines.add( new Line(p2,p3 ) );
                            lines.add( new Line(p3,p4 ) );
                            lines.add( new Line(p4,p0 ) );

                            firstPoint = p0;
                            previousPoint = p4;
                            triangulate();
                        }
                        else if ( e.getKeyCode() == KeyEvent.VK_ENTER && lines.lines.size() >= 2 )
                        {
                            if ( firstPoint != null )
                            {
                                final Line newLine = new Line( previousPoint, firstPoint );
                                lines.add( newLine );
                                System.out.println("ADDED: "+newLine);
                            }
                            triangulate();
                        }
                    }
                });
            }

            private void clearLines() {
                lines.clear();
                highlighted = null;
                highlightedPoly = null;
                previousPoint = null;
                firstPoint = null;
                polys.clear();
            }

            private void triangulate()
            {
                firstPoint = previousPoint = null;
                polys.clear();
                polys.addAll( lines.toPolygon().triangulate() );
                System.out.println("Triangulated into "+polys.size()+" triangles.");
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent( g );

                if ( DRAW_OUTLINE_FIRST )
                {
                    drawOutline( g );
                    drawPolys( g );
                } else {
                    drawPolys( g );
                    drawOutline( g );
                }

                if ( DRAW_HIGHLIGHTED_LINE && highlighted != null )
                {
                    g.setColor( Color.GREEN);
                    highlighted.draw( (Graphics2D) g );

                    g.setColor( Color.BLACK );
                    g.setFont( g.getFont().deriveFont( Font.BOLD, 12f ) );
                    g.drawString( highlighted.toString(), 15,15 );
                }
            }

            private void drawPolys(Graphics g)
            {
                if ( ! DRAW_POLYS ) {
                    return;
                }
                final Color[] colors = { Color.MAGENTA, Color.CYAN, Color.PINK};
                for ( Poly p : polys )
                {
                    if ( p == highlightedPoly ) {
                        g.setColor( Color.ORANGE );
                    } else {
                        g.setColor( colors[ p.id % colors.length ] );
                    }
                    p.draw( (Graphics2D) g );

                    if ( DRAW_TRIANGLE_BB )
                    {
                    g.setColor( colors[ p.id % colors.length ] );
                    final Rectangle bb = p.getBoundingBox();
                    g.drawRect( bb.x, bb.y, bb.width, bb.height );

                    int cx = (int) bb.getCenterX();
                    int cy = (int) bb.getCenterY();
                    g.drawString("Poly #"+p.id,cx,cy);
                    }
                }
            }

            private void drawOutline(Graphics g)
            {
                if (! DRAW_OUTLINE ) {
                    return;
                }
                g.setColor( Color.BLACK );
                lines.lineColor = Color.BLACK;

                if ( lines.isEmpty() && firstPoint != null ) {
                    int radius = 12;
                    g.fillArc( firstPoint.x-radius/2,firstPoint.y-radius/2,radius,radius,0,360 );
                }
                lines.draw( (Graphics2D) g );
            }
        };

        panel.setPreferredSize( new Dimension( 640, 480 ) );
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( panel, BorderLayout.CENTER );
        pack();
        setLocationRelativeTo( null);
        setVisible( true );
    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait( () -> new TriangulateTest() );
    }
}
