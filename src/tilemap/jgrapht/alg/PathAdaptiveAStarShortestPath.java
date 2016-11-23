package tilemap.jgrapht.alg;

import tilemap.GameMap;
import tilemap.jgrapht.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.util.FibonacciHeap;
import tilemap.jgrapht.util.FibonacciHeapNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by notsaved on 10/26/16.
 */
public class PathAdaptiveAStarShortestPath<V,E> extends AStarShortestPath<V,E> implements Pathfinder<V,E> {

    private HashMap<V,Integer> search;
    private HashMap<Integer, Double> pathToCost;
    private HashMap<V,Double> hScoreMap;
    private int counter=0;
    private HashMap<V,V> nextstate;
    private HashMap<V,V> backstate;
    private V start, goal;
    private GraphPath<V,E> path;
    private Set<E> updatedEdge;
    private boolean toBeUpdated=false;
    public PathAdaptiveAStarShortestPath(Graph<V, E> graph) {
        super(graph);
    }

    public void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic){
        this.admissibleHeuristic =admissibleHeuristic;
        openList = new FibonacciHeap<V>();
        vertexToHeapNodeMap=new HashMap<V, FibonacciHeapNode<V>>();
        closedList = new HashSet<V>();
        gScoreMap = new HashMap<V, Double>();
        hScoreMap = new HashMap<V, Double>();
        cameFrom=new HashMap<V,E>();
        search = new HashMap<V,Integer>();
        pathToCost = new HashMap<>();
        nextstate = new HashMap<V, V>();
        backstate = new HashMap<V,V>();
        numberOfExpandedNodes =0;
        path=null;
        for(V s : graph.vertexSet()) {
            search.put(s, 0);
            gScoreMap.put(s,0.0);
            hScoreMap.put(s,0.0);

        }
    }

    protected void initializeState(V s){
        if(search.get(s).equals(0)){
            hScoreMap.put(s,admissibleHeuristic.getCostEstimate(s,goal));
            gScoreMap.put(s, Double.POSITIVE_INFINITY);
        }else if(!search.get(s).equals(counter)){
            if( pathToCost.containsKey(search.get(s)) && gScoreMap.get(s) + hScoreMap.get(s) < pathToCost.get(search.get(s)))
                hScoreMap.put(s,pathToCost.get(search.get(s)) - gScoreMap.get(s));
            gScoreMap.put(s,Double.POSITIVE_INFINITY);
        }
        search.put(s,counter);
    }

    private void MakePath(V s){
        V aux;
        while(!s.equals(start)){
            aux = s;
            s = parent(s);
            nextstate.put(s,aux);
            backstate.put(aux,s);
            cameFrom.put(aux, graph.getEdge(s,aux));
        }

    }

    private void CleanPath(V s){
        V aux;
        while(backstate.containsKey(s)) {
            aux = backstate.get(s);
            backstate.remove(s);
            cameFrom.remove(s);
            nextstate.remove(aux);
            s = aux;
        }
    }

    private V parent(V s){
        if(cameFrom.containsKey(s))
            return Graphs.getOppositeVertex(graph, cameFrom.get(s), s);
        return null;
    }

    private boolean ComputePath(){
        while(!openList.isEmpty()) {
            FibonacciHeapNode<V> currentNode = openList.removeMin();
            numberOfExpandedNodes++;
            Set<E> outgoingEdges = null;
            if (graph instanceof UndirectedGraph)
                outgoingEdges = graph.edgesOf(currentNode.getData());
            else if (graph instanceof DirectedGraph)
                outgoingEdges = ((DirectedGraph) graph).outgoingEdgesOf(currentNode.getData());

            if(currentNode.getData().equals(goal) || nextstate.containsKey(currentNode.getData())){
                pathToCost.put(counter, gScoreMap.get(currentNode.getData()) + hScoreMap.get(currentNode.getData()));
                CleanPath(currentNode.getData());
                MakePath(currentNode.getData());
                path=this.buildGraphPath(start,goal,currentNode.getKey());
                return true;
            }
            for(E edge : outgoingEdges){
                V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());
                if(successor == currentNode.getData() || closedList.contains(successor)) continue;
                initializeState(successor);
                double gScore_current = gScoreMap.get(currentNode.getData());
                double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
                if(gScoreMap.get(successor) > tentativeGScore){
                    gScoreMap.put(successor, tentativeGScore);
                    cameFrom.put(successor, edge);
                }
                double fScore= CalculateKey(successor);

                if (!vertexToHeapNodeMap.containsKey(successor)) {
                    FibonacciHeapNode<V> heapNode=new FibonacciHeapNode<V>(successor);
                    openList.insert(heapNode, fScore);
                    vertexToHeapNodeMap.put(successor, heapNode);
                }else{
                    openList.decreaseKey(vertexToHeapNodeMap.get(successor), fScore);
                    //openList.delete(vertexToHeapNodeMap.get(successor));
                    //vertexToHeapNodeMap.remove(successor);
                }

            }
            closedList.add(currentNode.getData());

        }
        return false;
    }

    private void updateHValue(){
        for(V node : closedList)
            hScoreMap.put(node, gScoreMap.get(goal) - gScoreMap.get(node));
    }

    public void notifyUpdatedEdges(Set<E> updatedEdge){
        this.updatedEdge=updatedEdge;
        toBeUpdated=true;
    }
    private void makeUpdates(){

        for(E e : updatedEdge)
            if(backstate.containsKey(graph.getEdgeTarget(e)) && backstate.get(graph.getEdgeTarget(e)).equals(graph.getEdgeSource(e)) )
                CleanPath(graph.getEdgeTarget(e));
        toBeUpdated=false;
    }

    private double CalculateKey(V v){
        return hScoreMap.get(v) + gScoreMap.get(v);
    }

    @Override
    public GraphPath<V,E> getShortestPath(V sourceVertex, V targetVertex, AStarAdmissibleHeuristic<V> admissibleHeuristic) {
        if(!graph.containsVertex(sourceVertex) || !graph.containsVertex(targetVertex))
            throw new IllegalArgumentException("Source or target vertex not contained in the graph!");

        if(counter==0) this.initialize(admissibleHeuristic);
        numberOfExpandedNodes=0;
        start = sourceVertex;
        goal= targetVertex;
        path=null;
        if(!start.equals(goal)) {
            counter++;
            initializeState(start);
            initializeState(goal);
            gScoreMap.put(start, 0.0);
            FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<V>(start);
            openList.clear();
            closedList.clear();
            vertexToHeapNodeMap.clear();
            openList.insert(heapNode, CalculateKey(heapNode.getData()));
            vertexToHeapNodeMap.put(start, heapNode);
            if(!ComputePath())
                path=null;
            if(toBeUpdated)
                makeUpdates();
            //updateHValue();
            return path;
        }
        return null;
    }


}
