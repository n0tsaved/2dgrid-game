package tilemap.jgrapht.alg;

import tilemap.GameMap;
import tilemap.jgrapht.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.GraphPathImpl;
import tilemap.jgrapht.util.FibonacciHeap;
import tilemap.jgrapht.util.FibonacciHeapNode;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by notsaved on 1/16/17.
 */
public class BidirectionalAStarShortestPath<V,E>  implements Pathfinder<V,E> {

    protected final Graph<V, E> graph;

    //List of open nodes
    protected FibonacciHeap<V> forwardOpenList, backwardOpenList;
    protected Map<V, FibonacciHeapNode<V>> forwardVertexToHeapNodeMap, backwardVertexToHeapNodeMap;
    //List of closed nodes
    protected Set<V> forwardClosedList, backwardClosedList;
    //Mapping of nodes to their g-scores (g(x)).
    protected Map<V, Double> forwardGScoreMap, backwardGScoreMap;
    //Predecessor map: mapping of a node to an edge that leads to its predecessor on its shortest path towards the targetVertex
    protected Map<V,E> forwardCameFrom, backwardCameFrom;
    //Reference to the admissible heuristic
    protected AStarAdmissibleHeuristic<V> admissibleHeuristic;
    //Counter which keeps track of the number of expanded nodes
    protected int forwardRun, backwardRun;

    protected V touchNode;

    protected double bestPathLength;
    private long elapsedTime = Long.MAX_VALUE;

    protected boolean verbose = true;

    public BidirectionalAStarShortestPath(Graph<V, E> graph) {
        if(graph==null)
            throw new IllegalArgumentException("Graph cannot be null!");
        this.graph = graph;
    }

