package de.codesourcery.quix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

public class GameState implements ICollisionCheck
{
    public static final Dimension PLAYFIELD_SIZE = new Dimension(640 ,480 );

    // hint: player location does NOT take playFieldOffset into consideration
    public final Player player = new Player();

    public Difficulty level = new Difficulty(1 );

    public static final int PLAYFIELD_WIDTH = PLAYFIELD_SIZE.width;
    public static final int PLAYFIELD_HEIGHT = PLAYFIELD_SIZE.height;

    public final LineCollection borderLines = new LineCollection();
    public final LineCollection playfieldLines = new LineCollection();

    private IncompleteLineCollection currentPoly;

    public GameState() {

        final Node nw = new Node(0,0);
        final Node ne = new Node( PLAYFIELD_WIDTH,0);
        final Node se = new Node( PLAYFIELD_WIDTH, PLAYFIELD_HEIGHT );
        final Node sw = new Node(0, PLAYFIELD_HEIGHT );

        final Line top = new Line(0,0, PLAYFIELD_WIDTH,0);
        final Line right = new Line( PLAYFIELD_WIDTH,0, PLAYFIELD_WIDTH, PLAYFIELD_HEIGHT );
        final Line bottom = new Line( PLAYFIELD_WIDTH, PLAYFIELD_HEIGHT,0, PLAYFIELD_HEIGHT );
        final Line left = new Line(0, PLAYFIELD_HEIGHT,0,0);

        top.setLeftNode( nw );
        top.setRightNode( ne );

        right.setTopNode( ne );
        right.setBottomNode( se );

        bottom.setRightNode( se );
        bottom.setLeftNode( sw );

        left.setBottomNode( sw );
        left.setTopNode( nw );
        borderLines.addAll( List.of( top,right,bottom,left ) );
        playfieldLines.addAll( List.of( top,right,bottom,left ) );

        player.x = player.y = 0;
        player.setCurrentLine(top);
    }

    /**
     * Determine how many pixels an entity could move upwards at its current location.
     *
     * @param entity
     * @return <code>true</code> if entity could move
     */
    public boolean moveUp(Entity entity,Mode mode)
    {
        if ( mode == Mode.LINE_FAST )
        {
            if ( entity.y > 0 )
            {
                if ( this.currentPoly == null )
                {
                    // TODO: Check whether there's free space below us...
                    Node newNode = playfieldLines.split(entity.getCurrentLine(),entity.x, entity.y );
                    currentPoly = new IncompleteLineCollection(mode, entity.x, entity.y, Direction.UP,newNode) {

                        @Override
                        protected void currentLineChanged(Line line)
                        {
                            entity.setCurrentLine(line);
                        }
                    };
                    entity.y--;
                } else {
                    switch( currentPoly.tryMove(Direction.UP,this) )
                    {
                        case MOVED:
                            entity.y--;
                            break;
                        case CANT_MOVE:
                            return false;
                        case TOUCHED_FOREIGN_LINE:
                            entity.y--;
                            playfieldLines.addAll( currentPoly.lines );
                            currentPoly=null;
                            break;
                    }
                }
                return true;
            }
            return false;
        }

        if ( entity.getCurrentLine().isVertical() )
        {
            // can only move up if not already at the top
            final int top = entity.getCurrentLine().minY();
            if ( entity.y > top) {
                entity.y--;
                return true;
            }
            // we're at the top, check whether there's a node
            // here that also has an UP line
            final Node topNode = entity.getCurrentLine().topNode();
            if ( topNode != null && topNode.up != null ) {
                entity.y--;
                entity.setCurrentLine(topNode.up);
            }
            return false;
        }
        // not on a vertical line, can only move up if the
        // current location is also a graph node that
        // has an UP line
        final Node node = entity.getCurrentLine().getNodeForEndpoint( entity.x, entity.y );
        if ( node != null && node.up != null ) {
            entity.y--;
            entity.setCurrentLine(node.up);
        }
        return false;
    }

