package tilemap.jgrapht.alg;

import tilemap.Game;
import tilemap.jgrapht.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.DefaultWeightedEdge;
import tilemap.jgrapht.graph.GraphPathImpl;
import tilemap.jgrapht.graph.SimpleWeightedGraph;
import tilemap.jgrapht.util.FibonacciHeap;
import tilemap.jgrapht.util.FibonacciHeapNode;

import java.util.Set;
import tilemap.GameMap;
import java.util.*;
import tilemap.jgrapht.alg.interfaces.Pathfinder;

/**
 * Created by notsaved on 8/29/16.
 */
public class ThetaStarShortestPath<V extends Integer ,E>  {


    protected final SimpleWeightedGraph<V, E> graph;

    //List of open nodes
    protected FibonacciHeap<V> openList;
    protected Map<V, FibonacciHeapNode<V>> vertexToHeapNodeMap;
    //List of closed nodes
    protected Set<V> closedList;
    //Mapping of nodes to their g-scores (g(x)).
    protected Map<V, Double> gScoreMap;
    //Predecessor map: mapping of a node to an edge that leads to its predecessor on its shortest path towards the targetVertex
    protected Map<V,V> cameFrom;
    //Reference to the admissible heuristic
    protected AStarAdmissibleHeuristic<V> admissibleHeuristic;
    //Counter which keeps track of the number of expanded nodes
    protected int numberOfExpandedNodes;
    private long elapsedTime = Long.MAX_VALUE;

    private boolean verbose = false;

    public ThetaStarShortestPath(SimpleWeightedGraph<V, E> graph) {
        this.graph=graph;
    }

    protected void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic){
        this.admissibleHeuristic =admissibleHeuristic;
        openList = new FibonacciHeap<V>();
        vertexToHeapNodeMap=new HashMap<V, FibonacciHeapNode<V>>();
        closedList = new HashSet<V>();
        gScoreMap = new HashMap<V, Double>();
        cameFrom=new HashMap<V,V>();
        numberOfExpandedNodes =0;
    }

    public List<V> getShortestPath(V sourceVertex, V targetVertex, AStarAdmissibleHeuristic<V> admissibleHeuristic){
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
                elapsedTime= System.currentTimeMillis() - now;

                if(verbose)
                    System.out.println("["+this.getClass()+"] source: "+sourceVertex+" target: "+targetVertex+" elapsed: "+elapsedTime+" expanded: "+numberOfExpandedNodes);
                //Build the path
                return this.buildPath(targetVertex);
            }
            //We haven't reached the target vertex yet; expand the node
            expandNode(currentNode, targetVertex);
            closedList.add(currentNode.getData());
        } while(!openList.isEmpty());
        //No path exists from sourceVertex to TargetVertex
        return null;
    }


    protected void expandNode(FibonacciHeapNode<V> currentNode, V endVertex){
        boolean lof = false;
        numberOfExpandedNodes++;
        //System.out.println("THETA");

        Set<E> outgoingEdges=null;
        if(graph instanceof UndirectedGraph)
            outgoingEdges=graph.edgesOf(currentNode.getData());
        else if(graph instanceof DirectedGraph)
            outgoingEdges=((DirectedGraph)graph).outgoingEdgesOf(currentNode.getData());

        for(E edge : outgoingEdges){
            lof=false;
            V successor = Graphs.getOppositeVertex((Graph<V, E>) graph, edge, currentNode.getData());
            if (successor == currentNode.getData() || closedList.contains(successor)) //Ignore self-loops or nodes which have already been expanded
                continue;
            double gScore_current = (double) gScoreMap.get(currentNode.getData());
            double tentativeGScore =0;
            V parent =  cameFrom.get(currentNode.getData());
            if(parent!= null) lof=GameMap.lineOfSight((int) parent%GameMap.WIDTH, (int) parent/ GameMap.WIDTH, (int) successor%GameMap.WIDTH, (int) successor/GameMap.WIDTH);
            if(lof)
            {
                tentativeGScore=(double) gScoreMap.get(parent) +getDist(parent, successor);
                lof=true;
            }else{
                tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
                parent=currentNode.getData();

            }

            if (vertexToHeapNodeMap.containsKey(successor)) { // We re-encountered a vertex. It's
                // either in the open or closed list.
                if (tentativeGScore >= gScoreMap.get(successor)) // Ignore path since it is
                    // non-improving
                    continue;

                cameFrom.put(successor, parent);
                gScoreMap.put(successor, tentativeGScore);

                if (closedList.contains(successor)) { // it's in the closed list. Move node back to
                    // open list, since we discovered a shorter
                    // path to this node
                    closedList.remove(successor);
                    openList.insert(vertexToHeapNodeMap.get(successor), tentativeGScore);
                } else { // It's in the open list
                    openList.decreaseKey(vertexToHeapNodeMap.get(successor), tentativeGScore);
                }
            } else { // We've encountered a new vertex.
                cameFrom.put(successor, parent);
                gScoreMap.put(successor, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(successor);
                openList.insert(heapNode, tentativeGScore);
                vertexToHeapNodeMap.put(successor, heapNode);
            }
        }
    }

    /*protected GraphPath<V, E> buildGraphPath(V startVertex, V targetVertex, double pathLength){
         Graph<V,E> fakeGraph = new SimpleWeightedGraph<V, E>((Class<? extends E>) DefaultWeightedEdge.class);
        for(int i=0; i< GameMap.WIDTH*GameMap.HEIGHT; i++)
            fakeGraph.addVertex((V) new Integer(i));
        Integer currentNode = targetVertex;
        while(cameFrom.get(currentNode)!=null){
            System.out.println((V) graph.getEdgeSource(cameFrom.get(currentNode))+"," +currentNode);

            if(cameFrom.get(currentNode)!=null && !graph.getEdgeSource(cameFrom.get(currentNode)).equals(currentNode))
                if (!fakeGraph.containsEdge((V) graph.getEdgeSource(cameFrom.get(currentNode)), (V) currentNode))
                    fakeGraph.addEdge((V) graph.getEdgeSource(cameFrom.get(currentNode)), (V) currentNode);
            currentNode= (V) graph.getEdgeSource(cameFrom.get(currentNode));
        }
        List<E> edgeList = this.buildPath(targetVertex);
        return new GraphPathImpl<V, E>(fakeGraph, startVertex, targetVertex, edgeList, pathLength);

    }*/

    private List<V> buildPath(V currentNode){
        V current = currentNode;
        V next;
        ArrayList<V> list = new ArrayList<V>();
        while(current != null){
            list.add(current);
            current = cameFrom.get(current);
        }

        Collections.reverse(list);
        return list;
    }


    private double getDist(V sourceVertex, V targetVertex) {
        int sourceX, sourceY, targetX, targetY;
        sourceX=(int)sourceVertex%GameMap.WIDTH;
        sourceY=(int)sourceVertex/GameMap.WIDTH;
        targetX=(int)targetVertex%GameMap.WIDTH;
        targetY=(int)targetVertex/GameMap.WIDTH;
        //System.out.println("Source: [" +sourceX+","+sourceY+"] Target: ["+
        //        targetX+","+targetY+"] EstimatedCost: "+Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY));
        return Math.sqrt(Math.pow(sourceX - targetX,2)+Math.pow(sourceY - targetY,2));    }


    public void setVerbose(){
        this.verbose=true;
    }
}



