package de.codesourcery.quix;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
        movePlayer();
        movePlayer();
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

        private final Quix quix = new Quix();

        public MyPanel(GameState gameState) {
            setupQuix(gameState);
            setFocusable( true );
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

        private void setupQuix(GameState gameState)
        {
            int p0x=50,p0y=50,p1x=75,p1y=25;
            int xStep = 5, yStep = 5;
            for ( int lines = 10 ; lines > 0 ; lines-- )
            {
                final QuixLine line = new QuixLine( p0x, p0y, p1x, p1y );
                line.dx0 = 1;
                line.dy0 = 1;

                line.dx1 = 1;
                line.dy1 = 1;
                quix.add( line );

                p0x += xStep;
                p0y += xStep;

                p1x += yStep;
                p1y += yStep;
            }
        }

        public void tick()
        {
            quix.tick( gameState );
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D gfx = (Graphics2D) g;
            super.paintComponent( gfx );

            gfx.setTransform( AffineTransform.getTranslateInstance( PLAYFIELD_XOFFSET, PLAYFIELD_YOFFSET) );

            // draw quix
            quix.draw( gfx );

            // draw playing field
            gameState.draw( gfx );

            // draw player
            final float radius = 6;
            gfx.setColor( Color.BLUE );
            gfx.fillArc( gameState.player.x-(int) radius/2,
                    gameState.player.y - (int) radius/2 , (int) radius, (int) radius, 0,360 );

            Toolkit.getDefaultToolkit().sync();
        }

        private void draw(Line line, Color color,Graphics2D g)
        {
            g.setColor(  color  );
            line.draw(g);
            System.out.println("DRAW: "+line);
        }
    }
}