    protected void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic){
        this.admissibleHeuristic =admissibleHeuristic;
        forwardOpenList = new FibonacciHeap<V>();
        backwardOpenList = new FibonacciHeap<V>();
        forwardVertexToHeapNodeMap=new HashMap<V, FibonacciHeapNode<V>>();
        backwardVertexToHeapNodeMap = new HashMap<V, FibonacciHeapNode<V>>();
        forwardClosedList = new HashSet<V>();
        backwardClosedList = new HashSet<V>();
        forwardGScoreMap = new HashMap<V, Double>();
        backwardGScoreMap = new HashMap<V, Double>();
        forwardCameFrom = new HashMap<V,E>();
        backwardCameFrom = new HashMap<V, E>();
        touchNode = null;
        bestPathLength = Double.POSITIVE_INFINITY;
        forwardRun = backwardRun =0;
    }

    @Override
    public GraphPath<V, E> getShortestPath(V sourceVertex, V targetVertex, AStarAdmissibleHeuristic<V> admissibleHeuristic) {
        //System.out.println(sourceVertex+","+targetVertex);
        if(!graph.containsVertex(sourceVertex) || !graph.containsVertex(targetVertex)) {
            throw new IllegalArgumentException("Source or target vertex not contained in the graph! " + sourceVertex + "," + targetVertex);
        }
        if(sourceVertex.equals(targetVertex)) return null;
        this.initialize(admissibleHeuristic);
        forwardGScoreMap.put(sourceVertex, 0.0);
        backwardGScoreMap.put(targetVertex, 0.0);
        FibonacciHeapNode<V> sourceHeapNode=new FibonacciHeapNode<V>(sourceVertex);
        FibonacciHeapNode<V> targetHeapNode = new FibonacciHeapNode<V>(targetVertex);
        forwardOpenList.insert(sourceHeapNode, 0.0);
        backwardOpenList.insert(targetHeapNode, 0.0);
        forwardVertexToHeapNodeMap.put(sourceVertex, sourceHeapNode);
        backwardVertexToHeapNodeMap.put(targetVertex, targetHeapNode);

        long now = System.currentTimeMillis();
        while(!forwardOpenList.isEmpty() && !backwardOpenList.isEmpty()){
            if(touchNode != null){

                double forwardDistance = forwardOpenList.min().getKey();
                double backwardDistance = backwardOpenList.min().getKey();

                if(bestPathLength <= ((forwardDistance >= backwardDistance) ? forwardDistance : backwardDistance)){
                    //System.out.println("forward: "+forwardRun+", backward: "+backwardRun);
                    elapsedTime = System.currentTimeMillis() - now;
                    if(verbose)
                        System.out.println("["+this.getClass()+"] source: "+sourceVertex+" target: "+targetVertex+" elapsed: "+elapsedTime+" expanded: "+getNumberOfExpandedNodes());
                    //GameMap.data[(Integer) touchNode%GameMap.WIDTH][(Integer) touchNode/GameMap.WIDTH].isFrontier = true;
                    return buildGraphPath(sourceVertex, targetVertex);}
            }

            if(forwardOpenList.size() <= backwardOpenList.size())
                expandForwardFrontier(targetVertex);
            else expandBackwardFrontier(sourceVertex);
        }
        return null;
    }

    @Override
    public int getNumberOfExpandedNodes() {
        return forwardRun+backwardRun;
    }

    private void expandForwardFrontier(V endVertex) {
        forwardRun++;

        FibonacciHeapNode<V> currentNode = forwardOpenList.removeMin();

        forwardClosedList.add(currentNode.getData());

        Set<E> outgoingEdges=null;
        if(graph instanceof UndirectedGraph)
            outgoingEdges=graph.edgesOf(currentNode.getData());
        else if(graph instanceof DirectedGraph)
            outgoingEdges=((DirectedGraph)graph).outgoingEdgesOf(currentNode.getData());

        for(E edge : outgoingEdges) {
            V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());
            if (successor == currentNode.getData()) //Ignore self-loops or nodes which have already been expanded
                continue;

            double gScore_current = forwardGScoreMap.get(currentNode.getData());
            double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
            double fScore = tentativeGScore + admissibleHeuristic.getCostEstimate(successor, endVertex);

            if(forwardVertexToHeapNodeMap.containsKey(successor)){

                if(tentativeGScore >= forwardGScoreMap.get(successor))
                    continue;

                forwardCameFrom.put(successor, edge);
                forwardGScoreMap.put(successor, tentativeGScore);

                if(forwardClosedList.contains(successor)){
                    forwardClosedList.remove(successor);
                    forwardOpenList.insert(forwardVertexToHeapNodeMap.get(successor), fScore);
                }else{
                    forwardOpenList.decreaseKey(forwardVertexToHeapNodeMap.get(successor), fScore);
                }
                updateForwardFrontier(successor, tentativeGScore);
            }else{
                forwardCameFrom.put(successor, edge);
                forwardGScoreMap.put(successor, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<V>(successor);
                forwardOpenList.insert(heapNode,fScore);
                forwardVertexToHeapNodeMap.put(successor, heapNode);
                updateForwardFrontier(successor, tentativeGScore);
            }
        }
        //forwardClosedList.add(currentNode.getData());
    }

    private void updateForwardFrontier(V node, double nodeScore) {
        if(backwardClosedList.contains(node)){
            double pathLength = backwardGScoreMap.get(node) + nodeScore;
            bestPathLength = (bestPathLength < pathLength) ? bestPathLength : pathLength;
            touchNode = node;
        }
    }

    private void expandBackwardFrontier(V sourceVertex) {
        backwardRun++;

        FibonacciHeapNode<V> currentNode = backwardOpenList.removeMin();

        backwardClosedList.add(currentNode.getData());

        Set<E> outgoingEdges=null;
        if(graph instanceof UndirectedGraph)
            outgoingEdges=graph.edgesOf(currentNode.getData());
        else if(graph instanceof DirectedGraph)
            outgoingEdges=((DirectedGraph)graph).outgoingEdgesOf(currentNode.getData());

        for(E edge : outgoingEdges) {
            V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());
            if (successor == currentNode.getData()) //Ignore self-loops or nodes which have already been expanded
                continue;

            double gScore_current = backwardGScoreMap.get(currentNode.getData());
            double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
            double fScore = tentativeGScore + admissibleHeuristic.getCostEstimate(successor, sourceVertex);

            if(backwardVertexToHeapNodeMap.containsKey(successor)){

                if(tentativeGScore >= backwardGScoreMap.get(successor))
                    continue;

                backwardCameFrom.put(successor, edge);
                backwardGScoreMap.put(successor, tentativeGScore);

                if(backwardClosedList.contains(successor)){
                    backwardClosedList.remove(successor);
                    backwardOpenList.insert(backwardVertexToHeapNodeMap.get(successor), fScore);
                }else{
                    backwardOpenList.decreaseKey(backwardVertexToHeapNodeMap.get(successor), fScore);
                }
                updateBackwardFrontier(successor, tentativeGScore);
            }else{
                backwardCameFrom.put(successor, edge);
                backwardGScoreMap.put(successor, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<V>(successor);
                backwardOpenList.insert(heapNode,fScore);
                backwardVertexToHeapNodeMap.put(successor, heapNode);
                updateBackwardFrontier(successor, tentativeGScore);
            }
        }
      //  backwardClosedList.add(currentNode.getData());
    }

    private void updateBackwardFrontier(V node, double nodeScore) {
        if(forwardClosedList.contains(node)){
            double pathLength = forwardGScoreMap.get(node) + nodeScore;

            bestPathLength = (bestPathLength < pathLength) ? bestPathLength : pathLength;
            touchNode = node;
        }
    }

    private GraphPath<V,E> buildGraphPath(V sourceVertex, V targetVertex) {
        //List<E> edgelistA = buildForwardPath(targetVertex);
        //List<E> edgelistB = buildBackwardPath(sourceVertex);
        //Collections.reverse(edgelistB);
        //edgelistA.addAll(edgelistB);
        //List<E> edgelist = edgelistA;
        List<E> edgelist = new ArrayList<E>();
        V node = touchNode;
        E current = forwardCameFrom.get(node);

        while(current != null){
            edgelist.add(current);
            node = Graphs.getOppositeVertex(graph, current, node);
            current = forwardCameFrom.get(node);
        }

        Collections.<E>reverse(edgelist);
        if(backwardCameFrom != null){
            node = touchNode;
            current = backwardCameFrom.get(node);
            while(current!=null) {
                edgelist.add(current);
                node = Graphs.getOppositeVertex(graph, current, node);
                current = backwardCameFrom.get(node);
            }
        }
        return new GraphPathImpl<V, E>(graph, sourceVertex, targetVertex, edgelist, bestPathLength);
    }

    public long getElapsedTime(){
        return elapsedTime;
    }
}
