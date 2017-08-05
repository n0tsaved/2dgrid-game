package tilemap.jgrapht.alg;

import tilemap.GameMap;
import tilemap.jgrapht.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.*;
import tilemap.jgrapht.util.FibonacciHeap;
import tilemap.jgrapht.util.FibonacciHeapNode;

import java.util.*;

/**
 * Created by notsaved on 10/26/16.
 */
public class LazyMovingTargetAdaptiveAStarShortestPath<V,E extends DefaultWeightedEdge> implements Pathfinder<V,E> {
    private final Graph<V,E> graph;
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
    private HashMap<V,Integer> search;
    private HashMap<Integer, Double> pathToCost;
    private HashMap<V,Double> hScoreMap;
    private int counter;
    private HashMap<V,V> nextstate;
    private HashMap<V,V> backstate;
    private HashMap<Integer, Double> deltah;
    private V start, goal;
    private GraphPath<V,E> path;
    private Set<E> updatedEdge;
    private boolean toBeUpdated=false;
    private long elapsedTime = Long.MAX_VALUE;
    private int numberOfExpandedNodes=0;
    private boolean verbose = true;
    public LazyMovingTargetAdaptiveAStarShortestPath(SimpleWeightedGraph<V, E> graph) {
        this.graph=graph;
    }

    public void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic){
        this.admissibleHeuristic =admissibleHeuristic;
        counter =0;
        openList = new FibonacciHeap<V>();
        vertexToHeapNodeMap=new HashMap<V, FibonacciHeapNode<V>>();
        closedList = new HashSet<V>();
        gScoreMap = new HashMap<>();
        hScoreMap = new HashMap<>();
        cameFrom=new HashMap<V,E>();
        deltah = new HashMap<>();
        search = new HashMap<V,Integer>();
        pathToCost = new HashMap<>();
        nextstate = new HashMap<V,V>();
        backstate = new HashMap<V,V>();
        numberOfExpandedNodes =0;
        path=null;
        for(V s : graph.vertexSet()) {
            search.put(s, 0);
            //gScoreMap.put(s,0.0);
            //hScoreMap.put(s,0.0);
            //backstate.put(s,null);
            //nextstate.put(s,null);
            //cameFrom.put(s, null);
        }
        deltah.put(1, 0.0);
    }

    protected void initializeState(V s){
        Integer current_search = search.get(s);
        if(current_search.equals(0)){
            hScoreMap.put(s,admissibleHeuristic.getCostEstimate(s,goal));
            gScoreMap.put(s, Double.POSITIVE_INFINITY);
        }else if(!current_search.equals(counter)){
            Double g_value = gScoreMap.get(s); if(g_value == null) g_value = Double.POSITIVE_INFINITY;
            Double h_value = hScoreMap.get(s);
            Double previous_pathcost = pathToCost.get(current_search);
            if( g_value + h_value < previous_pathcost)
                h_value = previous_pathcost - g_value;
                //hScoreMap.put(s,pathToCost.get(search.get(s)) - gScoreMap.get(s));
            h_value =  h_value - (deltah.get(counter) - deltah.get(search.get(s)));
            double newHvalue = admissibleHeuristic.getCostEstimate(s, goal);
            h_value = (h_value > newHvalue) ? h_value : newHvalue;
            hScoreMap.put(s, h_value);
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
        while(backstate.get(s)!=null) {
            aux = backstate.get(s);
            backstate.put(s,null);
            cameFrom.put(s,null);
            nextstate.put(aux,null);
            s = aux;
        }
    }

    private V parent(V s){
        if(cameFrom.containsKey(s))
            return Graphs.getOppositeVertex(graph, cameFrom.get(s), s);
        return null;
    }

    protected GraphPath<V, E> buildGraphPath(V startVertex, V targetVertex, double pathLength){
        List<E> edgeList = this.buildPath(targetVertex);
        return new GraphPathImpl<V, E>(graph, startVertex, targetVertex, edgeList, pathLength);
    }

    private List<E> buildPath(V currentNode){
        if(cameFrom.containsKey(currentNode) && cameFrom.get(currentNode)!=null){
            List<E> path = buildPath(Graphs.getOppositeVertex(graph, cameFrom.get(currentNode), currentNode));
            path.add(cameFrom.get(currentNode));
            return path;
        }else
            return new ArrayList<E>();
    }

    private boolean ComputePath(){
        //System.out.println("AA*");
        long now = System.currentTimeMillis();
        while(!openList.isEmpty()) {
            FibonacciHeapNode<V> currentNode = openList.removeMin();
            Set<E> outgoingEdges = null;
            if (graph instanceof UndirectedGraph)
                outgoingEdges = ((AbstractBaseGraph<V,E>) graph).edgesOf(currentNode.getData());
            else if (graph instanceof DirectedGraph)
                outgoingEdges = ((SimpleWeightedGraph<V,E>) graph).outgoingEdgesOf(currentNode.getData());

            if(currentNode.getData().equals(goal)){
                elapsedTime = System.currentTimeMillis() - now;
                path=this.buildGraphPath(start,goal,currentNode.getKey());
                if(verbose)
                    System.out.println("["+this.getClass()+"] source: "+start+" target: "+goal+" elapsed: "+elapsedTime+" expanded: "+numberOfExpandedNodes);
                return true;
            }
            numberOfExpandedNodes++;
            for(E edge : outgoingEdges){
                V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());
                if(successor.equals(currentNode.getData())) continue; //ignore self-loops

                initializeState(successor);
                double gScore_current = gScoreMap.get(currentNode.getData());
                double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
                double fScore = tentativeGScore + hScoreMap.get(successor);

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
        else restoreCoherence(targetVertex);
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
            cameFrom.clear();
            openList.insert(heapNode, CalculateKey(heapNode.getData()));
            vertexToHeapNodeMap.put(start, heapNode);
            if(!ComputePath())
                path=null;
            else pathToCost.put(counter, gScoreMap.get(goal));
            //System.out.println(counter+", "+pathToCost.get(counter));
            if(toBeUpdated)
                makeUpdates();
            //updateHValue();
            return path;
        }
        return null;
    }

    @Override
    public int getNumberOfExpandedNodes() {
        return numberOfExpandedNodes;
    }


    private void restoreCoherence(V newGoal){
        if(counter<1) return;
        if(!goal.equals(newGoal)) {
            initializeState(newGoal);
            Double g_score = gScoreMap.get(newGoal);
            Double h_score = hScoreMap.get(newGoal);
            if(g_score + h_score < pathToCost.get(counter))
                hScoreMap.put(newGoal, pathToCost.get(counter) - gScoreMap.get(newGoal));
            deltah.put(counter+1, deltah.get(counter) + hScoreMap.get(newGoal));
        }else deltah.put(counter+1, deltah.get(counter));
    }

    public long getElapsedTime(){
        return elapsedTime;
    }
}