    public Mode getMode(Mode wanted) {
        return currentPoly != null ? currentPoly.mode : wanted;
    }

    public boolean moveDown(Entity entity, Mode mode)
    {
        if ( mode == Mode.LINE_FAST )
        {
            if ( entity.y < PLAYFIELD_HEIGHT )
            {
                if ( this.currentPoly == null )
                {
                    // TODO: Check whether there's free space above us...
                    Node newNode = playfieldLines.split(entity.getCurrentLine(),entity.x, entity.y );
                    currentPoly = new IncompleteLineCollection(mode, entity.x, entity.y, Direction.DOWN, newNode) {
                        @Override
                        protected void currentLineChanged(Line line)
                        {
                            entity.setCurrentLine(line);
                        }
                    };
                    entity.y++;
                } else {
                    switch( currentPoly.tryMove(Direction.DOWN,this) )
                    {
                        case MOVED:
                            entity.y++;
                            break;
                        case CANT_MOVE:
                            return false;
                        case TOUCHED_FOREIGN_LINE:
                            entity.y++;
                            playfieldLines.addAll( currentPoly.lines );
                            currentPoly=null;
                            break;
                    }
                }
                return true;
            }
            return false;
        }

        if ( entity.getCurrentLine().isVertical() )
        {
            // can only move up if not already at the top
            final int bottom = entity.getCurrentLine().maxY();
            if ( entity.y < bottom ) {
                entity.y++;
                return true;
            }
            // we're at the bottom, check whether there's a node
            // here that also has a DOWN line
            final Node bottomNode = entity.getCurrentLine().bottomNode();
            if ( bottomNode != null && bottomNode.down != null )
            {
                entity.y++;
                entity.setCurrentLine(bottomNode.down);
                return true;
            }
            return false;
        }
        // not on a vertical line, can only move down if the
        // current location is also a graph node that
        // has a DOWN line
        final Node node = entity.getCurrentLine().getNodeForEndpoint( entity.x, entity.y );
        if ( node != null && node.down != null ) {
            entity.y++;
            entity.setCurrentLine(node.down);
            return true;
        }
        return false;
    }

    public boolean moveLeft(Entity entity, Mode mode)
    {
        if ( mode == Mode.LINE_FAST )
        {
            if ( entity.x > 0 )
            {
                if ( this.currentPoly == null )
                {
                    // TODO: Check whether there's free space below us...
                    Node newNode = playfieldLines.split(entity.getCurrentLine(),entity.x, entity.y );
                    currentPoly = new IncompleteLineCollection(mode, entity.x, entity.y, Direction.LEFT,newNode) {

                        @Override
                        protected void currentLineChanged(Line line)
                        {
                            entity.setCurrentLine(line);
                        }
                    };
                    entity.x--;
                } else {
                    switch( currentPoly.tryMove(Direction.LEFT,this) )
                    {
                        case MOVED:
                            entity.x--;
                            break;
                        case CANT_MOVE:
                            return false;
                        case TOUCHED_FOREIGN_LINE:
                            entity.x--;
                            playfieldLines.addAll( currentPoly.lines );
                            currentPoly=null;
                            break;
                    }
                }
                return true;
            }
            return false;
        }

        if ( entity.getCurrentLine().isHorizontal() )
        {
            // can only move up if not already at the left-most point
            final int left = entity.getCurrentLine().minX();
            if ( entity.x > left ) {
                entity.x--;
                return true;
            }
            // we're at the left-mode point, check whether there's a node
            // here that also has a LEFT line
            final Node leftNode = entity.getCurrentLine().leftNode();
            if ( leftNode != null && leftNode.left != null ) {
                entity.x--;
                entity.setCurrentLine(leftNode.left);
                return true;
            }
        }
        // not on a horizontal line, can only move left if the
        // current location is also a graph node that
        // has a LEFT line
        final Node node = entity.getCurrentLine().getNodeForEndpoint( entity.x, entity.y );
        if ( node != null && node.left != null ) {
            entity.x--;
            entity.setCurrentLine(node.left);
            return true;
        }
        return false;
    }

