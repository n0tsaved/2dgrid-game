package tilemap.jgrapht.alg.util;

import tilemap.GameMap;
import tilemap.jgrapht.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.GraphPathImpl;
import tilemap.jgrapht.util.FibonacciHeap;
import tilemap.jgrapht.util.FibonacciHeapNode;

import java.util.*;

/**
 * Created by notsaved on 3/26/17.
 */
public class Trailmax<V,E> implements Pathfinder<V,E>{

    private final Graph<V,E> graph;
    private FibonacciHeap<V> agentQueue, targetQueue;
    private Map<V, FibonacciHeapNode<V>> agentVertexToHeapNodeMap, targetVertexToHeapNodeMap;
    private Map<V, Double> agentGScoreMap, targetGScoreMap;
    private Map<V,E> cameFrom;
    private Set<V> agentClosed, targetClosed;
    private FibonacciHeapNode<V> last;
    private int expandedNodes;
    private long elapsedTime = Long.MAX_VALUE;
    private boolean verbose = false;

    private void initialize(){
        agentQueue = new FibonacciHeap<V>();
        targetQueue = new FibonacciHeap<V>();
        agentGScoreMap = new HashMap<V,Double>();
        targetGScoreMap = new HashMap<V, Double>();
        agentClosed = new HashSet<V>();
        targetClosed = new HashSet<V>();
        cameFrom = new HashMap<V, E>();
        agentVertexToHeapNodeMap = new HashMap<V, FibonacciHeapNode<V>>();
        targetVertexToHeapNodeMap = new HashMap<V, FibonacciHeapNode<V>>();
    }

    public Trailmax(Graph<V, E> graph) {
        this.graph = graph;

    }



    @Override
    public GraphPath<V, E> getShortestPath(V sourceVertex, V targetVertex, AStarAdmissibleHeuristic<V> admissibleHeuristic) {
        if(!graph.containsVertex(sourceVertex) || !graph.containsVertex(targetVertex))
            throw new IllegalArgumentException("Source or target vertex not contained in the graph!");

        initialize();
        agentGScoreMap.put(sourceVertex, 0.0);
        targetGScoreMap.put(targetVertex, 0.0);
        FibonacciHeapNode<V> sourceHeapNode=new FibonacciHeapNode<V>(sourceVertex);
        FibonacciHeapNode<V> targetHeapNode=new FibonacciHeapNode<V>(targetVertex);
        agentVertexToHeapNodeMap.put(sourceVertex, sourceHeapNode);
        targetVertexToHeapNodeMap.put(targetVertex, targetHeapNode);
        agentQueue.insert(sourceHeapNode, 0.0);
        targetQueue.insert(targetHeapNode, 0.0);
        long now = System.currentTimeMillis();
        do{

            FibonacciHeapNode<V> currentA = agentQueue.removeMin();
            FibonacciHeapNode<V> currentT = targetQueue.removeMin();
            expandAgent(currentA);
            expandTarget(currentT);
        }while(!agentClosed.containsAll(targetClosed) && !agentQueue.isEmpty() && !targetQueue.isEmpty());
        if(agentClosed.containsAll(targetClosed)) {
            elapsedTime = System.currentTimeMillis() - now;
            return this.buildGraphPath(targetVertex, last.getData(), last.getKey());
        }
        return null;
    }

    private void expandTarget(FibonacciHeapNode<V> currentNode) {
        if(agentVertexToHeapNodeMap.containsValue(currentNode)) { // if agent already expanded this vertex
            if (agentGScoreMap.get(currentNode.getData()) < targetGScoreMap.get(currentNode.getData())) {
                targetClosed.add(currentNode.getData());
            }
            return;
        }
        //System.out.println("robber search");
        expandedNodes++;
        Set<E> outgoingEdges = null;
        if (graph instanceof UndirectedGraph)
            outgoingEdges = graph.edgesOf(currentNode.getData());
        else if (graph instanceof DirectedGraph)
            outgoingEdges = ((DirectedGraph) graph).outgoingEdgesOf(currentNode.getData());

        for (E edge : outgoingEdges) {
            V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());
            if (successor == currentNode.getData()) //Ignore self-loops
                continue;

            double gScore_current = targetGScoreMap.get(currentNode.getData());
            double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
            if (targetVertexToHeapNodeMap.containsKey(successor)) { // We re-encountered a vertex. It's
                // either in the open or closed list.

                if(targetClosed.contains(successor)) continue;
                else { //it's in the Open queue

                    if (tentativeGScore >= targetGScoreMap.get(successor)) // Ignore path since it is
                        // non-improving
                        continue;

                    targetGScoreMap.put(successor, tentativeGScore);
                    cameFrom.put(successor, edge);
                    targetQueue.decreaseKey(targetVertexToHeapNodeMap.get(successor), tentativeGScore);

                }
            } else { // We've encountered a new vertex.
                targetGScoreMap.put(successor, tentativeGScore);
                cameFrom.put(successor, edge);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(successor);
                targetQueue.insert(heapNode, tentativeGScore);
                targetVertexToHeapNodeMap.put(successor, heapNode);
            }
        }
        targetClosed.add(currentNode.getData());
    }

    private void expandAgent(FibonacciHeapNode<V> currentNode) {
        //System.out.println("cop search");

        expandedNodes++;
        last=currentNode;
        Set<E> outgoingEdges = null;
        if (graph instanceof UndirectedGraph)
            outgoingEdges = graph.edgesOf(currentNode.getData());
        else if (graph instanceof DirectedGraph)
            outgoingEdges = ((DirectedGraph) graph).outgoingEdgesOf(currentNode.getData());

        for (E edge : outgoingEdges) {
            V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());
            if (successor == currentNode.getData()) //Ignore self-loops
                continue;

            double gScore_current = agentGScoreMap.get(currentNode.getData());
            double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
            if (agentVertexToHeapNodeMap.containsKey(successor)) { // We re-encountered a vertex. It's
                // either in the open or closed list.

                if (agentClosed.contains(successor)) continue;
                else {
                    if (tentativeGScore >= agentGScoreMap.get(successor)) // Ignore path since it is
                        // non-improving
                        continue;

                    agentGScoreMap.put(successor, tentativeGScore);
                    agentQueue.decreaseKey(agentVertexToHeapNodeMap.get(successor), tentativeGScore);
                    //cameFrom.put(successor, edge);

                }
            } else { // We've encountered a new vertex.
                //cameFrom.put(successor, edge);
                agentGScoreMap.put(successor, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(successor);
                agentQueue.insert(heapNode, tentativeGScore);
                agentVertexToHeapNodeMap.put(successor, heapNode);
            }
        }
        agentClosed.add(currentNode.getData());
    }

    public int getNumberOfExpandedNodes(){
        return expandedNodes;
    }

    @Override
    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public void setVerbose() {
        this.verbose = true;
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

    private List<E> buildPath(V currentNode){
        if(cameFrom.containsKey(currentNode)){
            List<E> path = buildPath(Graphs.getOppositeVertex(graph, cameFrom.get(currentNode), currentNode));
            path.add(cameFrom.get(currentNode));
            return path;
        }else
            return new ArrayList<E>();
    }
}
