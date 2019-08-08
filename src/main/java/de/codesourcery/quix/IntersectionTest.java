package de.codesourcery.quix;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class IntersectionTest extends JFrame
{
    public IntersectionTest() throws HeadlessException
    {
        super("test");

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        final JPanel panel = new JPanel() {

            final LineCollection lines = new LineCollection();

            private final Point p = new Point();
            private Node p0 = null;

            {
                addMouseMotionListener( new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e)
                    {
                        p.setLocation( e.getX(), e.getY() );
                        repaint();
                    }
                } );
                addMouseListener( new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        final Node newNode = new Node( e.getX(), e.getY() );
                        if (p0 == null ) {
                            p0 = newNode;
                        } else {
                            lines.add( new Line(p0,newNode ) );
                            p0 = null;
                        }
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent( g );

                g.setColor( Color.BLACK );
                g.drawString(p.toString(), 15,15);

                final Set<Line> intersected = new HashSet<>();
                for ( int i = 0,len = lines.size() ; i < len ; i++ )
                {
                    final Line l1 = lines.get( i );
                    for (int j = i + 1; j < len; j++)
                    {
                        Line l2 = lines.get( j );
                        if ( l1.intersects( l2 ) )
                        {
                            intersected.add( l1 );
                            intersected.add( l2 );
                        }
                    }
                }
                for ( Line l : lines.lines )
                {
                    if ( intersected.contains(l) )
                    {
                        g.setColor( Color.RED );
                    } else {
                        g.setColor( Color.GREEN );
                    }
                    l.draw( (Graphics2D) g );
                }

                if ( p0 != null ) {
                    int radius = 12;
                    g.setColor( Color.BLACK );
                    g.fillArc( p0.x-radius/2,p0.y-radius/2,radius,radius,0,360 );
                }
            }
        };
        panel.setFocusable( true );
        panel.setPreferredSize( new Dimension(640,480) );
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( panel, BorderLayout.CENTER );
        setLocationRelativeTo( null );
        pack();
        setVisible( true );
    }



    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait( () -> new IntersectionTest() );
    }
}
