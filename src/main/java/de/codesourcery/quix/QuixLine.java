package de.codesourcery.quix;

import java.awt.Point;

public class QuixLine extends Line
{
    public int dx0=1,dy0=1;
    public int dx1=1,dy1=1;

    public QuixLine(Point p0, Point p1)
    {
        super( p0, p1 );
    }

    public QuixLine(int x0, int y0, int x1, int y1)
    {
        super( x0, y0, x1, y1 );
    }

    /**
     *
     * @param check
     * @return line the quix collided with (if any)
     */
    public boolean tick(GameState check)
    {
        int newX0 = x0() + dx0;
        int newY0 = y0() + dy0;

        int newX1 = x1() + dx1;
        int newY1 = y1() + dy1;

        boolean flipY0=false;
        boolean flipY1=false;

        boolean flipX0=false;
        boolean flipX1=false;

        boolean gameOver = false;

        // check first line endpoint
        Line tmp = check.getLine(newX0, newY0);
        if ( tmp != null )
        {
            gameOver |= check.isInCurrentlyDrawnPoly( tmp );
            if ( dy0 != 0 )
            {
                if ( dx0 != 0 ) {
                    // moving up/down-left or up/down-right
                    if ( tmp.isHorizontal() ) {
                        // hit top
                        flipY0=true;
                    } else {
                        // hit left or right
                        flipX0=true;
                    }
                } else {
                    // moving straight up or down
                    flipY0=true;
                }
            } else {
                // dy0 == 0 -> moving only horizontally
                if ( dx0 != 0 ) {
                    // hit left or right border
                    flipX0=true;
                }
            }
        }

        // check second line endpoint
        tmp = check.getLine( newX1,newY1 );
        if ( tmp != null )
        {
            gameOver |= check.isInCurrentlyDrawnPoly( tmp );
            if ( dy1 != 0 )
            {
                if ( dx1 != 0 ) {
                    // moving up/down-left or up/down-right
                    if ( tmp.isHorizontal() ) {
                        // hit top
                        flipY1 = true;
                    } else {
                        // hit left or right
                        flipX1 = true;
                    }
                } else {
                    // moving straight up or down
                    flipY1 = true;
                }
            } else {
                // dy0 == 0 -> moving only horizontally
                if ( dx1 != 0 ) {
                    // hit left or right border
                    flipX1 = true;
                }
            }
        }

        // check whether line itself intersects another line
        tmp = check.intersects( newX0, newY0, newX1, newY1 );
        if ( tmp != null && ! check.isBorder( tmp ) )
        {
            gameOver |= check.isInCurrentlyDrawnPoly( tmp );
            flipX0 = true;
            flipX1 = true;
            flipY0 = true;
            flipY1 = true;
        }

        if ( flipX0 || flipY0 )
        {
            if ( flipX0 ) {
                dx0 = -dx0;
            }
            if ( flipY0 ) {
                dy0 = -dy0;
            }
        } else {
            setX0( x0() + dx0 );
            setY0( y0() + dy0 );
        }

        if ( flipX1 || flipY1 )
        {
            if ( flipX1 ) {
                dx1 = -dx1;
            }
            if ( flipY1 ) {
                dy1 = -dy1;
            }
        } else {
            setX1( x1() + dx1 );
            setY1( y1() + dy1 );
        }
        return gameOver;
    }

    public boolean hasSameHeading(QuixLine other)
    {
        return ( this.dx0 == other.dx0 && this.dy0 == other.dy0 || this.dx0 == -other.dx0 && this.dy0 == -other.dy0) &&
        ( this.dx1 == other.dx1 && this.dy1 == other.dy1 || this.dx1 == -other.dx1 && this.dy1 == -other.dy1);
    }
}
