package de.codesourcery.quix;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A* path-finding algorithm.
 *
 * Implementation can operate on arbitrary {@link NavMesh} instances that
 * are responsible for locating reachable neighbour nodes and
 * providing the 'g' and 'h' metrics for the A* path-finding algorithm.
 *
 * @author tobias.gierke@code-sourcery.de
 */
public class AStar
{
    private static final boolean DEBUG = false;

    // max. number of nodes that may be visited
    // Exceeding this number will make the min-heap crash
    // as it uses a fixed-length array
    private static final int MAX_NODES = 65535;

    // max. number of nodes any node in the NavMesh may have
    private static final int MAX_NEIGHBOURS = 8;

    // Until Java has value objects, I'm using
    // a single int[] array to hold all node data
    // to avoid pointer-chasing/cache-misses
    private static final int NODE_DATA_SIZE = 5; // node ID,f,g,h

    // struct component IDs
    private static final int OFFSET_NODE_ID = 0;
    private static final int OFFSET_PARENT = 1;
    private static final int OFFSET_F = 2;
    private static final int OFFSET_G = 3;
    private static final int OFFSET_H = 4;

    // number of internal nodes allocated so far
    private int nodeCount;

    // node data
    private final int[] nodeData = new int[ MAX_NODES * NODE_DATA_SIZE ];

    public interface Spy
    {
        void visit(int extNodeId,int f,int g,int h,int parentExtNodeId);
    }

    private NavMesh navMesh;

    // close list
    // holds external IDs of visited nodes
    private final IntOpenHashSet closedList = new IntOpenHashSet();

    // open list
    // key is external node ID, value is internal node ID
    private final Int2IntArrayMap openList = new Int2IntArrayMap();
    private final MinHeap openListHeap = new MinHeap(1024);

    /**
     * Reset internal state before initiating
     * a new path search.
     */
    private void reset()
    {
        nodeCount = 0;
        closedList.clear();
        openList.clear();
        openListHeap.clear();
    }

    private String nodeToString(int intNodeId) {
        final int extId = getExternalNodeId( intNodeId );
        final int f = getF( intNodeId );
        final int g = getG( intNodeId );
        final int h = getH( intNodeId );
        final int parentIntId = getParent( intNodeId );
        return "Int. ID="+intNodeId+" -> Ext. ID="+extId+", "+
                navMesh.getCoordinates( extId )+" "+f+" = "+g+" + "+h+", parent="+parentIntId;
    }

