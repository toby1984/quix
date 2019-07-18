package de.codesourcery.quix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Quix
{
    private final List<QuixLine> lines = new ArrayList<>();

    public Color lineColor = Color.BLUE;

    public int randomizationInterval = 2*60;
    public int currentTick = 0;

    private final Random RND = new Random(0xdeadbeef);

    public void tick(GameState state)
    {
        final Difficulty difficulty = state.level;
        // randomize movement periodically
        currentTick++;
        if ( currentTick == randomizationInterval )
        {
            currentTick = 0;
            randomizeMovement(difficulty);
        }

        // animate quix
        for ( int j = state.level.quixSpeed ; j >= 0 ; j-- )
        {
            for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
            {
                lines.get(i).tick(state);
            }
        }
    }

    private void randomizeMovement(Difficulty level)
    {
        int dx = -1 + RND.nextInt(3);
        int dy = -1 + RND.nextInt(3);

        if ( RND.nextBoolean() )
        {
            for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
            {
                final QuixLine line = lines.get( i );
                line.dx0 = dx;
                line.dy0 = dy;
            }
        } else {
            for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
            {
                final QuixLine line = lines.get( i );
                line.dx1 = dx;
                line.dy1 = dy;
            }
        }
    }

    public void add(QuixLine line)
    {
        if ( ! lines.isEmpty() ) {
            if ( ! line.hasSameHeading( lines.get( lines.size()-1 ) ) )
            {
                throw new IllegalArgumentException( "Not same heading" );
            }
        }
        lines.add( line );
    }

    public void draw(Graphics2D gfx)
    {
        gfx.setColor( lineColor );

        for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
        {
            final QuixLine line = lines.get( i );
            line.draw( gfx );
        }
    }
}
