package de.codesourcery.quix;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class IncompleteLineCollection extends LineCollection
{
    enum MoveResult {
        MOVED,
        CANT_MOVE,
        TOUCHED_FOREIGN_LINE
    }

    public final Line currentLine;
    public Direction currentDirection;

    public final Mode mode;
    public boolean completed;

    public IncompleteLineCollection(Mode mode,int x,int y,Direction currentDirection,Node newNode)
    {
        if ( currentDirection == null ) {
            throw new IllegalArgumentException("currentDirection cannot be NULL");
        }
        if ( mode == null || mode == Mode.MOVE ) {
            throw new IllegalArgumentException("mode cannot be NULL or Mode.MOVE");
        }
        this.mode = mode;
        currentLine = new Line(x,y,x,y);
        this.currentDirection = currentDirection;
        switch( currentDirection ) {

            case LEFT:
                currentLine.setRightNode(newNode);
                break;
            case RIGHT:
                currentLine.setLeftNode(newNode);
                break;
            case UP:
                currentLine.setBottomNode(newNode);
                break;
            case DOWN:
                currentLine.setTopNode(newNode);
                break;
        }
        currentLineChanged( currentLine );
    }

    public MoveResult tryMove(Direction newDirection,ICollisionCheck check)
    {
        if ( completed ) {
            throw new IllegalStateException("Already completed");
        }

        if ( newDirection == currentDirection.opposite() ) {
            return MoveResult.CANT_MOVE;
        }

        int x0 = currentLine.x1;
        int y0 = currentLine.y1;
        int x1 = currentLine.x1+newDirection.dx;
        int y1 = currentLine.y1+newDirection.dy;

        Line line = check.getLine(x1,y1);
        if ( line == null )
        {
            // we can move
            if ( newDirection == currentDirection)
            {
                // we keep moving in the same direction
                currentLine.setEnd(x1, y1);
            }
            else
            {
                // we changed direction, complete the current line segment and
                // initialize a new one
                final Line previousLine = currentLine.shallowCopy();
                add( previousLine );
                currentLine.set( x0,y0,x1,y1);
                currentLine.node0 = currentLine.node1 = null;
                final Node newNode = new Node(x0,y0);
                final Direction previousDirection = currentDirection;
                currentDirection = newDirection;
                switch(newDirection)
                {
                    case LEFT: // we're now moving left;
                        switch( previousDirection )
                        {
                            case UP:
                                previousLine.setTopNode( newNode );
                                break;
                            case DOWN:
                                previousLine.setBottomNode( newNode );
                                break;
                        }
                        currentLine.setRightNode( newNode );
                        break;
                    case RIGHT:
                        switch( previousDirection )
                        {
                            case UP:
                                previousLine.setTopNode( newNode );
                                break;
                            case DOWN:
                                previousLine.setBottomNode( newNode );
                                break;
                        }
                        currentLine.setLeftNode( newNode );
                        break;
                    case UP:
                        switch( previousDirection )
                        {
                            case LEFT:
                                previousLine.setLeftNode( newNode );
                                break;
                            case RIGHT:
                                previousLine.setRightNode( newNode );
                                break;
                        }
                        currentLine.setBottomNode( newNode );
                        break;
                    case DOWN:
                        switch( previousDirection )
                        {
                            case LEFT:
                                previousLine.setLeftNode( newNode );
                                break;
                            case RIGHT:
                                previousLine.setRightNode( newNode );
                                break;
                        }
                        currentLine.setTopNode( newNode );
                        break;
                }
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
        completed = true;

        add( currentLine );

        final Node newNode = check.split(line, x1, y1);
        currentLine.x1 = x1;
        currentLine.y1 = y1;
        switch( currentDirection )
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
        if ( ! completed )
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