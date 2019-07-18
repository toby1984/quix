package de.codesourcery.quix;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntersectionExample extends JFrame
{
    public IntersectionExample()
    {
        super("Test");
        setPreferredSize( new Dimension(320,240 ) );
        getContentPane().add( new MyPanel() );
        pack();
        setLocationRelativeTo( null );
        setVisible( true );
    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait( () -> new IntersectionExample() );
    }

    private static final class MyPanel extends JPanel {

        private static final Color[] colors = { Color.RED , Color.BLUE };
        private final static float dash1[] = {10.0f};
        private final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

        private Point start;
        private Point end;

        private final List<Line> lines = new ArrayList<>();

        public MyPanel() {

            setFocusable( true );
            addKeyListener( new KeyAdapter()
            {
                @Override
                public void keyReleased(KeyEvent e)
                {
                    if ( e.getKeyCode() == KeyEvent.VK_DELETE ) {
                        start = end = null;
                        lines.clear();
                        repaint();
                    }
                }
            });

            addMouseListener( new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if ( start == null ) {
                        start = new Point(e.getPoint());
                        end = new Point(e.getPoint());
                    } else {
                        lines.add( new Line( start, e.getPoint()) );
                        start = end = null;
                    }
                    repaint();
                }
            });
            addMouseMotionListener( new MouseAdapter()
            {
                @Override
                public void mouseMoved(MouseEvent e)
                {
                    if ( start != null )
                    {
                        end.setLocation( e.getPoint() );
                        repaint();
                    }
                }
            } );

        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D gfx = (Graphics2D) g;
            super.paintComponent( gfx );

            if ( start != null ) {
                gfx.setColor( Color.GREEN );
                gfx.drawLine( start.x, start.y, end.x, end.y);
            }

            final Set<Line> intersectedLines = new HashSet<>();
            for ( int i = 0 , len = lines.size() ; i < len ; i++ )
            {
                final Line line1 = lines.get( i );
                boolean intersects = false;
                for ( int j = i+1 ; j < len ; j++ )
                {
                    final Line line2 = lines.get( j );
                    if ( line1.intersects( line2 ) )
                    {
                        intersects = true;
                        intersectedLines.add( line2 );
                        System.out.println("   INTERSECTION: "+line1+" <=> "+line2);
                    } else {
                        System.out.println("NO INTERSECTION: "+line1+" <=> "+line2);
                    }
                }
                if ( intersects ) {
                    intersectedLines.add(line1);
                }
            }
            for ( int i = 0 , len = lines.size() ; i < len ; i++ )
            {
                final Line line1 = lines.get( i );
                if ( intersectedLines.contains( line1 ) )
                {
                    final Stroke stroke = gfx.getStroke();
                    try
                    {
                        gfx.setStroke( dashed );
                        draw( line1, colors[i % colors.length], gfx );
                    } finally {
                        gfx.setStroke( stroke );
                    }
                } else {
                    draw( line1, colors[i % colors.length], gfx );
                }
            }
        }

        private void draw(Line line, Color color,Graphics2D g)
        {
            g.setColor(  color  );
            line.draw(g);
        }
    }
}
