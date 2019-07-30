package de.codesourcery.quix;

import java.awt.Graphics2D;

public abstract class IncompleteLineCollection extends LineCollection
{
    enum MoveResult {
        MOVED,
        CANT_MOVE,
        TOUCHED_FOREIGN_LINE
    }

    public final Node firstNode;
    public Node lastNode;

    public DirectedLine currentLine;

    public final Mode mode;

    public IncompleteLineCollection(Mode mode, Direction currentDirection, Node newNode)
    {
        if ( currentDirection == null ) {
            throw new IllegalArgumentException("currentDirection cannot be NULL");
        }
        if ( mode == null || mode == Mode.MOVE ) {
            throw new IllegalArgumentException("mode cannot be NULL or Mode.MOVE");
        }
        this.firstNode = newNode;
        this.mode = mode;
        this.currentLine = new DirectedLine( currentDirection, newNode );

        switch( currentDirection )
        {
            case LEFT:
                newNode.setLeft( currentLine );
                break;
            case RIGHT:
                newNode.setRight( currentLine );
                break;
            case UP:
                newNode.setUp( currentLine );
                break;
            case DOWN:
                newNode.setDown( currentLine );
                break;
        }
        currentLineChanged( currentLine );
    }

    public MoveResult tryMove(Direction newDirection,ICollisionCheck check)
    {
        if ( lastNode != null ) {
            throw new IllegalStateException("Already completed");
        }
        if ( newDirection == currentLine.direction.opposite() ) {
            return MoveResult.CANT_MOVE;
        }

        int x1 = currentLine.x1() +newDirection.dx;
        int y1 = currentLine.y1() +newDirection.dy;

        Line line = check.getLine(x1,y1);
        if ( line == null )
        {
            // we can move
            if ( newDirection == currentLine.direction )
            {
                // we keep moving in the same direction
                currentLine.move();
            }
            else
            {
                // we changed direction, complete the current line segment and
                // initialize a new one
                add( currentLine );
                currentLine = currentLine.changeDirection( newDirection );
            }
            return MoveResult.MOVED;
        }
        // we hit a line.
        // If it's any of the lines in this collection, the move is not allowed
        if ( line == currentLine || containsIdentity(line ) ) {
            return MoveResult.CANT_MOVE;
        }
        // we hit a line but it's none of our own
        // -> split the line, inserting a new node here

        add( currentLine );

        final Node newNode = check.split(line, x1, y1);
        lastNode = newNode;

        switch( currentLine.direction )
        {
            case LEFT:
                currentLine.setLeftNode(newNode);
                break;
            case RIGHT:
                currentLine.setRightNode(newNode);
                break;
            case UP:
                currentLine.setTopNode(newNode);
                break;
            case DOWN:
                currentLine.setBottomNode(newNode);
                break;
        }
        System.out.println("*** Touched foreign line ***");
        currentLineChanged(line);
        return MoveResult.TOUCHED_FOREIGN_LINE;
    }

    @Override
    public void draw(Graphics2D gfx)
    {
        super.draw(gfx);
        if ( lastNode == null )
        {
            currentLine.draw(gfx);
        }
    }

    @Override
    public Line getLine(int x, int y) {
        Line result = super.getLine(x,y);
        if ( result == null && currentLine.contains(x,y)) {
            result = currentLine;
        }
        return result;
    }

    @Override
    public Line intersects(Line line) {
        Line result = super.intersects(line);
        if ( result == null && currentLine.intersects(line)) {
            result = currentLine;
        }
        return result;
    }

    @Override
    public Line intersects(int x0,int y0,int x1,int y1) {
        Line result = super.intersects(x0,y0,x1,y1);
        if ( result == null )
        {
            tmp.set(x0,y0,x1,y1);
            if ( currentLine.intersects(tmp) )
            {
                result = currentLine;
            }
        }
        return result;
    }

    protected abstract void currentLineChanged(Line line);
}