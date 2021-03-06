package de.codesourcery.quix;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.lang.reflect.InvocationTargetException;

public class Main extends JFrame
{
    private static final int PLAYFIELD_XOFFSET = 10;
    private static final int PLAYFIELD_YOFFSET = 10;

    public static final boolean DEBUG_NODES = false;
    public static final boolean DEBUG_LINES = false;

    private final GameState gameState = new GameState();
    private final MyPanel panel = new MyPanel(gameState);

    // user input
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;

    private boolean fastSpeed;

    private Node highlightedNode; // debugging
    private Line highlightedLine; // debugging

    public Main()
    {
        super("Test");
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setPreferredSize( new Dimension( GameState.PLAYFIELD_WIDTH+50, GameState.PLAYFIELD_HEIGHT+50) );

        getContentPane().add( panel );
        pack();
        setLocationRelativeTo( null );
        setVisible( true );

        final Timer t = new Timer(16, ev -> gameLoop() );
        t.start();
    }

    private void gameLoop()
    {
        final Mode mode = fastSpeed ? Mode.LINE_FAST : Mode.MOVE;
        if ( ! gameState.gameOver )
        {
            gameState.movePlayer(left,right,up,down,mode );
        }
        if ( ! gameState.gameOver )
        {
            gameState.movePlayer(left,right,up,down,mode );
        }

        // tick game state
        gameState.tick();

        panel.tick();
    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait( () -> new Main() );
    }

    private final class MyPanel extends JPanel {

        public MyPanel(GameState gameState)
        {
            ToolTipManager.sharedInstance().registerComponent( this );
            ToolTipManager.sharedInstance().setDismissDelay( 3000 );

            setFocusable( true );
            addMouseMotionListener( new MouseAdapter()
            {
                private final Vec2 v = new Vec2();

                @Override
                public void mouseMoved(MouseEvent e)
                {
                    v.set( e.getX(), e.getY() );

                    if ( DEBUG_NODES )
                    {
                        var newHL = gameState.getClosestNode(v);
                        if (newHL != highlightedNode)
                        {
                            if (newHL != null)
                            {
                                System.out.println("-----------");
                                System.out.println("Node "+newHL);
                                newHL.visitDirections((dir,line) -> {
                                    System.out.println( dir+" : "+line);
                                });
                                setToolTipText(newHL.toString());
                            }
                            highlightedNode = newHL;
                        }
                    }

                    if ( DEBUG_LINES )
                    {
                        var newHl = gameState.getClosestLine( v );
                        if ( newHl != highlightedLine )
                        {
                            if ( newHl != null )
                            {
                                setToolTipText( newHl.toString() );
                            }
                            highlightedLine = newHl;
                        }
                    }
                }
            });
            addKeyListener( new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    switch( e.getKeyCode() )
                    {
                        case KeyEvent.VK_BACK_SPACE:
                            gameState.restart();
                            break;
                        case KeyEvent.VK_SPACE:
                            fastSpeed = true;
                            break;
                        case KeyEvent.VK_UP:
                            up = true;
                            break;
                        case KeyEvent.VK_DOWN:
                            down = true;
                            break;
                        case KeyEvent.VK_LEFT:
                            left = true;
                            break;
                        case KeyEvent.VK_RIGHT:
                            right = true;
                            break;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e)
                {
                    if ( gameState.gameOver )
                    {
                        if ( e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER )
                        {
                            gameState.restart();
                            fastSpeed = false;
                            up = down = left = right = false;
                        }
                        return;
                    }
                    switch( e.getKeyCode() )
                    {
                        case KeyEvent.VK_SPACE:
                            fastSpeed = false;
                            break;
                        case KeyEvent.VK_UP:
                            up = false;
                            break;
                        case KeyEvent.VK_DOWN:
                            down = false;
                            break;
                        case KeyEvent.VK_LEFT:
                            left = false;
                            break;
                        case KeyEvent.VK_RIGHT:
                            right = false;
                            break;
                    }
                }
            });
        }

        public void tick()
        {
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            final Graphics2D gfx = (Graphics2D) g;
            super.paintComponent( gfx );

            double w = getWidth();
            double h = getHeight();

            final double scaleW = w / (GameState.PLAYFIELD_WIDTH*1.1);
            final double scaleH = h / (GameState.PLAYFIELD_HEIGHT*1.1);

            AffineTransform transform =
                AffineTransform.getScaleInstance( scaleW, scaleH );
            transform.translate( PLAYFIELD_XOFFSET, PLAYFIELD_YOFFSET );

            gfx.setTransform( transform );

            // render game
            gameState.draw( gfx );

            if ( DEBUG_NODES && highlightedNode != null )
            {
                int radius = 12;
                gfx.setColor(Color.GREEN);

                gfx.fillArc(highlightedNode.x - radius/2,
                    highlightedNode.y - radius/2, radius, radius, 0,360);

                highlightedNode.visitDirections((dir,exit ) -> exit.draw(gfx));
            }

            // TODO: Debug - draw highlighted line (if any)
            if ( DEBUG_LINES && highlightedLine != null ) {
                gfx.setColor(Color.GREEN);
                highlightedLine.draw(gfx);
            }
            Toolkit.getDefaultToolkit().sync();
        }
    }
}