    public List<Integer> findPath(int startNodeExtId,int dstNodeExtID, NavMesh navMesh, Spy spy)
    {
        this.navMesh = navMesh;
        reset();
        if ( startNodeExtId == dstNodeExtID ) {
            throw new IllegalArgumentException( "start = destination?" );
        }
        /*
         * 1. Add the starting square (or node) to the open list.
         */
        final int intNodeId = createInternalNode(startNodeExtId);
        int h = navMesh.calcH( startNodeExtId, dstNodeExtID );
        setFGH( intNodeId, h,0,h );
        setParent( intNodeId,-1 ); // mark this is as the first node
        putOnOpenList( intNodeId, startNodeExtId );

        final int[] neighbourCoords = new int[ MAX_NEIGHBOURS ];
        int currentSquareIntId;
        int previousSquareIntId=-1;

        /*
         * 2. Repeat the following:
         */
        while ( ! openListHeap.isEmpty() )
        {
            /*
             * A) Look for the lowest F cost square on the open list.
             * We refer to this as the current square.
             */
            currentSquareIntId = openListHeap.remove(); // current square
            final int currSquareExtId = getExternalNodeId( currentSquareIntId );

            if ( DEBUG && spy != null ) {
                // visit(int extNodeId,int f,int g,int h,int parentExtNodeId);
                spy.visit( currSquareExtId, getF( currentSquareIntId) ,
                        getG( currentSquareIntId) ,
                        getH( currentSquareIntId) ,
                        getParent( currentSquareIntId ) );
            }
            if ( currSquareExtId == dstNodeExtID ) {

                // we reached the destination
                final List<Integer> result = new ArrayList<>();
                result.add( currSquareExtId );
                do {
                    final int extId = getExternalNodeId( previousSquareIntId );
                    result.add( extId );
                    previousSquareIntId = getParent( previousSquareIntId );
                } while ( previousSquareIntId != -1 );
                Collections.reverse(result);
                return result;
            }

            openList.remove( currSquareExtId );

            /*
             * B). Switch it to the closed list.
             */
            closedList.add( currSquareExtId );

            /*
C) For each of the neighbours adjacent to the current square …

    If it is not walkable or if it is on the closed list, ignore it. Otherwise do the following.
             */
            final int neighbourCount = navMesh.getNeighbours( currSquareExtId, closedList,neighbourCoords);
            for ( int i = 0 ; i < neighbourCount ; i++ )
            {
                final int extId = neighbourCoords[i];
                if ( ! openList.containsKey( extId ) )
                {
                    // neighbour is walkable, not on the close list and not on the open list
                    /*
                        If it isn’t on the open list, add it to the open list.
                        Make the current square the parent of this square.
                        Record the F, G, and H costs of the square.
                     */
                    final int newIntNodeId = createInternalNode( extId );
                    int g = calcG( currentSquareIntId, newIntNodeId );
                    h = navMesh.calcH( extId , dstNodeExtID );
                    setFGH( newIntNodeId, g+h, g, h);
                    setParent( newIntNodeId, currentSquareIntId );
                    putOnOpenList( newIntNodeId,extId ); // do this last, depends on calculation of F
                } else {
                    // neighbour is walkable, not on the close list but on the open list
                    /*
    If it is on the open list already, check to see if this path to that square is better,
      using G cost as the measure.
      A lower G cost means that this is a better path.
      If so, change the parent of the square to the current square, and
      recalculate the G and F scores of the square.
      If you are keeping your open list sorted by F score,
      you may need to resort the list to account for the change.

                     */
                    final int childIntId = openList.get( extId );
                    final int oldG = getG( currentSquareIntId );
                    final int newG = getG( childIntId );
                    if ( newG < oldG ) {
                        setParent( childIntId, currentSquareIntId );
                        final int g = calcG( currentSquareIntId, childIntId );
                        h = getH( childIntId );
                        setFGH( childIntId, g+h, g, h);
                        openListHeap.remove( childIntId );
                        openListHeap.insert( childIntId );
                    }
                }
            }
            previousSquareIntId = currentSquareIntId;
        }
/*

D) Stop when you:

    Add the target square to the closed list, in which case the path has been found, or
    Fail to find the target square, and the open list is empty. In this case, there is no path.

3. Save the path. Working backwards from the target square, go from each square to its parent square until you reach the starting square. That is your path.
         */
        return Collections.emptyList();
    }

    private int getExternalNodeId(int intNodeId)
    {
        return nodeData[ intNodeId * NODE_DATA_SIZE + OFFSET_NODE_ID ];
    }

    private int calcG(int nodeAIdx, int nodeBIdx)
    {
        final int extNodeIdA = getExternalNodeId( nodeAIdx );
        final int extNodeIdB = getExternalNodeId( nodeBIdx );
        final int delta = navMesh.calcG( extNodeIdA, extNodeIdB );
        return getG(nodeAIdx) + delta;
    }

    private int getG(int intNodeId)
    {
        return nodeData[ intNodeId * NODE_DATA_SIZE + OFFSET_G ];
    }

    private int getH(int intNodeId)
    {
        return nodeData[ intNodeId * NODE_DATA_SIZE + OFFSET_H ];
    }

    private int getF(int intNodeId)
    {
        return nodeData[ intNodeId * NODE_DATA_SIZE + OFFSET_F ];
    }

    /**
     * Returns the internal node ID of another internal node's parent.
     * @param intNodeId
     * @return
     */
    private int getParent(int intNodeId)
    {
        return nodeData[ intNodeId * NODE_DATA_SIZE + OFFSET_PARENT ];
    }

    /**
     * Store a node on the open list.
     *
     * @param intNodeId
     * @param extNodeId
     */
    private void putOnOpenList(int intNodeId,int extNodeId)
    {
        openList.put(extNodeId,intNodeId);
        openListHeap.insert(intNodeId);
    }

