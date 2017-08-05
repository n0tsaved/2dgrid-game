package tilemap;

import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.AStarShortestPath;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.DefaultWeightedEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by notsaved on 3/14/17.
 */
public class AStarTest extends Test {


    public AStarTest(SimpleWeightedGraph map, List<Point[]> points) {
        super(map, points);
    }

    @Override
    public void runStationaryTest() {
        GraphPath<Integer,DefaultWeightedEdge> path;
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
            pathfinder= new AStarShortestPath<>(map);
            pathfinder.getShortestPath(next,p[1].toNode(), new OctileDistance());
            elapsedTime.put(p, pathfinder.getElapsedTime());
            expandedCells.put(p,pathfinder.getNumberOfExpandedNodes());
        }
    }

    @Override
    public void runMovingTest() {
        //System.out.println("Performing A* Moving Target Test");
        long now;
        int count, search;
        elapsedTime.clear();
        expandedCells.clear();
        GraphPath<Integer,DefaultWeightedEdge> agentPath=null;
        GraphPath<Integer,DefaultWeightedEdge> targetPath=null;
        //Integer agentNode, targetNode;
        List<Integer> pathToFollow = null;
        for(Point[] p : points){



            count=0;
            search=0;
            agentNode = p[0].toNode();
            targetNode = p[1].toNode();
            LinkedList<Integer> movingExpCell = new LinkedList<>();
            LinkedList<Long> movingElapsTime = new LinkedList<>();
            pathfinder = new AStarShortestPath<>(map);
            targetThread r = new targetThread();
            evadeThread = new Thread(r);

            evadeThread.start();
            while(!agentNode.equals(targetNode)){
                //System.out.println(agentNode+", "+targetNode);
                if(pathToFollow==null ||((agentPath!=null) && !agentPath.getEndVertex().equals(targetNode))) {
                    agentPath = pathfinder.getShortestPath(agentNode, targetNode, new OctileDistance());
                    if(pathfinder.getElapsedTime() > Long.valueOf(1900))
                        continue;
                    movingElapsTime.add(pathfinder.getElapsedTime());
                    movingExpCell.add(pathfinder.getNumberOfExpandedNodes());
                    search++;
                }
                Integer targetNext = null;
                Integer agentNext = null;
                if(count%2==0) {

                    synchronized(moveTarget) {
                        moveTarget = new Boolean(true);
                    }

                    try {
                        Thread.sleep(12);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    /*targetPath = new Trailmax<Integer,DefaultWeightedEdge>(map).getShortestPath(agentNode,targetNode,null);
                    pathToFollow = Graphs.getPathVertexList(targetPath);
                    if (!pathToFollow.isEmpty()) targetNext = pathToFollow.remove(0);
                    if (targetNext.equals(targetNode) && !pathToFollow.isEmpty()) targetNext = pathToFollow.remove(0);
                    targetNode = targetNext;*/
                }
                if(agentPath!= null)
                    pathToFollow=Graphs.getPathVertexList(agentPath);
                if(!pathToFollow.isEmpty()){
                    int i = pathToFollow.lastIndexOf(agentNode);
                    agentNext=pathToFollow.remove(i+1);
                }
                agentNode = agentNext;
                count++;
            }
            r.terminate();
            try {
                evadeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pathToFollow=null;
            movingElapsedTime.put(p, movingElapsTime);
            movingExpandedCells.put(p, movingExpCell);
            movesMap.put(p, count);
            searchesMap.put(p, search);
            if(verbose) {
                Long totElaps = Long.valueOf(0);
                Integer totExp = 0;
                for (Long l : movingElapsTime) totElaps += l;
                for (Integer i : movingExpCell) totExp += i;
                System.out.println("total elapsed: " + totElaps + " total expanded " + totExp);
            }
        }
    }
}
