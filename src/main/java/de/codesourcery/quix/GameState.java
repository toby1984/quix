package de.codesourcery.quix;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameState implements ICollisionCheck
{
    public static final Dimension PLAYFIELD_SIZE = new Dimension(640 ,480 );

    public static final int ENEMY_RADIUS = 10;

    // enemies will never spawn closer than this
    public static final float MIN_ENEMY_SPAWN_DISTANCE_TO_PLAYER = ENEMY_RADIUS*3;

    public static final long RANDOM_SEED = 0x12345678;

    public final Random rnd = new Random(RANDOM_SEED);

    private Quix quix = new Quix();

    public boolean gameOver;

    // TODO: Remove debug code
    private final LineCollection lastPath = new LineCollection();

    private List<Poly> polys = new ArrayList<>();

    // hint: player location does NOT take playFieldOffset into consideration
    public Player player = new Player();

    public Difficulty difficulty;

    public static final int PLAYFIELD_WIDTH = PLAYFIELD_SIZE.width;
    public static final int PLAYFIELD_HEIGHT = PLAYFIELD_SIZE.height;

    private final List<Enemy> enemies = new ArrayList<>();

    public final LineCollection playfieldLines = new LineCollection();

    private IncompleteLineCollection currentPoly;

    private final AStar astar = new AStar();

    public GameState()
    {
        lastPath.lineColor = Color.ORANGE;
        restart();
    }

    public void restart()
    {
        Node.ALL_NODES.clear();

        lastPath.clear();
        gameOver = false;
        enemies.clear();
        playfieldLines.clear();
        currentPoly = null;
        difficulty = new Difficulty( 1 , 0 );
        player = new Player();
        quix = new Quix();
        rnd.setSeed( RANDOM_SEED );

        final Node nw = new Node(0,0);
        final Node ne = new Node( PLAYFIELD_WIDTH,0);
        final Node se = new Node( PLAYFIELD_WIDTH, PLAYFIELD_HEIGHT );
        final Node sw = new Node(0, PLAYFIELD_HEIGHT );

        final Line top = new Line(nw,ne );
        final Line right = new Line( ne,se );
        final Line bottom = new Line( se , sw );
        final Line left = new Line(sw,nw);

        nw.right = top;
        nw.down = left;

        ne.left = top;
        ne.down = right;

        se.up = right;
        se.left = bottom;

        sw.right = bottom;
        sw.up = left;
        playfieldLines.addAll( List.of( top,right,bottom,left ) );

        player.set(0,0);
        player.setCurrentLine(top);
        setupQuix(difficulty);

        for (int i = 0; i < difficulty.enemyCount ; i++ ) {
            spawnEnemy();
        }
    }

    public boolean isDrawingPoly() {
        return currentPoly != null;
    }

    public boolean isInCurrentlyDrawnPoly(Line line) {
        if ( isDrawingPoly() ) {
            if ( currentPoly.currentLine.equals( line ) ) {
                return true;
            }
            return currentPoly.contains( line );
        }
        return false;
    }

    public boolean move(Entity entity,Direction direction, Mode mode,boolean applyChanges)
    {
        if ( mode == Mode.LINE_FAST )
        {
            final boolean canMove;
            switch( direction ) {
                case LEFT:
                    canMove = entity.x > 0;
                    break;
                case RIGHT:
                    canMove = entity.x < PLAYFIELD_WIDTH;
                    break;
                case UP:
                    canMove = entity.y > 0;
                    break;
                case DOWN:
                    canMove = entity.y < PLAYFIELD_HEIGHT;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + direction);
            }
            if ( canMove )
            {
                if ( this.currentPoly == null )
                {
                    // TODO: Check whether there's free BEFORE splitting the line...
                    if ( applyChanges )
                    {
                        Node newNode = playfieldLines.split(entity.getCurrentLine(), entity.x, entity.y);
                        currentPoly = new IncompleteLineCollection(mode, direction, newNode)
                        {

                            @Override
                            protected void currentLineChanged(Line line)
                            {
                                entity.setCurrentLine(line);
                            }
                        };
                        entity.move(direction);
                    }
                } else {
                    switch( currentPoly.tryMove(direction,this, applyChanges) )
                    {
                        case MOVED:
                            if ( applyChanges )
                            {
                                entity.move(direction);
                            }
                            break;
                        case CANT_MOVE:
                            return false;
                        case TOUCHED_FOREIGN_LINE:
                            if ( applyChanges )
                            {
                                entity.move(direction);
                                closePoly();
                            }
                            break;
                    }
                }
                return true;
            }
            return false;
        }

        // check whether we're at the end of the current line
        final Node nodeForEndpoint = entity.getCurrentLine().getNodeForEndpoint(entity.x, entity.y);
        if ( nodeForEndpoint != null )
        {
            // we're at the end of the current line, check whether we can
            // continue moving in the current direction
            final Line exit = nodeForEndpoint.getExit(direction, null);
            if ( exit != null ) {
                if ( applyChanges )
                {
                    entity.move(direction);
                    entity.setCurrentLine(exit);
                }
                return true;
            }
            return false;
        }

        // check whether the user is trying to move along the current line
        if ( entity.getCurrentLine().isHorizontal() && ( direction == Direction.LEFT || direction == Direction.RIGHT ) )
        {
            if ( applyChanges )
            {
                entity.move(direction);
            }
            return true;
        }
        if ( entity.getCurrentLine().isVertical() && ( direction == Direction.UP || direction == Direction.DOWN ) )
        {
            if ( applyChanges )
            {
                entity.move(direction);
            }
            return true;
        }
        return false;
    }

    private void closePoly()
    {
        // TODO: Remove debug code
        lastPath.clear();
        lastPath.addAll( currentPoly.lines );
        try
        {
            final int firstNodeID = currentPoly.firstNode.id;
            final int lastNodeID = currentPoly.lastNode.id;
            System.out.println("SEARCHING PATH FROM "+currentPoly.firstNode+" -> "+currentPoly.lastNode);
            final List<Integer> path = astar.findPath( firstNodeID, lastNodeID, mesh, null );
            if ( path.size() < 2 ) {
                throw new RuntimeException("Internal error, found no path between nodes?");
            }

            System.out.println("FOUND PATH: " +path.stream().map(Node::get).map(Node::toString).collect(Collectors.joining(" -> ")));

            final List<Line> searchPath = searchPathToLines( path );
            Collections.reverse(searchPath);
            lastPath.addAll( searchPath );
            // TODO: Remove debug code

            final List<Poly> newPolys = lastPath.toPolygon().triangulate();

            // update area
            double sumArea = newPolys.stream().mapToDouble( x -> x.area() ).sum();
            player.area += (float) sumArea;

            // update score
            final int factor;
            switch( currentPoly.mode ) {
                case LINE_FAST:
                    factor = 1;
                    break;
                case LINE_SLOW:
                    factor = 2;
                    break;
                default:
                    throw new IllegalStateException( "Unexpected value: " + currentPoly.mode );
            }
            player.score += (int) (sumArea*factor);
            polys.addAll( newPolys );
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        playfieldLines.addAll( currentPoly.lines );
        currentPoly=null;
    }

    private List<Line> searchPathToLines(List<Integer> nodeIds)
    {
        final List<Line> result = new ArrayList<>();
        for ( int ptr = 0 ; ptr < nodeIds.size() ; ptr++ )
        {
            final Node current = Node.get( nodeIds.get(ptr) );
            if ( ptr+1 >= nodeIds.size() ) {
                break;
            }
            final Node next = Node.get( nodeIds.get(ptr+1) );
            Line currentLine = playfieldLines.findLine(current,next);
            if ( currentLine == null ) {
                // check current polygon
                currentLine = currentPoly.findLine( current,next );
                if ( currentLine == null )
                {
                    throw new RuntimeException( "Found no line for " + current + " -> " + next );
                }
            }
            result.add( currentLine );
        }
        return result;
    }


    public Mode getMode(Mode wanted) {
        return currentPoly != null ? currentPoly.mode : wanted;
    }

    @Override
    public boolean isBorder(Line tmp)
    {
        final Node endpoint = tmp.node0;
        return (tmp.isHorizontal() && ( endpoint.y == 0 || endpoint.y == PLAYFIELD_HEIGHT ) ) ||
        (tmp.isVertical() && ( endpoint.x == 0 || endpoint.x == PLAYFIELD_WIDTH ));
    }

    @Override
    public Line getLine(int x, int y)
    {
        Line result = playfieldLines.getLine( x, y );
        if ( result == null && currentPoly != null ) {
            result = currentPoly.getLine( x, y );
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

    public void tick()
    {
        if ( gameOver ) {
            return;
        }

        // animate quix
        quix.tick( this );

        if ( gameOver ) {
            return;
        }

        // animate enemies
outer:
        for ( int j = difficulty.enemySpeed ; j > 0 ; j-- )
        {
            for (int i = 0, enemiesSize = enemies.size(); i < enemiesSize; i++)
            {
                final Enemy enemy = enemies.get( i );
                if ( player.dst2( enemy ) < ENEMY_RADIUS * ENEMY_RADIUS )
                {
                    gameOver = true;
                    break outer;
                }
                if ( ! enemy.outOfBounds )
                {
                    enemy.tick( this );
                }
            }
        }
    }

    private Font gameOverFont;

    public void draw(Graphics2D gfx)
    {
        drawBorder(gfx);
        drawAreas(gfx);
        drawEnemies(gfx);
        quix.draw(gfx);

        final Line currentLine = player.getCurrentLine();
        gfx.setColor(Color.RED);
        currentLine.draw(gfx);

        if ( ! lastPath.isEmpty() ) {
            gfx.setColor( Color.ORANGE );
            lastPath.draw( gfx );
        }

        if ( ! polys.isEmpty() ) {
            for ( Poly p : polys ) {
                p.draw( gfx );
            }
        }

        drawPlayer(gfx);

        if ( gameOver )
        {
            final Font oldFont = gfx.getFont();
            try
            {
                if ( gameOverFont == null ) {
                    gameOverFont = gfx.getFont().deriveFont( Font.BOLD, 32f );
                }
                gfx.setColor( Color.RED );
                gfx.setFont( gameOverFont );
                gfx.drawString( "GAME OVER !!!", 100, 100 );
            }
            finally
            {
                gfx.setFont( oldFont );
            }
        }

        gfx.setColor( Color.BLACK );
        gfx.drawString("Entity @ "+player.x+","+player.y,20,20);
        gfx.drawString("Score: "+player.score, 20, 30);
        final float totalArea = PLAYFIELD_WIDTH * PLAYFIELD_HEIGHT;
        final float perc = (int) (100*(player.area/totalArea));
        gfx.drawString("Area: "+player.area+"( "+perc+"% of "+totalArea+")" ,20, 40);
    }

    private void drawPlayer(Graphics2D gfx) {
        // draw player
        final float radius = ENEMY_RADIUS;
        gfx.setColor( Color.BLUE );
        gfx.fillArc( player.x-(int) radius/2,
                     player.y - (int) radius/2 , (int) radius, (int) radius, 0,360 );
    }

    public void drawEnemies(Graphics2D gfx)
    {
        for (int i = 0, enemiesSize = enemies.size(); i < enemiesSize; i++)
        {
            final Enemy e = enemies.get( i );
            drawEnemy( e, gfx );
        }
    }

    private void drawEnemy(Enemy e, Graphics2D gfx)
    {
        int w = ENEMY_RADIUS;
        int h = ENEMY_RADIUS;
        gfx.setColor(Color.RED);
        gfx.fillArc( e.x - (w/2) , e.y - (h/2) , w, h, 0 , 360 );
        gfx.setColor( Color.BLACK );
        gfx.drawString( e.toString() , e.x , e.y );
    }

    public void drawBorder(Graphics2D gfx)
    {
        final List<Line> lines = playfieldLines.lines;
        gfx.setColor(Color.BLUE);
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
        {
            final Line l = lines.get(i);
            if ( isBorder( l ) )
            {
                l.draw( gfx );
            }
        }
    }

    public void spawnEnemy()
    {
        final Enemy enemy = new Enemy();

        final List<Line> candidates = playfieldLines.lines;
        do
        {
            final int idx = rnd.nextInt( candidates.size() );
            final Line l = candidates.get(idx);

            final int min,max;
            if ( l.isHorizontal() ) {
                min = l.leftNode().x;
                max = l.rightNode().x;
            } else if ( l.isVertical() ) {
                min = l.topNode().y;
                max = l.bottomNode().y;
            } else {
                throw new RuntimeException("Not a border (horizontal|vertical) line ? "+l);
            }
            int pos = min + rnd.nextInt(max-min);
            if ( l.isHorizontal() ) {
                enemy.set( pos , l.y0() );
                enemy.direction = rnd.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
            } else {
                enemy.set( l.x0(), pos );
                enemy.direction = rnd.nextBoolean() ? Direction.UP : Direction.DOWN;
            }

            // set current line AFTER adjusting the position
            // so we don't get "not on line" sanity check errors
            enemy.setCurrentLine( l );

            // don't spawn too close to player
        } while (enemy.dst2( player ) <= MIN_ENEMY_SPAWN_DISTANCE_TO_PLAYER*MIN_ENEMY_SPAWN_DISTANCE_TO_PLAYER );
        enemies.add( enemy );
    }

    public void drawAreas(Graphics2D gfx)
    {
        final List<Line> lines = playfieldLines.lines;
        gfx.setColor(Color.WHITE);
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++)
        {
            final Line l = lines.get(i);
            if ( ! isBorder(l) )
            {
                l.draw( gfx, true );
            }
        }
        if ( currentPoly != null )
        {
            currentPoly.draw(gfx, false,true);
        }
        gfx.drawString("Mode: "+getMode(Mode.MOVE),15,15);
    }

    public Node getClosestNode(Vec2 p) {

        Node result = null;
        float dst2 = 0;
        for ( Line l : playfieldLines.lines )
        {
            float dx = p.x - l.node0.x;
            float dy = p.y - l.node0.y;
            float tmp = dx*dx + dy*dy;
            if ( result == null || tmp < dst2 ) {
                result = l.node0;
                dst2 = tmp;
            }
            dx = p.x - l.node1.x;
            dy = p.y - l.node1.y;
            tmp = dx*dx + dy*dy;
            if ( tmp < dst2 ) {
                result = l.node1;
                dst2 = tmp;
            }
        }
        return result;
    }

    public Line getClosestLine(Vec2 p) {

        Line result = null;
        float distance = 0;
        for ( Line l : playfieldLines.lines )
        {
            float dist = LinAlg.distToLineSegment(l, p);
            if ( result == null || dist < distance  ) {
                result = l;
                distance = dist;
            }
        }
        return distance < 20 ? result : null;
    }

    private void setupQuix(Difficulty difficulty)
    {
        int p0x=50,p0y=50,p1x=75,p1y=25;
        int xStep = 5, yStep = 5;
        for ( int lines = difficulty.quixLineCount ; lines > 0 ; lines-- )
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

    public final NavMesh mesh = new AbstractNavMesh()
    {
        private int safeGet(int forbiddenId, int toTest) {
            if ( forbiddenId == toTest ) {
                throw new RuntimeException("Line has same start and end point ?");
            }
            return toTest;
        }

        @Override
        public int getNeighbours(int node, IntOpenHashSet visitedNodeIds, int[] result)
        {
            Node n = Node.get(node);
            System.out.println("Getting neightbours for "+n);

            int count=0;
            if ( n.up != null )
            {
                final Node other = n.up.getOther( n );
                if ( ! visitedNodeIds.contains( other.id ) && ! currentPoly.containsIdentity(n.up) )
                {
                    System.out.println( "Adding UP: " + other );
                    result[count++] = safeGet( n.id, other.id );
                }
            }
            if ( n.down != null ) {
                final Node other = n.down.getOther( n );
                if ( ! visitedNodeIds.contains( other.id ) && ! currentPoly.containsIdentity(n.down) )
                {
                    System.out.println( "Adding DOWN:" + other );
                    result[count++] = safeGet( n.id, other.id );
                }
            }
            if ( n.left!= null ) {
                final Node other = n.left.getOther( n );
                if ( ! visitedNodeIds.contains( other.id ) &&  ! currentPoly.containsIdentity(n.left) )
                {
                    System.out.println( "Adding LEFT: " + other );
                    result[count++] = safeGet( n.id, other.id );
                }
            }
            if ( n.right!= null )
            {
                final Node other = n.right.getOther( n );
                if ( ! visitedNodeIds.contains( other.id ) && ! currentPoly.containsIdentity(n.right))
                {
                    System.out.println( "Adding RIGHT: " + other );
                    result[count++] = safeGet( n.id, other.id );
                }
            }
            return count;
        }
    };

    public void movePlayer(boolean left, boolean right, boolean up, boolean down, Mode desiredMode)
    {
        final Mode mode = getMode( desiredMode );
        final Direction leftRight = left ? Direction.LEFT : Direction.RIGHT;
        final Direction upDown = up ? Direction.UP : Direction.DOWN;

        boolean canMoveSideways = false;
        boolean canMoveUpDown = false;

        if ( left || right )
        {
            canMoveSideways = move(player, leftRight, mode, false);
        }
        if ( up || down )
        {
            canMoveUpDown = move(player, upDown, mode, false);
        }
        Direction newDir = null;
        if ( ! isDrawingPoly() && player.previousMovement != null && ( left || right) && ( up || down ) )
        {
            // user tries to move left (or right) and up (or down) at the same time
            if ( player.previousMovement.isVerticalMovement() ) {
                // previously moved up or down
                if ( canMoveSideways ) {
                    newDir = leftRight;
                } else if ( canMoveUpDown ){
                    newDir = upDown;
                }
            } else if ( player.previousMovement.isHorizontalMovement() ) {
                // previously moved left or right
                if ( canMoveUpDown ){
                    newDir = upDown;
                } else if ( canMoveSideways ) {
                    newDir = leftRight;
                }
            } else {
                throw new RuntimeException("Unreachable code reached");
            }
        }
        else
        {
            // move in one direction only
            if ( left || right )
            {
                newDir = canMoveSideways ? leftRight : null;
            } else if ( up || down )
            {
                newDir = canMoveUpDown ? upDown : null;
            }
        }
        if ( newDir != null )
        {
            move(player, newDir, mode, true);
            player.previousMovement = newDir;
        }
    }

    public void markGameOver() {
        gameOver = true;
    }
}