/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -------------------------
 * AStarShortestPath.java
 * -------------------------
 * (C) Copyright 2015-2015, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):
 *
 * Changes
 * -------
 * Aug-2015 : Initial version;
 *
 */
package tilemap.jgrapht.alg;

import tilemap.jgrapht.*;
import tilemap.jgrapht.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.GraphPathImpl;
import tilemap.jgrapht.graph.SimpleWeightedGraph;
import tilemap.jgrapht.util.FibonacciHeap;
import tilemap.jgrapht.util.FibonacciHeapNode;

import java.util.*;

/**
 * An implementation of <a
 * href="http://en.wikipedia.org/wiki/A*_search_algorithm">A* shortest
 * path algorithm</a>.
 * <a href="http://de.wikipedia.org/wiki/A*-Algorithmus"> A* shortest path algorithm german Wiki </a>.
 *
 * This class works for Directed and Undirected graphs, as well as Multi-Graphs and Mixed-Graphs. It's ok if the graph changes in between
 * invocations of the {@link #getShortestPath(Object, Object, AStarAdmissibleHeuristic)}  getShortestPath} method; no new instance of this class
 * has to be created.
 *
 * The heuristic is implemented using a FibonacciHeap data structure to maintain the set of open nodes. However, there still exist several approaches in
 * literature to improve the performance of this heuristic which one could consider to implement.
 * Another issue to take into consideration is the following: given to candidate nodes, i, j to expand, where f(i)=f(j), g(i)&gt;g(j), h(i)&lt;g(j), f(i)=g(i)+h(i),
 * g(i) is the actual distance from the source node to i, h(i) is the estimated distance from i to the target node. Usually a depth-first search
 * is desired, so ideally we would expand node i first. Using the FibonacciHeap, this is not necessarily the case though. This could be improved in a later version.
 *
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 *
 * @since Aug, 2015
 */
public class AStarShortestPath<V,E> implements Pathfinder<V,E> {

    protected final SimpleWeightedGraph<V, E> graph;

    //List of open nodes
    protected FibonacciHeap<V> openList;
    protected Map<V, FibonacciHeapNode<V>> vertexToHeapNodeMap;
    //List of closed nodes
    protected Set<V> closedList;
    //Mapping of nodes to their g-scores (g(x)).
    protected Map<V, Double> gScoreMap;
    //Predecessor map: mapping of a node to an edge that leads to its predecessor on its shortest path towards the targetVertex
    protected Map<V,E> cameFrom;
    //Reference to the admissible heuristic
    protected AStarAdmissibleHeuristic<V> admissibleHeuristic;
    //Counter which keeps track of the number of expanded nodes
    protected int numberOfExpandedNodes;
    private long elapsedTime = Long.MAX_VALUE;
    private boolean verbose=false;


    public AStarShortestPath(SimpleWeightedGraph<V, E> graph) {
        if(graph==null)
            throw new IllegalArgumentException("Graph cannot be null!");
        this.graph = graph;
    }

