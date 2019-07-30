    package de.codesourcery.quix;

    import java.util.function.Predicate;

    public class Enemy extends Entity
{
    public static long ID=0;

    public boolean outOfBounds;
    public Direction direction;
    public final long id = ID++;

    public Enemy()
    {
    }

    public void setDirection(Direction dir) {
        this.direction = dir;
    }

    public void tick(GameState state)
    {
        final Node node = getCurrentLine().getNodeForEndpoint( x, y );
        if ( node != null )
        {
            System.out.println("Picking new direction for "+this);
            pickNewDirection( state, node );
        }
        move( direction );
        if ( x < 0 || y < 0 || x > GameState.PLAYFIELD_WIDTH || y > GameState.PLAYFIELD_HEIGHT ) {
            this.outOfBounds = true;
            throw new IllegalStateException("Entity out of bounds: "+this);
        }
    }

    private void pickNewDirection(GameState state, Node node)
    {
        // check whether we can keep on going in the same direction
        // and roll a dice to see if we should
        final Line tmp = node.getExit( direction , null );
        if (  tmp != null && state.rnd.nextFloat() >= 0.33f ) {
            // keep going in same direction
            setCurrentLine(  tmp );
            System.out.println(this+" can continue in current direction along "+tmp);
            return;
        }

        // pick a new direction
        // TODO: Maybe don't chase the player on lines he's currently drawing ?
        final int exitCount = node.getExitCount( direction.opposite() );
        int idx = state.rnd.nextInt( exitCount );
        System.out.println(this+" CAN NOT continue in current direction, got "+exitCount+" exits and picked #"+idx);

        for ( Direction dir : Direction.values() )
        {
            Line l = node.getExit( dir, direction.opposite() );
            if ( l != null )
            {
                if ( idx == 0 ) {
                    System.out.println(this+" picked new direction "+dir+", new line: "+l);
                    this.direction = dir;
                    setCurrentLine( l );
                    return;
                }
                idx--;
            }
        }
        throw new RuntimeException("Unreachable code reached");
    }

    @Override
    public String toString()
    {
        return "Enemy #"+id+" heading "+direction+" "+super.toString();
    }
}