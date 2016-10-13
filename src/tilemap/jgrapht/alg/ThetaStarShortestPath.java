package tilemap.jgrapht.alg;

import tilemap.Game;
import tilemap.jgrapht.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.DefaultWeightedEdge;
import tilemap.jgrapht.graph.GraphPathImpl;
import tilemap.jgrapht.graph.SimpleWeightedGraph;
import tilemap.jgrapht.util.FibonacciHeapNode;

import java.util.Set;
import tilemap.GameMap;
import java.util.*;
import tilemap.jgrapht.alg.interfaces.Pathfinder;

/**
 * Created by notsaved on 8/29/16.
 */
public class ThetaStarShortestPath<V extends Integer ,E> extends AStarShortestPath {

    public ThetaStarShortestPath(Graph<V, E> graph) {
        super(graph);
    }



    public GraphPath<V,E> getShortestPath(V sourceVertex, V targetVertex, AStarAdmissibleHeuristic<V> admissibleHeuristic){
        if(!graph.containsVertex(sourceVertex) || !graph.containsVertex(targetVertex))
            throw new IllegalArgumentException("Source or target vertex not contained in the graph!");

        this.initialize(admissibleHeuristic);
        gScoreMap.put(sourceVertex, 0.0);
        FibonacciHeapNode<V> heapNode=new FibonacciHeapNode<V>(sourceVertex);
        openList.insert(heapNode, 0.0);
        vertexToHeapNodeMap.put(sourceVertex, heapNode);

        do {
            FibonacciHeapNode<V> currentNode = openList.removeMin();
            //Check whether we reached the target vertex
            if (currentNode.getData().equals(targetVertex)){
                //Build the path
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
        boolean lof = false;
        numberOfExpandedNodes++;
        //System.out.println("THETA");

        Set<E> outgoingEdges=null;
        if(graph instanceof UndirectedGraph)
            outgoingEdges=graph.edgesOf(currentNode.getData());
        else if(graph instanceof DirectedGraph)
            outgoingEdges=((DirectedGraph)graph).outgoingEdgesOf(currentNode.getData());
        V parent = null;

        for(E edge : outgoingEdges){
            lof=false;
            V successor = Graphs.getOppositeVertex((Graph<V, E>) graph, edge, currentNode.getData());
            if (successor == currentNode.getData() || closedList.contains(successor)) //Ignore self-loops or nodes which have already been expanded
                continue;
            double gScore_current = (double) gScoreMap.get(currentNode.getData());
            double tentativeGScore =0;
            E parentEdge = (E) cameFrom.get(currentNode.getData());
            if(parentEdge!= null) parent = (V) graph.getEdgeSource(parentEdge);
            if(parent!= null &&
                    GameMap.lineOfSight((int) parent%GameMap.WIDTH, (int) parent/ GameMap.WIDTH, (int) successor%GameMap.WIDTH, (int) successor/GameMap.WIDTH))
            {
                tentativeGScore=(double) gScoreMap.get(parent) +getDist(parent, successor);
                lof=true;
            }else{
                tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
                parent=currentNode.getData();

            }

            if(!vertexToHeapNodeMap.containsKey(successor) || tentativeGScore < (double) gScoreMap.get(successor)){

               // if(lof) cameFrom.put(successor,parentEdge);
                //else cameFrom.put(successor, edge);
                cameFrom.put(successor, edge);
                gScoreMap.put(successor, tentativeGScore);

                double fScore= tentativeGScore + admissibleHeuristic.getCostEstimate(successor, endVertex);
                if (!vertexToHeapNodeMap.containsKey(successor)) {
                    FibonacciHeapNode<V> heapNode=new FibonacciHeapNode<V>(successor);
                    openList.insert(heapNode, fScore);
                    vertexToHeapNodeMap.put(successor, heapNode);
                }else{
                    openList.decreaseKey((FibonacciHeapNode) vertexToHeapNodeMap.get(successor), fScore);
                }
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

    }

    private List<E> buildPath(V currentNode){
        if(cameFrom.containsKey(currentNode)){
            List<E> path = buildPath(Graphs.getOppositeVertex((Graph<V, E>) graph, (E) cameFrom.get(currentNode), (V) currentNode));
            path.add((E) cameFrom.get(currentNode));
            return path;
            }else
                return new ArrayList<E>();
    }
*/

    private double getDist(V sourceVertex, V targetVertex) {
        int sourceX, sourceY, targetX, targetY;
        sourceX=(int)sourceVertex%GameMap.WIDTH;
        sourceY=(int)sourceVertex/GameMap.WIDTH;
        targetX=(int)targetVertex%GameMap.WIDTH;
        targetY=(int)targetVertex/GameMap.WIDTH;
        //System.out.println("Source: [" +sourceX+","+sourceY+"] Target: ["+
        //        targetX+","+targetY+"] EstimatedCost: "+Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY));
        return Math.sqrt(Math.pow(sourceX - targetX,2)+Math.pow(sourceY - targetY,2));    }


}



