package tilemap;

import tilemap.jgrapht.alg.DijkstraShortestPath;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by notsaved on 3/30/17.
 */
public class StationaryTest {
    private static final int NUMBER_OF_MAPS = 65;
    private static final int NUMBER_OF_POINTS = 100;
    private LinkedList<SimpleWeightedGraph<Integer,DefaultEdge>> indoorMaps = new LinkedList<>();
    private LinkedList<SimpleWeightedGraph<Integer,DefaultEdge>> outdoorMaps = new LinkedList<>();
    private LinkedList<SimpleWeightedGraph<Integer,DefaultEdge>> dngMaps = new LinkedList<>();
    private DijkstraShortestPath<Integer, DefaultEdge> pathfinder;
    private HashMap<SimpleWeightedGraph<Integer,DefaultEdge>, LinkedList<Point[]>> mapToPoints = new HashMap<>();
    private static Random r = new Random(GameMap.WIDTH*GameMap.HEIGHT);


    public StationaryTest(){
        System.out.println("Generating "+NUMBER_OF_MAPS+" maps for each kind with "+NUMBER_OF_POINTS+" points for each one...");
        for(int i = 0; i<NUMBER_OF_MAPS; i++){
            GameMap m = new GameMap(null, null, new IndoorMapGenerator());
            indoorMaps.add(m.getGraph());
            LinkedList<Point[]> points = new LinkedList<>();
            for(int j=0; j<NUMBER_OF_POINTS; j++){
                Point a, b;
                do {
                    a = chooseRandomPoint(m);
                    b = chooseRandomPoint(m);
                    pathfinder = new DijkstraShortestPath<Integer, DefaultEdge>(m.getGraph(), a.toNode(), b.toNode());

                } while (pathfinder.getPath() == null);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
            mapToPoints.put(m.getGraph(), points);
        }

        for(int i=0; i<NUMBER_OF_MAPS; i++){
            GameMap m = new GameMap(null, null, new OutdoorMapGenerator());
            outdoorMaps.add(m.getGraph());
            LinkedList<Point[]> points = new LinkedList<>();
            for(int j = 0; j<NUMBER_OF_POINTS; j++){
                Point a = chooseRandomPoint(m);
                Point b = chooseRandomPoint(m);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
            mapToPoints.put(m.getGraph(), points);
        }

        for(int i=0; i<NUMBER_OF_MAPS; i++){
            GameMap m = new GameMap(null, null,  new DungeonMapGenerator());
            dngMaps.add(m.getGraph());
            LinkedList<Point[]> points = new LinkedList<>();
            for(int j = 0; j<NUMBER_OF_POINTS; j++){
                Point a = chooseRandomPoint(m);
                Point b = chooseRandomPoint(m);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
            mapToPoints.put(m.getGraph(), points);
        }
    }

    public void runIndoor(){
        System.out.println("\nTesting "+indoorMaps.size()+" indoor maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n");

        LinkedList<Double> expNodesDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesAstarAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesBiAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeBiAstarAverages = new LinkedList<>();
        for(SimpleWeightedGraph m : indoorMaps){
            Test t1 = new DijkstraTest(m, mapToPoints.get(m));
            Test t2 = new AStarTest(m, mapToPoints.get(m));
            Test t3 = new BidirectionalAstarTest(m, mapToPoints.get(m));
            t1.run();
            t2.run();
            t3.run();
            expNodesDijkstraAverages.add(t1.getExpandedCellPerRun());
            ElapsTimeDijkstraAverages.add(t1.getElapsedTimePerRun());
            expNodesAstarAverages.add(t2.getExpandedCellPerRun());
            ElapsTimeAstarAverages.add(t2.getElapsedTimePerRun());
            expNodesBiAstarAverages.add(t3.getExpandedCellPerRun());
            ElapsTimeBiAstarAverages.add(t3.getElapsedTimePerRun());
        }

        double djkstrTotExp = getSum(expNodesDijkstraAverages);
        double djkstrNodeAvg = getAvg(expNodesDijkstraAverages);
        double djkstrNodeStdDev = getStandardDeviation(expNodesDijkstraAverages,djkstrNodeAvg);
        double djkstrTotTime = getSum(ElapsTimeDijkstraAverages);
        double djkstrTimeAvg = getAvg(ElapsTimeDijkstraAverages);
        double djkstrTimeStdDev = getStandardDeviation(ElapsTimeDijkstraAverages,djkstrTimeAvg);

        double astarTotExp = getSum(expNodesAstarAverages);
        double astarNodeAvg = getAvg(expNodesAstarAverages);
        double astarNodeStdDev = getStandardDeviation(expNodesAstarAverages,astarNodeAvg);
        double astarTotTime = getSum(ElapsTimeAstarAverages);
        double astarTimeAvg = getAvg(ElapsTimeAstarAverages);
        double astarTimeStdDev = getStandardDeviation(ElapsTimeAstarAverages,astarTimeAvg);

        double biastarTotExp = getSum(expNodesBiAstarAverages);
        double biastarNodeAvg = getAvg(expNodesBiAstarAverages);
        double biastarNodeStdDev = getStandardDeviation(expNodesBiAstarAverages,biastarNodeAvg);
        double biastarTotTime = getSum(ElapsTimeBiAstarAverages);
        double biastarTimeAvg = getAvg(ElapsTimeBiAstarAverages);
        double biastarTimeStdDev = getStandardDeviation(ElapsTimeAstarAverages,biastarTimeAvg);

        System.out.println("Dijkstra\nTotal Expandend Cell: "+djkstrTotExp+"; Average Expandend Cell: "+djkstrNodeAvg+" ["+djkstrNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",djkstrTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",djkstrTimeAvg)+"ms ["+String.format("%.6f", djkstrTimeStdDev)+"]");
        System.out.println("A*\nTotal Expandend Cell: "+astarTotExp+"; Average Expandend Cell: "+astarNodeAvg+" ["+astarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",astarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",astarTimeAvg)+"ms ["+String.format("%.6f",astarTimeStdDev)+"]");
        System.out.println("Bidirectional A*\nTotal Expandend Cell: "+biastarTotExp+"; Average Expandend Cell: "+biastarNodeAvg+" ["+biastarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",biastarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",biastarTimeAvg)+"ms ["+String.format("%.6f",biastarTimeStdDev)+"]");
    }

