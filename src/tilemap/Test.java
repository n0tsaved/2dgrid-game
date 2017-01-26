package tilemap;

import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.AStarShortestPath;
import tilemap.jgrapht.alg.BidirectionalAStarShortestPath;
import tilemap.jgrapht.alg.DijkstraShortestPath;
import tilemap.jgrapht.alg.MovingTargetAdaptiveAStarShortestPath;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.Point;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by notsaved on 1/18/17.
 */
public abstract class Test  {

    protected GameMap map;
    protected List<Point[]> points;
    protected Map<Point[], List<Long>> elapsedTime = new HashMap<>();
    protected Map<Point[], List<Integer>> expandedCells = new HashMap<>();
    protected Pathfinder<Integer, DefaultEdge> pathfinder;
    protected String result;
   /* private Map<Point[],Long> executionTime = new HashMap<>();
    private HashMap<Point[], List<Pathfinder<Integer, DefaultEdge>>> pathfinders = new HashMap<>();

    private static Random r = new Random(GameMap.WIDTH*GameMap.HEIGHT);
*/
    public Test(GameMap map, List<Point[]> points){
        this.map = map;
        this.points = points;
    }

    public abstract void run();
    /*
    public void run(){
        long now;
        GraphPath<Integer,DefaultEdge> path = null;
        for(Point[] p : points)
        {
            if(p[0].toNode().equals(p[1].toNode())) continue;
            for(Pathfinder<Integer, DefaultEdge> ptfndr : pathfinders.get(p))
            {
                now = System.currentTimeMillis();
                path=ptfndr.getShortestPath(p[0].toNode(), p[1].toNode(), new ManhattanDistance());
                executionTime.put(p, System.currentTimeMillis() - now);
                Graphs.getPathVertexList(path);

            }
        }
    }

    private Point chooseRandomPoint(){
        int node = r.nextInt();
        while(map.blocked(node%map.WIDTH, node/map.WIDTH))
            node=r.nextInt();
        return new Point(node%map.WIDTH,node/map.WIDTH);
    }*/



    protected static class ManhattanDistance implements AStarAdmissibleHeuristic<Integer> {

        @Override
        public double getCostEstimate(Integer sourceVertex, Integer targetVertex) {
            int sourceX, sourceY, targetX, targetY;
            sourceX=sourceVertex%GameMap.WIDTH;
            sourceY=sourceVertex/GameMap.WIDTH;
            targetX=targetVertex%GameMap.WIDTH;
            targetY=targetVertex/GameMap.WIDTH;
            //System.out.println("Source: [" +sourceX+","+sourceY+"] Target: ["+
            //        targetX+","+targetY+"] EstimatedCost: "+Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY));
            return Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY);
        }
    }

    public int getTotalExpandedCells(){
        //int length=0;
        int tot =0;
        for(Point[] p : points){
            //length = expandedCells.get(p).size();
            for(int i : expandedCells.get(p))
                tot+=i;
            //subtot = subtot / length;
            //tot+=subtot;
        }
        return tot;
    }

    public double getExpandedCellPerRun(){
        int length=0, tot=0;
        for(Point[] p: points)
            length+=expandedCells.get(p).size();
        for(Point[] p : points)
            for(int i: expandedCells.get(p))
                tot+=i;
        return tot/length;
    }

    public long getTotalElapsedTime(){
        long elapsed = 0;
        for(Point[] p : points)
            for(long i : elapsedTime.get(p))
                elapsed+=i;
        return elapsed;
    }

    public double getElapsedTimePerRun() {
        int length = 0;
        long tot = 0;
        for (Point[] p : points) {
            length += elapsedTime.get(p).size();
            for(long i : elapsedTime.get(p))
                tot+=i;
        }
        return tot/length;
    }

    public double getTotalMoves(){
        int moves=0;
        for(Point[] p: points)
            moves+=expandedCells.get(p).size();
        return moves;
    }
}
