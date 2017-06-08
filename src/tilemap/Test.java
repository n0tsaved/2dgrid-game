package tilemap;

import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultWeightedEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;
import java.util.List;

/**
 * Created by notsaved on 1/18/17.
 */
public abstract class Test  {

    protected SimpleWeightedGraph<Integer, DefaultWeightedEdge> map;
    protected List<Point[]> points;
    protected Map<Point[], Long> elapsedTime = new HashMap<>();
    protected Map<Point[], Integer> expandedCells = new HashMap<>();
    protected Map<Point[], LinkedList<Integer>> movingExpandedCells = new HashMap<>();
    protected Map<Point[], LinkedList<Long>> movingElapsedTime = new HashMap<>();
    protected Map<Point[], Integer> movesMap = new HashMap<>();
    protected Map<Point[], Integer> searchesMap = new HashMap<>();
    protected Pathfinder<Integer, DefaultWeightedEdge> pathfinder;
    protected String result;
    protected double expandedNodeAverage;
    protected double elapsedTimeAverage;
    protected boolean verbose = true;
    protected Integer agentNode, targetNode;
    protected Boolean moveTarget = new Boolean(false);
    protected Thread evadeThread = null;
   /* private Map<Point[],Long> executionTime = new HashMap<>();
    private HashMap<Point[], List<Pathfinder<Integer, DefaultEdge>>> pathfinders = new HashMap<>();

    private static Random r = new Random(GameMap.WIDTH*GameMap.HEIGHT);
*/
    public Test(SimpleWeightedGraph map, List<Point[]> points){
        this.map = map;
        this.points = points;
    }

    public abstract void runStationaryTest();

    public abstract void runMovingTest();


    /*
    public void runStationaryTest(){
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

    protected class OctileDistance implements AStarAdmissibleHeuristic<Integer> {

        @Override
        public double getCostEstimate(Integer sourceVertex, Integer targetVertex) {
            int sourceX, sourceY, targetX, targetY;
            sourceX=sourceVertex%GameMap.WIDTH;
            sourceY=sourceVertex/GameMap.WIDTH;
            targetX=targetVertex%GameMap.WIDTH;
            targetY=targetVertex/GameMap.WIDTH;
            //System.out.println("Source: [" +sourceX+","+sourceY+"] Target: ["+
            //        targetX+","+targetY+"] EstimatedCost: "+Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY));
            return Math.abs(Math.abs(sourceX - targetX) - Math.abs(sourceY - targetY))+
                    Math.sqrt(2)*Math.min(Math.abs(sourceX - targetX), Math.abs(sourceY - targetY));
        }
    }


    public int getTotalExpandedCells(){
        //int length=0;
        int tot =0;
        for(Point[] p : points){
            //length = expandedCells.get(p).size();
                tot+=expandedCells.get(p);
            //subtot = subtot / length;
            //tot+=subtot;
        }
        return tot;
    }

    public double getMovingTotalElapsedTime() {
        double tot =0;
        for(Point[] p : points)
            for(Long l : movingElapsedTime.get(p))
                tot+=l;
        return tot;
    }

    public double getMovingTotalSearches(){
        double tot=0;
        for(Point[] p : points)
            tot+=searchesMap.get(p);
        return tot;
    }

    public double getMovingTotalExpandedCells(){
        double tot=0;
        for (Point[] p : points) {
            for (Integer i : movingExpandedCells.get(p))
                tot += i;
        }
        return tot;
    }

    public double getExpandedCellPerRun(){
        if(expandedNodeAverage==0) {
            expandedNodeAverage = getTotalExpandedCells()/points.size();
        }
        return expandedNodeAverage;
    }

    public double getTotalElapsedTime(){
        double elapsed = 0;
        for(Point[] p : points)
            elapsed+=elapsedTime.get(p);
        return elapsed;
    }

    public double getElapsedTimePerRun() {
        if(elapsedTimeAverage==0) {
            elapsedTimeAverage= getTotalElapsedTime() / points.size();
        }
        return elapsedTimeAverage;
    }

   public double getTotalMoves(){
        int moves=0;
        for(Point[] p: points)
            moves+=movesMap.get(p);
        return moves;
    }


   public double getTimeStandardDeviation(){
       return Math.sqrt(getTimeVariance());
   }

   public double getTimeVariance(){
       double aux=0;
       for(Point[] p: points)
           aux += (elapsedTime.get(p) - elapsedTimeAverage)*(elapsedTime.get(p) - elapsedTimeAverage);
       return aux/points.size();
   }

    public double getExpandedNodesStandardDeviation(){
        return Math.sqrt(getExpandedNodeVariance());
    }

    public double getExpandedNodeVariance(){
        double aux=0;
        for(Point[] p: points)
            aux += (expandedCells.get(p) - expandedNodeAverage)*(expandedCells.get(p) - expandedNodeAverage);
        return aux/points.size();
    }


    protected class targetThread implements Runnable{


        private volatile boolean running = true;

        public void terminate() {
            running = false;
        }

        @Override
        public void run() {
            GraphPath<Integer,DefaultWeightedEdge> targetPath=null;
            List<Integer> path = null;
            Integer targetNext = null;
            while(running){
                if(path==null || path.isEmpty()) {
                    targetPath = new Trailmax<Integer, DefaultWeightedEdge>(map).getShortestPath(agentNode, targetNode, null);
                    path = Graphs.getPathVertexList(targetPath);
                }
               synchronized (moveTarget) {
                    if (moveTarget) {
                        //System.out.println(agentNode+", "+targetNode);
                        if (!path.isEmpty()) targetNext = path.remove(0);
                        if (targetNext.equals(targetNode) && !path.isEmpty()) targetNext = path.remove(0);
                        targetNode = targetNext;
                        moveTarget=!moveTarget;
                        try {
                            Thread.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                   }
                }
            }
        }
    }
}