    /**
     * Initializes the data structures
     * @param admissibleHeuristic admissible heuristic
     */
    protected void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic){
        this.admissibleHeuristic =admissibleHeuristic;
        openList = new FibonacciHeap<V>();
        vertexToHeapNodeMap=new HashMap<V, FibonacciHeapNode<V>>();
        closedList = new HashSet<V>();
        gScoreMap = new HashMap<V, Double>();
        cameFrom=new HashMap<V,E>();
        numberOfExpandedNodes =0;
    }

    /**
     * Calculates (and returns) the shortest path from the sourceVertex to the targetVertex. Note: each time you invoke this method,
     * the path gets recomputed.
     * @param sourceVertex source vertex
     * @param targetVertex target vertex
     * @param admissibleHeuristic admissible heuristic which estimates the distance from a node to the target node.
     * @return the shortest path from sourceVertex to targetVertex
     */


    public GraphPath<V,E> getShortestPath(V sourceVertex, V targetVertex, AStarAdmissibleHeuristic<V> admissibleHeuristic){
        if(!graph.containsVertex(sourceVertex) || !graph.containsVertex(targetVertex))
            throw new IllegalArgumentException("Source or target vertex not contained in the graph!");

        this.initialize(admissibleHeuristic);
        gScoreMap.put(sourceVertex, 0.0);
        FibonacciHeapNode<V> heapNode=new FibonacciHeapNode<V>(sourceVertex);
        openList.insert(heapNode, 0.0);
        vertexToHeapNodeMap.put(sourceVertex, heapNode);
        long now = System.currentTimeMillis();
        do {
            FibonacciHeapNode<V> currentNode = openList.removeMin();
            //Check whether we reached the target vertex
            if (currentNode.getData().equals(targetVertex)){
                //Build the path
                elapsedTime= System.currentTimeMillis() - now;
                if(verbose)
                    System.out.println("["+this.getClass()+"] source: "+sourceVertex+" target: "+targetVertex+" elapsed: "+elapsedTime+" expanded: "+numberOfExpandedNodes);

                return this.buildGraphPath(sourceVertex, targetVertex, currentNode.getKey());
            }
            //We haven't reached the target vertex yet; expand the node
            expandNode(currentNode, targetVertex);
            closedList.add(currentNode.getData());
        } while(!openList.isEmpty());
        //No path exists from sourceVertex to TargetVertex
        return null;
    }

    protected void expandNode(FibonacciHeapNode<V> currentNode, V endVertex){
        numberOfExpandedNodes++;
        Set<E> outgoingEdges=null;
        if(graph instanceof UndirectedGraph)
            outgoingEdges=graph.edgesOf(currentNode.getData());
        else if(graph instanceof DirectedGraph)
            outgoingEdges=((DirectedGraph)graph).outgoingEdgesOf(currentNode.getData());


        for(E edge : outgoingEdges){
            V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());
            if (successor == currentNode.getData()) //Ignore self-loops
                continue;

            double gScore_current = gScoreMap.get(currentNode.getData());
            double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
            double fScore= tentativeGScore + admissibleHeuristic.getCostEstimate(successor, endVertex);
            if (vertexToHeapNodeMap.containsKey(successor)) { // We re-encountered a vertex. It's
                // either in the open or closed list.
                if (tentativeGScore >= gScoreMap.get(successor)) // Ignore path since it is
                    // non-improving
                    continue;

                cameFrom.put(successor, edge);
                gScoreMap.put(successor, tentativeGScore);

                if (closedList.contains(successor)) { // it's in the closed list. Move node back to
                    // open list, since we discovered a shorter
                    // path to this node
                    closedList.remove(successor);
                    openList.insert(vertexToHeapNodeMap.get(successor), fScore);
                } else { // It's in the open list
                    openList.decreaseKey(vertexToHeapNodeMap.get(successor), fScore);
                }
            } else { // We've encountered a new vertex.
                cameFrom.put(successor, edge);
                gScoreMap.put(successor, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(successor);
                openList.insert(heapNode, fScore);
                vertexToHeapNodeMap.put(successor, heapNode);
            }
        }
    }

    /**
     * Builds the graph path
     * @param startVertex starting vertex of the path
     * @param targetVertex ending vertex of the path
     * @param pathLength length of the path
     * @return the shortest path from startVertex to endVertex
     */
    protected GraphPath<V, E> buildGraphPath(V startVertex, V targetVertex, double pathLength){
        List<E> edgeList = this.buildPath(targetVertex);
        return new GraphPathImpl<V, E>(graph, startVertex, targetVertex, edgeList, pathLength);
    }

    /**
     * Recursive method which traces the path from the targetVertex to the startVertex. The method traces back the path
     * over the edges, so the method is safe to use for multi-graphs.
     * @param currentNode node
     * @return List of edges/arcs that constitutes the path
     */
    private List<E> buildPath(V currentNode){
        if(cameFrom.containsKey(currentNode)){
            List<E> path = buildPath(Graphs.getOppositeVertex(graph, cameFrom.get(currentNode), currentNode));
            path.add(cameFrom.get(currentNode));
            return path;
        }else
            return new ArrayList<E>();
    }

    /**
     * Returns how many nodes have been expanded in the A* search procedure in its last invocation. A node is expanded if it is removed from the open list.
     * @return number of expanded nodes
     */
    public int getNumberOfExpandedNodes(){
        return numberOfExpandedNodes;
    }

    public long getElapsedTime(){
        return elapsedTime;
    }

    @Override
    public void setVerbose() {
        this.verbose = true;
    }


}