    public boolean moveRight(Entity entity, Mode mode)
    {
        if ( mode == Mode.LINE_FAST )
        {
            if ( entity.x < PLAYFIELD_WIDTH )
            {
                if ( this.currentPoly == null )
                {
                    // TODO: Check whether there's free space below us...
                    Node newNode = playfieldLines.split(entity.getCurrentLine(),entity.x, entity.y );
                    currentPoly = new IncompleteLineCollection(mode, entity.x, entity.y, Direction.RIGHT,newNode) {

                        @Override
                        protected void currentLineChanged(Line line)
                        {
                            entity.setCurrentLine(line);
                        }
                    };
                    entity.x++;
                } else {
                    switch( currentPoly.tryMove(Direction.RIGHT,this) )
                    {
                        case MOVED:
                            entity.x++;
                            break;
                        case CANT_MOVE:
                            return false;
                        case TOUCHED_FOREIGN_LINE:
                            entity.x++;
                            playfieldLines.addAll( currentPoly.lines );
                            currentPoly=null;
                            break;
                    }
                }
                return true;
            }
            return false;
        }

        if ( entity.getCurrentLine().isHorizontal() )
        {
            // can only move up if not already at the right-most point
            System.out.println("Moving right on "+entity.getCurrentLine());
            final int right = entity.getCurrentLine().maxX();
            if ( entity.x < right ) {
                entity.x++;
                return true;
            }
            // we're at the right-most point, check whether there's a node
            // here that also has a RIGHT line
            final Node rightNode = entity.getCurrentLine().rightNode();
            if ( rightNode != null && rightNode.right != null ) {
                entity.x++;
                entity.setCurrentLine(rightNode.right);
                return true;
            }
            return false;
        }
        // not on a horizontal line, can only move right if the
        // current location is also a graph node that
        // has a RIGHT line
        final Node node = entity.getCurrentLine().getNodeForEndpoint( entity.x, entity.y );
        if ( node != null && node.right != null ) {
            entity.x++;
            entity.setCurrentLine(node.right);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBorder(Line tmp)
    {
        return borderLines.containsIdentity(tmp);
    }

    @Override
    public Line getLine(int x, int y)
    {
        final Line result = playfieldLines.getLine(x,y);
        if ( result == null && currentPoly != null ) {
            return currentPoly.getLine(x,y );
        }
        return result;
    }

    @Override
    public Node split(Line line, int xSplit, int ySplit)
    {
        return playfieldLines.split(line,xSplit,ySplit);
    }

    @Override
    public Line intersects(int x0, int y0, int x1, int y1)
    {
        Line result = playfieldLines.intersects(x0,y0,x1,y1);
        if ( result == null && currentPoly != null ) {
            return currentPoly.intersects(x0,y0,x1,y1);
        }
        return result;
    }

    @Override
    public Line intersects(Line line)
    {
        Line result = playfieldLines.intersects(line);
        if ( result == null && currentPoly != null ) {
            return currentPoly.intersects(line);
        }
        return result;
    }

    public void draw(Graphics2D gfx)
    {
        drawBorder(gfx);
        drawAreas(gfx);

        final Line currentLine = player.getCurrentLine();
        gfx.setColor(Color.RED);
        currentLine.draw(gfx);
    }

    public void drawBorder(Graphics2D gfx)
    {
        final List<Line> lines = borderLines.lines;
        gfx.setColor(Color.BLUE);
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
        {
            final Line l = lines.get(i);
            l.draw(gfx);
        }
    }

    public void drawAreas(Graphics2D gfx)
    {
        final List<Line> lines = playfieldLines.lines;
        gfx.setColor(Color.WHITE);
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
        {
            final Line l = lines.get(i);
            l.draw(gfx);
        }
        if ( currentPoly != null )
        {
            currentPoly.draw(gfx);
        }
        gfx.drawString("Mode: "+getMode(Mode.MOVE),15,15);


    }
}