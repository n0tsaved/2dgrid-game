package tilemap;

import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.MovingTargetAdaptiveAStarShortestPath;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by notsaved on 4/1/17.
 */
public class AdaptiveAStarTest extends Test {
    public AdaptiveAStarTest(SimpleWeightedGraph map, List<Point[]> points) {
        super(map, points);
    }

    @Override
    public void runStationaryTest() {

    }

    @Override
    public void runMovingTest() {
        long now;
        int count, search;
        elapsedTime.clear();
        expandedCells.clear();
        GraphPath<Integer,DefaultEdge> agentPath=null;
        GraphPath<Integer,DefaultEdge> targetPath=null;
        Integer agentNode, targetNode;
        List<Integer> pathToFollow = null;
        pathfinder = new MovingTargetAdaptiveAStarShortestPath<>(map);
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
                    agentPath = pathfinder.getShortestPath(agentNode, targetNode, new OctileDistance());
                    movingElapsTime.add(System.currentTimeMillis() - now);
                    movingExpCell.add(pathfinder.getNumberOfExpandedNodes());
                    search++;
                }
                Integer targetNext = null;
                Integer agentNext = null;
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

