package de.codesourcery.quix;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
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
        setupQuix();

        for (int i = 0; i < difficulty.enemyCount ; i++ ) {
            spawnEnemy();
        }
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
                    currentPoly = new IncompleteLineCollection( mode, Direction.UP, newNode) {

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
                            closePoly();
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
                return true;
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
            return true;
        }
        return false;
    }

    private void closePoly()
    {
//        final List<Integer> path =
//            astar.findPath(currentPoly.firstNode.id, currentPoly.lastNode.id, mesh, null);
//
//        if ( path.size() < 2 ) {
//            throw new RuntimeException("Internal error, found no path between nodes?");
//        }
//
//        System.out.println("FOUND PATH: " +path.stream().map(Node::get).map(Node::toString).collect(Collectors.joining(" -> ")));
//
//        // TODO: Remove debug code
//        lastPath.clear();
//        lastPath.addAll( currentPoly.lines );
//        lastPath.addAll( toLines( path ) );
//        // TODO: Remove debug code

        playfieldLines.addAll( currentPoly.lines );
        currentPoly=null;
    }

    private List<Line> toLines(List<Integer> nodeIds)
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
                throw new RuntimeException("Found no line for "+current+" -> "+next);
            }
            result.add( currentLine );
        }
        return result;
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
                    // TODO: Check whether there's free space below us...
                    Node newNode = playfieldLines.split(entity.getCurrentLine(),entity.x, entity.y );
                    currentPoly = new IncompleteLineCollection( mode, Direction.DOWN, newNode) {
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
                            closePoly();
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
                    currentPoly = new IncompleteLineCollection( mode, Direction.LEFT, newNode) {

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
                            closePoly();
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
                    currentPoly = new IncompleteLineCollection( mode, Direction.RIGHT, newNode) {

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
                            closePoly();
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
        drawPlayer(gfx);
        drawEnemies(gfx);
        quix.draw(gfx);

        final Line currentLine = player.getCurrentLine();
        gfx.setColor(Color.RED);
        currentLine.draw(gfx);

        gfx.drawString("Entity @ "+player.x+","+player.y,20,20);

        if ( ! lastPath.isEmpty() ) {
            gfx.setColor( Color.ORANGE );
            lastPath.draw( gfx );
        }

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
                l.draw( gfx );
            }
        }
        if ( currentPoly != null )
        {
            currentPoly.draw(gfx);
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

    private void setupQuix()
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

    public final NavMesh mesh = new AbstractNavMesh()
    {
        @Override
        public int getNeighbours(int node, IntOpenHashSet visitedNodeIds, int[] result)
        {
            Node n = Node.get(node);
            int count=0;
            if ( n.up != null && ! currentPoly.containsIdentity(n.up ) ) {
                result[count++] = n.up.getOther(n).id;
            }
            if ( n.down != null && ! currentPoly.containsIdentity(n.down )) {
                result[count++] = n.down.getOther(n).id;
            }
            if ( n.left!= null && ! currentPoly.containsIdentity(n.left )) {
                result[count++] = n.left.getOther(n).id;
            }
            if ( n.right!= null && ! currentPoly.containsIdentity(n.right )) {
                result[count++] = n.right.getOther(n).id;
            }
            return count;
        }
    };
}