    /**
     * Assign a new internal node to the given external node ID.
     * @param extNodeId
     * @return
     */
    private int createInternalNode(int extNodeId)
    {
        setExternalNodeId( nodeCount, extNodeId );
        return nodeCount++;
    }

    private void setExternalNodeId(int intNodeId, int extNodeId) {
        this.nodeData[ intNodeId * NODE_DATA_SIZE + OFFSET_NODE_ID] = extNodeId;
    }

    private void setFGH(int intNodeId,int f,int g,int h)
    {
        final int ptr = intNodeId * NODE_DATA_SIZE;
        this.nodeData[ ptr + OFFSET_F ] = f;
        this.nodeData[ ptr + OFFSET_G ] = g;
        this.nodeData[ ptr + OFFSET_H ] = h;
    }

    private void setParent(int childNodeIdx,int parentNodeIdx)
    {
        this.nodeData[ childNodeIdx * NODE_DATA_SIZE + OFFSET_PARENT ] = parentNodeIdx;
    }

    /*
     * Taken from https://www.geeksforgeeks.org/min-heap-in-java/
     * and bugfixed + adopted to store the internal node ID and
     * resolve the A* 'f' value dynamically.
     */
    final class MinHeap
    {
        private static final int FRONT = 1;

        private final int[] data;
        private final int maxsize;

        private int size;

        public MinHeap(int maxsize)
        {
            this.maxsize = maxsize;
            data = new int[this.maxsize + 1];
            clear();
        }

        public void clear() {
            this.size = 0;
            data[0] = Integer.MIN_VALUE;
        }

        private float f(int idx)
        {
            if ( idx == 0 ) {
                return Float.MIN_VALUE;
            }
            final int intNodeId = data[idx];
            return nodeData[intNodeId*NODE_DATA_SIZE + OFFSET_F];
        }

        private int parentIdx(int pos) { return pos / 2; }

        private int leftChildIdx(int pos) { return (2 * pos); }

        private int rightChildIdx(int pos) { return (2 * pos) + 1; }

        private boolean isLeaf(int pos)
        {
            return pos >= (size / 2) && pos <= size;
        }

        // Function to swap two nodes of the heap
        private void swap(int fpos, int spos)
        {
            final int tmp = data[fpos];
            data[fpos] = data[spos];
            data[spos] = tmp;
        }

        // Function to heapify the node at pos
        private void minHeapify(int pos)
        {
            // If the node is a non-leaf node and greater
            // than any of its child
            if ( ! isLeaf(pos) )
            {
                final float leftValue = f( leftChildIdx( pos ) );
                final float rightValue = f( rightChildIdx( pos ) );
                if ( f(pos) > leftValue || f(pos) > rightValue ) {

                    // Swap with the left child and heapify
                    // the left child
                    if ( leftValue < rightValue ) {
                        swap(pos, leftChildIdx(pos));
                        minHeapify(leftChildIdx(pos));
                    }
                    else
                    {
                        // Swap with the right child and heapify
                        // the right child
                        swap(pos, rightChildIdx(pos));
                        minHeapify(rightChildIdx(pos));
                    }
                }
            }
        }

        // Function to insert a node into the heap
        public void insert(int element)
        {
            if ((size+1) >= maxsize) {
                throw new IllegalStateException("min-heap full");
            }
            data[++size] = element;

            if ( size > 1 )
            {
                int current = size;
                while ( f( current ) < f( parentIdx( current ) ) )
                {
                    swap( current, parentIdx( current ) );
                    current = parentIdx( current );
                }
            }
        }

        // Function to remove and return the minimum
        // element from the heap
        public int remove()
        {
            int popped = data[FRONT];
            data[FRONT] = data[size--];
            if ( size > 1 )
            {
                minHeapify( FRONT );
            }
            return popped;
        }

        public void remove(int idx)
        {
            for ( int i = 1 ; i < size ; i++ )
            {
                if ( data[i] == idx )
                {
                    data[i] = data[size--];
                    if ( size > 1 )
                    {
                        minHeapify( i );
                    }
                }
            }
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }
}