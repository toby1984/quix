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

    private final GameState gameState = new GameState();
    private final MyPanel panel = new MyPanel(gameState);

    // user input
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;

    private boolean fastSpeed;

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
        if ( ! gameState.gameOver )
        {
            movePlayer();
        }
        if ( ! gameState.gameOver )
        {
            movePlayer();
        }
        panel.tick();
    }

    private boolean movePlayer()
    {
        final Mode mode = gameState.getMode( fastSpeed ? Mode.LINE_FAST : Mode.MOVE );
        if ( left || right )
        {
            return tryLeftRight(mode) || tryUpDown(mode);
        }
        else if ( up || down )
        {
            return tryUpDown(mode) || tryLeftRight(mode);
        }
        return false;
    }

    private boolean tryUpDown(Mode mode)
    {
        return up && gameState.moveUp(gameState.player, mode) || down && gameState.moveDown(gameState.player, mode);
    }

    private boolean tryLeftRight(Mode mode)
    {
        return left && gameState.moveLeft(gameState.player, mode) || right && gameState.moveRight(gameState.player, mode);
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
            });
            addKeyListener( new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    switch( e.getKeyCode() ) {
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

            gfx.setTransform( AffineTransform.getTranslateInstance( PLAYFIELD_XOFFSET, PLAYFIELD_YOFFSET) );

            // tick game state
            gameState.tick();

            // render game
            gameState.draw( gfx );

            // TODO: Debug - draw highlighted line (if any)
            if ( highlightedLine != null ) {
                gfx.setColor(Color.GREEN);
                highlightedLine.draw(gfx);
            }
            Toolkit.getDefaultToolkit().sync();
        }
    }
}
