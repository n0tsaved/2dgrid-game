package tilemap;

import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.alg.AStarShortestPath;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

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
    public void run() {
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
            pathfinder= new AStarShortestPath<>(map);
            now = System.currentTimeMillis();
            pathfinder.getShortestPath(next,p[1].toNode(), new OctileDistance());
            elapsedTime.put(p, System.currentTimeMillis() - now);
            expandedCells.put(p,pathfinder.getNumberOfExpandedNodes());
        }
    }


}
