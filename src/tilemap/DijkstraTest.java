package tilemap;

import tilemap.jgrapht.Graph;
import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.DijkstraShortestPath;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by notsaved on 1/23/17.
 */
public class DijkstraTest extends Test {


    public DijkstraTest(SimpleWeightedGraph map, List<Point[]> points) {
        super(map, points);
    }

    @Override
    public void runStationaryTest() {
        GraphPath<Integer,DefaultEdge> path;
        long now;
        Integer next;
        for(Point[] p : points){
           /* elapsedTime.put(p, new LinkedList<>());
            expandedCells.put(p, new LinkedList<>());
            next= p[0].toNode();
            while(!next.equals(p[1].toNode())) {
                now = System.currentTimeMillis();
                pathfinder = new DijkstraShortestPath<Integer, DefaultEdge>(map.getGraph(), next, p[1].toNode());
                elapsedTime.get(p).add(System.currentTimeMillis() - now);
                expandedCells.get(p).add(pathfinder.getNumberOfExpandedNodes());
                path=pathfinder.getShortestPath(next,p[1].toNode(),new ManhattanDistance());
                next=Graphs.getOppositeVertex(map.getGraph(), path.getEdgeList().get(0), next);
            }*/
           next = p[0].toNode();
           now = System.currentTimeMillis();
           pathfinder= new DijkstraShortestPath<Integer, DefaultEdge>(map, next, p[1].toNode());
           //elapsedTime.put(p, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - now));
            elapsedTime.put(p, System.currentTimeMillis() - now);
            expandedCells.put(p,pathfinder.getNumberOfExpandedNodes());
        }
    }

    @Override
    public void runMovingTest() {
        long now;
        int count;
        int search;
        elapsedTime.clear();
        expandedCells.clear();
        GraphPath<Integer,DefaultEdge> agentPath=null;
        GraphPath<Integer,DefaultEdge> targetPath=null;
        Integer agentNode, targetNode;
        List<Integer> pathToFollow = null;
        for(Point[] p : points){
            count=0;
            search=0;
            agentNode = p[0].toNode();
            targetNode = p[1].toNode();
            LinkedList<Integer> movingExpCell = new LinkedList<>();
            LinkedList<Long> movingElapsTime = new LinkedList<>();
            while(!agentNode.equals(targetNode)){
                if(pathToFollow==null || !agentPath.getEndVertex().equals(targetNode)) {
                    now = System.currentTimeMillis();
                    pathfinder = new DijkstraShortestPath<Integer, DefaultEdge>(map, agentNode, targetNode);
                    movingElapsTime.add(System.currentTimeMillis() - now);
                    agentPath = pathfinder.getShortestPath(agentNode, targetNode, new OctileDistance());
                    movingExpCell.add(pathfinder.getNumberOfExpandedNodes());
                    search++;
                }
                Integer targetNext = null;
                Integer agentNext = null;
                //System.out.println(agentNode+","+targetNode);
                if(count%2==0) {
                    targetPath = new Trailmax<Integer,DefaultEdge>(map).getShortestPath(agentNode,targetNode,null);
                    pathToFollow = Graphs.getPathVertexList(targetPath);
                    if (!pathToFollow.isEmpty()) targetNext = pathToFollow.remove(0);
                    if (targetNext.equals(targetNode) && !pathToFollow.isEmpty()) targetNext = pathToFollow.remove(0);
                    targetNode = targetNext;
                }
                pathToFollow=Graphs.getPathVertexList(agentPath);
                if(!pathToFollow.isEmpty()){
                    int i = pathToFollow.lastIndexOf(agentNode);
                    agentNext=pathToFollow.remove(i+1);
                }
                    agentNode = agentNext;
                    count++;

            }
            pathToFollow=null;
            movingElapsedTime.put(p, movingElapsTime);
            movingExpandedCells.put(p, movingExpCell);
            movesMap.put(p, count);
            searchesMap.put(p, search);
        }
    }


}
