package de.codesourcery.quix;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/**
 * Navigation node graph.
 *
 * Nodes are assumed to each have a unique integer ID and no two nodes
 * with the same coordinates exist.
 * @author tobias.gierke@code-sourcery.de
 */
public interface NavMesh
{
    /**
     * DEBUG ONLY, REMOVE WHEN DONE.
     *
     * @param nodeId
     * @return
     */
    Vec2 getCoordinates(int nodeId);

    /**
     * Heuristic function that estimates the cost of reaching
     * a certain destination node from some start node.
     *
     * @param nodeStart ID of start node
     * @param nodeEnd ID of destination node
     * @return cost
     */
    int calcH(int nodeStart, int nodeEnd);

    /**
     * Calculates the distance between two nodes.
     * @param nodeA
     * @param nodeB
     * @return cost
     */
    int calcG(int nodeA, int nodeB);

    /**
     * Returns all neighbours for a given node that are walkable
     * and NOT on the close list.
     *
     * @param node ID of node to check neighbours of
     * @param visitedNodeIds Close list containing IDs of already visited nodes
     * @param result array where to store neighbour node IDs in
     * @return number of neighbour node IDs written to the result list
     */
    int getNeighbours(int node, IntOpenHashSet visitedNodeIds, int[] result);
}
