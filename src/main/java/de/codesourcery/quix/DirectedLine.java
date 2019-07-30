package de.codesourcery.quix;

public class DirectedLine extends Line
{
    public final Direction direction;

    public DirectedLine(Direction direction,Node existing)
    {
        super(existing, new Node(existing).add( direction ) );
        this.direction = direction;
    }

    public DirectedLine changeDirection(Direction newDir) {

        if ( newDir == direction.opposite() || newDir == direction ) {
            throw new IllegalArgumentException( "Cannot change from "+direction+" to "+newDir );
        }

        DirectedLine newLine;
        switch( direction )
        {
            case LEFT:
            case RIGHT:
                // can only be up or down
                Node node = direction == Direction.LEFT ? leftNode() : rightNode();
                newLine = new DirectedLine( newDir, node);
                if ( newDir == Direction.UP ) {
                    node.setUp( newLine );
                    if ( direction == Direction.LEFT ) {
                        newLine.bottomNode().setRight( this );
                    }
                    else
                    {
                        newLine.bottomNode().setLeft( this );
                    }
                } else {
                    // move down
                    node.setDown( newLine );
                    if ( direction == Direction.LEFT ) {
                        newLine.topNode().setRight( this );
                    }
                    else
                    {
                        newLine.topNode().setLeft( this );
                    }
                }
                break;
            case UP:
            case DOWN:
                // can only be left or right
                node = direction == Direction.UP ? topNode() : bottomNode();
                newLine = new DirectedLine( newDir, node);
                if ( newDir == Direction.LEFT ) {
                    node.setLeft( newLine );
                    if ( direction == Direction.UP ) {
                        newLine.rightNode().setDown( this );
                    }
                    else
                    {
                        newLine.rightNode().setUp( this );
                    }
                } else {
                    // move right
                    node.setRight( newLine );

                    if ( direction == Direction.UP )
                    {
                        newLine.leftNode().setDown( this );
                    }
                    else
                    {
                        newLine.leftNode().setUp( this );
                    }
                }
                break;
            default:
                throw new RuntimeException("Unreachable code reached");
        }
        return newLine;
    }

    public void move()
    {
        switch( direction ) {
            case LEFT:
                leftNode().add( direction );
                return;
            case RIGHT:
                rightNode().add( direction );
                return;
            case UP:
                topNode().add( direction );
                return;
            case DOWN:
                bottomNode().add( direction );
                return;
            default:
                throw new IllegalStateException( "Unexpected value: " + direction );
        }
    }
}