    public void runOutdoor(){
        System.out.println("\nTesting "+outdoorMaps.size()+" outdoor maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n");

        LinkedList<Double> expNodesDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesAstarAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesBiAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeBiAstarAverages = new LinkedList<Double>();
        for(SimpleWeightedGraph m : outdoorMaps){
            Test t1 = new DijkstraTest(m, mapToPoints.get(m));
            Test t2 = new AStarTest(m, mapToPoints.get(m));
            Test t3 = new BidirectionalAstarTest(m, mapToPoints.get(m));
            t1.run();
            t2.run();
            t3.run();
            expNodesDijkstraAverages.add(t1.getExpandedCellPerRun());
            ElapsTimeDijkstraAverages.add(t1.getElapsedTimePerRun());
            expNodesAstarAverages.add(t2.getExpandedCellPerRun());
            ElapsTimeAstarAverages.add(t2.getElapsedTimePerRun());
            expNodesBiAstarAverages.add(t3.getExpandedCellPerRun());
            ElapsTimeBiAstarAverages.add(t3.getElapsedTimePerRun());
        }

        double djkstrTotExp = getSum(expNodesDijkstraAverages);
        double djkstrNodeAvg = getAvg(expNodesDijkstraAverages);
        double djkstrNodeStdDev = getStandardDeviation(expNodesDijkstraAverages,djkstrNodeAvg);
        double djkstrTotTime = getSum(ElapsTimeDijkstraAverages);
        double djkstrTimeAvg = getAvg(ElapsTimeDijkstraAverages);
        double djkstrTimeStdDev = getStandardDeviation(ElapsTimeDijkstraAverages,djkstrTimeAvg);

        double astarTotExp = getSum(expNodesAstarAverages);
        double astarNodeAvg = getAvg(expNodesAstarAverages);
        double astarNodeStdDev = getStandardDeviation(expNodesAstarAverages,astarNodeAvg);
        double astarTotTime = getSum(ElapsTimeAstarAverages);
        double astarTimeAvg = getAvg(ElapsTimeAstarAverages);
        double astarTimeStdDev = getStandardDeviation(ElapsTimeAstarAverages,astarTimeAvg);

        double biastarTotExp = getSum(expNodesBiAstarAverages);
        double biastarNodeAvg = getAvg(expNodesBiAstarAverages);
        double biastarNodeStdDev = getStandardDeviation(expNodesBiAstarAverages,biastarNodeAvg);
        double biastarTotTime = getSum(ElapsTimeBiAstarAverages);
        double biastarTimeAvg = getAvg(ElapsTimeBiAstarAverages);
        double biastarTimeStdDev = getStandardDeviation(ElapsTimeAstarAverages,biastarTimeAvg);

        System.out.println("Dijkstra\nTotal Expandend Cell: "+djkstrTotExp+"; Average Expandend Cell: "+djkstrNodeAvg+" ["+djkstrNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",djkstrTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",djkstrTimeAvg)+"ms ["+String.format("%.6f", djkstrTimeStdDev)+"]");
        System.out.println("A*\nTotal Expandend Cell: "+astarTotExp+"; Average Expandend Cell: "+astarNodeAvg+" ["+astarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",astarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",astarTimeAvg)+"ms ["+String.format("%.6f",astarTimeStdDev)+"]");
        System.out.println("Bidirectional A*\nTotal Expandend Cell: "+biastarTotExp+"; Average Expandend Cell: "+biastarNodeAvg+" ["+biastarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",biastarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",biastarTimeAvg)+"ms ["+String.format("%.6f",biastarTimeStdDev)+"]");

    }

    public void runDungeon(){
        System.out.println("\nTesting "+dngMaps.size()+" dungeon maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n");

        LinkedList<Double> expNodesDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesAstarAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesBiAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeBiAstarAverages = new LinkedList<Double>();
        for(SimpleWeightedGraph m : dngMaps){
            Test t1 = new DijkstraTest(m, mapToPoints.get(m));
            Test t2 = new AStarTest(m, mapToPoints.get(m));
            Test t3 = new BidirectionalAstarTest(m, mapToPoints.get(m));
            t1.run();
            t2.run();
            t3.run();
            expNodesDijkstraAverages.add(t1.getExpandedCellPerRun());
            ElapsTimeDijkstraAverages.add(t1.getElapsedTimePerRun());
            expNodesAstarAverages.add(t2.getExpandedCellPerRun());
            ElapsTimeAstarAverages.add(t2.getElapsedTimePerRun());
            expNodesBiAstarAverages.add(t3.getExpandedCellPerRun());
            ElapsTimeBiAstarAverages.add(t3.getElapsedTimePerRun());
        }

        double djkstrTotExp = getSum(expNodesDijkstraAverages);
        double djkstrNodeAvg = getAvg(expNodesDijkstraAverages);
        double djkstrNodeStdDev = getStandardDeviation(expNodesDijkstraAverages,djkstrNodeAvg);
        double djkstrTotTime = getSum(ElapsTimeDijkstraAverages);
        double djkstrTimeAvg = getAvg(ElapsTimeDijkstraAverages);
        double djkstrTimeStdDev = getStandardDeviation(ElapsTimeDijkstraAverages,djkstrTimeAvg);

        double astarTotExp = getSum(expNodesAstarAverages);
        double astarNodeAvg = getAvg(expNodesAstarAverages);
        double astarNodeStdDev = getStandardDeviation(expNodesAstarAverages,astarNodeAvg);
        double astarTotTime = getSum(ElapsTimeAstarAverages);
        double astarTimeAvg = getAvg(ElapsTimeAstarAverages);
        double astarTimeStdDev = getStandardDeviation(ElapsTimeAstarAverages,astarTimeAvg);

        double biastarTotExp = getSum(expNodesBiAstarAverages);
        double biastarNodeAvg = getAvg(expNodesBiAstarAverages);
        double biastarNodeStdDev = getStandardDeviation(expNodesBiAstarAverages,biastarNodeAvg);
        double biastarTotTime = getSum(ElapsTimeBiAstarAverages);
        double biastarTimeAvg = getAvg(ElapsTimeBiAstarAverages);
        double biastarTimeStdDev = getStandardDeviation(ElapsTimeAstarAverages,biastarTimeAvg);

        System.out.println("Dijkstra\nTotal Expandend Cell: "+djkstrTotExp+"; Average Expandend Cell: "+djkstrNodeAvg+" ["+djkstrNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",djkstrTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",djkstrTimeAvg)+"ms ["+String.format("%.6f", djkstrTimeStdDev)+"]");
        System.out.println("A*\nTotal Expandend Cell: "+astarTotExp+"; Average Expandend Cell: "+astarNodeAvg+" ["+astarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",astarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",astarTimeAvg)+"ms ["+String.format("%.6f",astarTimeStdDev)+"]");
        System.out.println("Bidirectional A*\nTotal Expandend Cell: "+biastarTotExp+"; Average Expandend Cell: "+biastarNodeAvg+" ["+biastarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",biastarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",biastarTimeAvg)+"ms ["+String.format("%.6f",biastarTimeStdDev)+"]");


    }

    private static Point chooseRandomPoint(GameMap map){
        int node = r.nextInt(GameMap.HEIGHT*GameMap.WIDTH);
        while(map.blocked(node%map.WIDTH, node/map.WIDTH))
            node=r.nextInt(GameMap.HEIGHT*GameMap.WIDTH);
        return new Point(node%map.WIDTH,node/map.WIDTH);
    }

    private static double getSum(LinkedList<Double> l) {
        double d=0;
        for(Double i : l)
            d+=i;
        return d;
    }

    private static double getAvg(LinkedList<Double> l){
        double d=0;
        for(Double i : l){
            d += i;
        }
        return d/l.size();
    }

    private static double getVariance(LinkedList<Double> l, double avg){
        double d=0;
        for(Double i : l) {
            d += ((i - avg) * (i - avg));
        }
        return d/(l.size()-1);
    }

    private static double getStandardDeviation(LinkedList<Double> l, double avg){
        return Math.sqrt(getVariance(l,avg));
    }
}
