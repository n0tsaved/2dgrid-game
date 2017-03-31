package tilemap;

import tilemap.jgrapht.Graph;
import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.DijkstraShortestPath;
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
           now = System.currentTimeMillis();
           pathfinder= new DijkstraShortestPath<Integer, DefaultEdge>(map, next, p[1].toNode());
           //elapsedTime.put(p, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - now));
            elapsedTime.put(p, System.currentTimeMillis() - now);
            expandedCells.put(p,pathfinder.getNumberOfExpandedNodes());
        }
    }


}
