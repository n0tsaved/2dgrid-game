package tilemap;

import tilemap.jgrapht.alg.DijkstraShortestPath;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.DefaultWeightedEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by notsaved on 3/30/17.
 */
public class GlobalTest {
    private static final int NUMBER_OF_MAPS = 80;
    private static final int NUMBER_OF_POINTS = 30;
    private LinkedList<SimpleWeightedGraph<Integer,DefaultWeightedEdge>> indoorMaps = new LinkedList<>();
    private LinkedList<SimpleWeightedGraph<Integer,DefaultWeightedEdge>> outdoorMaps = new LinkedList<>();
    private LinkedList<SimpleWeightedGraph<Integer,DefaultWeightedEdge>> dngMaps = new LinkedList<>();
    private DijkstraShortestPath<Integer, DefaultWeightedEdge> pathfinder;
    private HashMap<SimpleWeightedGraph<Integer,DefaultWeightedEdge>, LinkedList<Point[]>> mapToPoints = new HashMap<>();
    private static Random r = new Random(GameMap.WIDTH*GameMap.HEIGHT);
    private static final String movingTest = " (a) = searches until the target is caught;\n (b) = moves until the target is caught;\n (c) = total state expansions until the target is caught (" +
            "standard deviation of the mean);\n (d) = total search time until the target is caught in milliseconds (standard deviation of the mean);\n (e) = runtime per search (in milliseconds)";


    public GlobalTest(){
      /*  System.out.println("Generating "+NUMBER_OF_MAPS+" maps for each kind with "+NUMBER_OF_POINTS+" points for each one...");
        MapGenerator indoorGnrt = new IndoorMapGenerator();
        MapGenerator outdoorGnrt = new OutdoorMapGenerator();
        MapGenerator dungeonGnrt = new DungeonMapGenerator();
        for(int i = 0; i<NUMBER_OF_MAPS; i++){
            GameMap m = new GameMap(null, null, indoorGnrt);
            indoorMaps.add(m.getGraph());
            LinkedList<Point[]> points = new LinkedList<>();
            for(int j=0; j<NUMBER_OF_POINTS; j++){
                Point a, b;
                do {
                    a = chooseRandomPoint(m);
                    b = chooseRandomPoint(m);
                    pathfinder = new DijkstraShortestPath<>(m.getGraph(), a.toNode(), b.toNode());

                } while (pathfinder.getPath() == null);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
            mapToPoints.put(m.getGraph(), points);
        }

        for(int i=0; i<NUMBER_OF_MAPS; i++){
            GameMap m = new GameMap(null, null, outdoorGnrt);
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
            GameMap m = new GameMap(null, null,  dungeonGnrt);
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
       /* for(SimpleWeightedGraph<Integer, DefaultWeightedEdge> m : outdoorMaps)
            for(DefaultWeightedEdge e: m.edgeSet())
                System.out.println(e.toString()+", "+e.getWeight());*/
    }


    public void runStationaryTest(){
        System.out.println("Stationary Target Test\n");

        String res1 = performStationaryTest(indoorMaps, mapToPoints, "\nTesting " + indoorMaps.size() + " indoor maps [120x120], " + NUMBER_OF_POINTS + " points for each map\n");
        String res2 = performStationaryTest(outdoorMaps, mapToPoints, "\nTesting "+outdoorMaps.size()+" outdoor maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n");
        String res3 = performStationaryTest(dngMaps, mapToPoints, "\nTesting "+dngMaps.size()+" dungeon maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n");
        System.out.println(res1+res2+res3);
    }

    private static String performStationaryTest(LinkedList<SimpleWeightedGraph<Integer, DefaultWeightedEdge>> maps, HashMap<SimpleWeightedGraph<Integer, DefaultWeightedEdge>, LinkedList<Point[]>> mapToPoints, String print) {
        LinkedList<Double> expNodesDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesAstarAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesBiAstarAverages = new LinkedList<Double>();
        LinkedList<Double> expNodesAAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeDijkstraAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeBiAstarAverages = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeAAstarAverages = new LinkedList<Double>();

        for(SimpleWeightedGraph m : maps){
            Test t1 = new DijkstraTest(m, mapToPoints.get(m));
            Test t2 = new AStarTest(m, mapToPoints.get(m));
            Test t3 = new BidirectionalAstarTest(m, mapToPoints.get(m));
            Test t4 = new AdaptiveAStarTest(m, mapToPoints.get(m));
            t1.runStationaryTest();
            t2.runStationaryTest();
            t3.runStationaryTest();
            t4.runStationaryTest();
            expNodesDijkstraAverages.add(t1.getExpandedCellPerRun());
            ElapsTimeDijkstraAverages.add(t1.getElapsedTimePerRun());
            expNodesAstarAverages.add(t2.getExpandedCellPerRun());
            ElapsTimeAstarAverages.add(t2.getElapsedTimePerRun());
            expNodesBiAstarAverages.add(t3.getExpandedCellPerRun());
            ElapsTimeBiAstarAverages.add(t3.getElapsedTimePerRun());
            expNodesAAstarAverages.add(t4.getExpandedCellPerRun());
            ElapsTimeAAstarAverages.add(t4.getElapsedTimePerRun());

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
        double biastarTimeStdDev = getStandardDeviation(ElapsTimeBiAstarAverages,biastarTimeAvg);

        double aastarTotExp = getSum(expNodesAAstarAverages);
        double aastarNodeAvg = getAvg(expNodesAAstarAverages);
        double aastarNodeStdDev = getStandardDeviation(expNodesAAstarAverages,aastarNodeAvg);
        double aastarTotTime = getSum(ElapsTimeAAstarAverages);
        double aastarTimeAvg = getAvg(ElapsTimeAAstarAverages);
        double aastarTimeStdDev = getStandardDeviation(ElapsTimeAAstarAverages,aastarTimeAvg);

        String res;
        String djkstrRes = "Dijkstra\nTotal Expandend Cell: "+djkstrTotExp+"; Average Expandend Cell: "+djkstrNodeAvg+" ["+djkstrNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",djkstrTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",djkstrTimeAvg)+"ms ["+String.format("%.6f", djkstrTimeStdDev)+"]";
        String astarRes = "A*\nTotal Expandend Cell: "+astarTotExp+"; Average Expandend Cell: "+astarNodeAvg+" ["+astarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",astarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",astarTimeAvg)+"ms ["+String.format("%.6f",astarTimeStdDev)+"]";
        String bidiRes = "Bidirectional A*\nTotal Expandend Cell: "+biastarTotExp+"; Average Expandend Cell: "+biastarNodeAvg+" ["+biastarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",biastarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",biastarTimeAvg)+"ms ["+String.format("%.6f",biastarTimeStdDev)+"]";
        String aastarRes = "Adaptive-A*\nTotal Expandend Cell: "+aastarTotExp+"; Average Expandend Cell: "+aastarNodeAvg+" ["+aastarNodeStdDev+
                "]; Elapsed Time: "+String.format("%.6f",aastarTotTime)+"ms; Average Elapsed Time: "+String.format("%.6f",aastarTimeAvg)+"ms ["+String.format("%.6f",aastarTimeStdDev)+"]";

        res = "\n"+print + "\n"+djkstrRes+"\n"+astarRes+"\n"+bidiRes+"\n"+aastarRes;
        return res;
    }

    public void runMovingTest(){
        System.out.println("Moving Target Test\n");
        String indoorRes = performMovingTest2(new IndoorMapGenerator(), "Indoor Test: ");

        //String indoorRes = performMovingTest(indoorMaps, mapToPoints, "Indoor Test: ");
        //String outdoorRes = performMovingTest(outdoorMaps, mapToPoints, "Outdoor Test: ");
        //String dungeonRes = performMovingTest(dngMaps, mapToPoints, "Dungeon Test: ");
        System.out.println("\n"+movingTest);
        System.out.println("\nTested "+indoorMaps.size()+" indoor maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n"+indoorRes);
        //System.out.println("\nTested "+outdoorMaps.size()+" outdoor maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n"+outdoorRes);
        //System.out.println("\nTested "+dngMaps.size()+" dungeon maps [120x120], "+NUMBER_OF_POINTS+" points for each map\n"+dungeonRes);
    }

    private static String performMovingTest(LinkedList<SimpleWeightedGraph<Integer, DefaultWeightedEdge>> maps, HashMap<SimpleWeightedGraph<Integer, DefaultWeightedEdge>, LinkedList<Point[]>> mapToPoints, String print) {
        LinkedList<Double> expNodesDijkstraTotals = new LinkedList<>();
        LinkedList<Double> expNodesAstarTotals = new LinkedList<Double>();
        LinkedList<Double> expNodesBiAstarTotals = new LinkedList<Double>();
        LinkedList<Double> expNodesAAstarTotals = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeDijkstraTotals = new LinkedList<>();
        LinkedList<Double> ElapsTimeAstarTotals = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeBiAstarTotals = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeAAstarTotals = new LinkedList<Double>();
        double percentage=0;
        int count=0;
        int djkstrSearch =0, djkstrMoves =0;
        int astarSearch=0, astarMoves =0;
        int biastarSearch=0, biastarMoves =0;
        int aastarSearch=0, aastarMoves=0;
        for(SimpleWeightedGraph<Integer, DefaultWeightedEdge> m : maps){
            System.out.println(print+(100*count/maps.size())+"% \r");
            Test t1 = new DijkstraTest(m, mapToPoints.get(m));
            Test t2 = new AStarTest(m, mapToPoints.get(m));
            Test t3 = new BidirectionalAstarTest(m, mapToPoints.get(m));
            Test t4 = new AdaptiveAStarTest(m, mapToPoints.get(m));
            t1.runMovingTest();
            t2.runMovingTest();
            t3.runMovingTest();
            t4.runMovingTest();
            djkstrSearch += t1.getMovingTotalSearches(); djkstrMoves += t1.getTotalMoves();
            astarSearch += t2.getMovingTotalSearches(); astarMoves += t2.getTotalMoves();
            biastarSearch += t3.getMovingTotalSearches(); biastarMoves += t3.getTotalMoves();
            aastarSearch += t4.getMovingTotalSearches(); aastarMoves += t4.getTotalMoves();
            expNodesDijkstraTotals.add(t1.getMovingTotalExpandedCells());
            ElapsTimeDijkstraTotals.add(t1.getMovingTotalElapsedTime());
            expNodesAstarTotals.add(t2.getMovingTotalExpandedCells());
            ElapsTimeAstarTotals.add(t2.getMovingTotalElapsedTime());
            expNodesBiAstarTotals.add(t3.getMovingTotalExpandedCells());
            ElapsTimeBiAstarTotals.add(t3.getMovingTotalElapsedTime());
            expNodesAAstarTotals.add(t4.getMovingTotalExpandedCells());
            ElapsTimeAAstarTotals.add(t4.getMovingTotalElapsedTime());
            count++;
            try {
                Runtime.getRuntime().exec("clear");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        double djkstrTotExp = getSum(expNodesDijkstraTotals);
        double djkstrNodeAvg = getAvg(expNodesDijkstraTotals);
        double djkstrNodeStdDev = getStandardDeviation(expNodesDijkstraTotals,djkstrNodeAvg);
        double djkstrTotTime = getSum(ElapsTimeDijkstraTotals);
        double djkstrTimeAvg = getAvg(ElapsTimeDijkstraTotals);
        double djkstrTimeStdDev = getStandardDeviation(ElapsTimeDijkstraTotals,djkstrTimeAvg);
        double djkstrRuntimePerSearch = djkstrTotTime / djkstrSearch;

        String djkstrRes = "DIJKSTRA\n(a): " +djkstrSearch+" (b): "+djkstrMoves+" (c): "+djkstrTotExp+" ("+djkstrNodeStdDev+"); (d): "+djkstrTotTime+
                " ("+djkstrTimeStdDev+") (e): "+djkstrRuntimePerSearch+" (f): "+djkstrTimeAvg;

        double astarTotExp = getSum(expNodesAstarTotals);
        double astarNodeAvg = getAvg(expNodesAstarTotals);
        double astarNodeStdDev = getStandardDeviation(expNodesAstarTotals,astarNodeAvg);
        double astarTotTime = getSum(ElapsTimeAstarTotals);
        double astarTimeAvg = getAvg(ElapsTimeAstarTotals);
        double astarTimeStdDev = getStandardDeviation(ElapsTimeAstarTotals,astarTimeAvg);
        double astarRuntimePerSearch = astarTotTime / astarSearch;

        String astarRes = "A*\n(a): " +astarSearch+" (b): "+astarMoves+" (c): "+astarTotExp+" ("+astarNodeStdDev+"); (d): "+astarTotTime+
                " ("+astarTimeStdDev+") (e): "+astarRuntimePerSearch;

        double biastarTotExp = getSum(expNodesBiAstarTotals);
        double biastarNodeAvg = getAvg(expNodesBiAstarTotals);
        double biastarNodeStdDev = getStandardDeviation(expNodesBiAstarTotals,biastarNodeAvg);
        double biastarTotTime = getSum(ElapsTimeBiAstarTotals);
        double biastarTimeAvg = getAvg(ElapsTimeBiAstarTotals);
        double biastarTimeStdDev = getStandardDeviation(ElapsTimeBiAstarTotals,biastarTimeAvg);
        double biastarRuntimePerSearch = biastarTotTime / biastarSearch;

        String biastarRes = "BIDIRECTIONAL A*\n(a): " +biastarSearch+" (b): "+biastarMoves+" (c): "+biastarTotExp+" ("+biastarNodeStdDev+"); (d): "+biastarTotTime+
                " ("+biastarTimeStdDev+") (e): "+biastarRuntimePerSearch;

        double aastarTotExp = getSum(expNodesAAstarTotals);
        double aastarNodeAvg = getAvg(expNodesAAstarTotals);
        double aastarNodeStdDev = getStandardDeviation(expNodesAAstarTotals,aastarNodeAvg);
        double aastarTotTime = getSum(ElapsTimeAAstarTotals);
        double aastarTimeAvg = getAvg(ElapsTimeAAstarTotals);
        double aastarTimeStdDev = getStandardDeviation(ElapsTimeAAstarTotals,aastarTimeAvg);
        double aastarRuntimePerSearch = aastarTotTime / aastarSearch;

        String aastarRes = "ADAPTIVE-A*\n(a): " +aastarSearch+" (b): "+aastarMoves+" (c): "+aastarTotExp+" ("+aastarNodeStdDev+"); (d): "+aastarTotTime+
                " ("+aastarTimeStdDev+") (e): "+aastarRuntimePerSearch;

        String res = "\n"+djkstrRes+"\n"+astarRes+"\n"+biastarRes+"\n"+aastarRes;
        return res;
    }


    private static String performMovingTest2(MapGenerator mapGenerator, String print) {
        HashMap<SimpleWeightedGraph<Integer, DefaultWeightedEdge>, LinkedList<Point[]>> mapToPoints;
        LinkedList<Double> expNodesDijkstraTotals = new LinkedList<>();
        LinkedList<Double> expNodesAstarTotals = new LinkedList<Double>();
        LinkedList<Double> expNodesBiAstarTotals = new LinkedList<Double>();
        LinkedList<Double> expNodesAAstarTotals = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeDijkstraTotals = new LinkedList<>();
        LinkedList<Double> ElapsTimeAstarTotals = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeBiAstarTotals = new LinkedList<Double>();
        LinkedList<Double> ElapsTimeAAstarTotals = new LinkedList<Double>();
        double percentage=0;
        int count=0;
        int djkstrSearch =0, djkstrMoves =0;
        int astarSearch=0, astarMoves =0;
        int biastarSearch=0, biastarMoves =0;
        int aastarSearch=0, aastarMoves=0;
        for(int i = 0; i< NUMBER_OF_MAPS; i++){
            GameMap map = new GameMap(null, null, mapGenerator);
                SimpleWeightedGraph m = map.getGraph();
                LinkedList<Point[]> points = new LinkedList<>();
                for(int j = 0; j<NUMBER_OF_POINTS; j++){
                    Point a = chooseRandomPoint(map);
                    Point b = chooseRandomPoint(map);
                    Point[] pair = new Point[2];
                    pair[0] = a;
                    pair[1] = b;
                    points.add(pair);
                }
            System.out.println(print+(100*count/NUMBER_OF_MAPS)+"% \r");
            Test t1 = new DijkstraTest(m, points);
            Test t2 = new AStarTest(m, points);
            Test t3 = new BidirectionalAstarTest(m, points);
            Test t4 = new AdaptiveAStarTest(m, points);
            t1.runMovingTest();
            t2.runMovingTest();
            t3.runMovingTest();
            t4.runMovingTest();
            djkstrSearch += t1.getMovingTotalSearches(); djkstrMoves += t1.getTotalMoves();
            astarSearch += t2.getMovingTotalSearches(); astarMoves += t2.getTotalMoves();
            biastarSearch += t3.getMovingTotalSearches(); biastarMoves += t3.getTotalMoves();
            aastarSearch += t4.getMovingTotalSearches(); aastarMoves += t4.getTotalMoves();
            expNodesDijkstraTotals.add(t1.getMovingTotalExpandedCells());
            ElapsTimeDijkstraTotals.add(t1.getMovingTotalElapsedTime());
            expNodesAstarTotals.add(t2.getMovingTotalExpandedCells());
            ElapsTimeAstarTotals.add(t2.getMovingTotalElapsedTime());
            expNodesBiAstarTotals.add(t3.getMovingTotalExpandedCells());
            ElapsTimeBiAstarTotals.add(t3.getMovingTotalElapsedTime());
            expNodesAAstarTotals.add(t4.getMovingTotalExpandedCells());
            ElapsTimeAAstarTotals.add(t4.getMovingTotalElapsedTime());
            count++;
            try {
                Runtime.getRuntime().exec("clear");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        double djkstrTotExp = getSum(expNodesDijkstraTotals);
        double djkstrNodeAvg = getAvg(expNodesDijkstraTotals);
        double djkstrNodeStdDev = getStandardDeviation(expNodesDijkstraTotals,djkstrNodeAvg);
        double djkstrTotTime = getSum(ElapsTimeDijkstraTotals);
        double djkstrTimeAvg = getAvg(ElapsTimeDijkstraTotals);
        double djkstrTimeStdDev = getStandardDeviation(ElapsTimeDijkstraTotals,djkstrTimeAvg);
        double djkstrRuntimePerSearch = djkstrTotTime / djkstrSearch;

        String djkstrRes = "DIJKSTRA\n(a): " +djkstrSearch+" (b): "+djkstrMoves+" (c): "+djkstrTotExp+" ("+djkstrNodeStdDev+"); (d): "+djkstrTotTime+
                " ("+djkstrTimeStdDev+") (e): "+djkstrRuntimePerSearch+" (f): "+djkstrTimeAvg;

        double astarTotExp = getSum(expNodesAstarTotals);
        double astarNodeAvg = getAvg(expNodesAstarTotals);
        double astarNodeStdDev = getStandardDeviation(expNodesAstarTotals,astarNodeAvg);
        double astarTotTime = getSum(ElapsTimeAstarTotals);
        double astarTimeAvg = getAvg(ElapsTimeAstarTotals);
        double astarTimeStdDev = getStandardDeviation(ElapsTimeAstarTotals,astarTimeAvg);
        double astarRuntimePerSearch = astarTotTime / astarSearch;

        String astarRes = "A*\n(a): " +astarSearch+" (b): "+astarMoves+" (c): "+astarTotExp+" ("+astarNodeStdDev+"); (d): "+astarTotTime+
                " ("+astarTimeStdDev+") (e): "+astarRuntimePerSearch;

        double biastarTotExp = getSum(expNodesBiAstarTotals);
        double biastarNodeAvg = getAvg(expNodesBiAstarTotals);
        double biastarNodeStdDev = getStandardDeviation(expNodesBiAstarTotals,biastarNodeAvg);
        double biastarTotTime = getSum(ElapsTimeBiAstarTotals);
        double biastarTimeAvg = getAvg(ElapsTimeBiAstarTotals);
        double biastarTimeStdDev = getStandardDeviation(ElapsTimeBiAstarTotals,biastarTimeAvg);
        double biastarRuntimePerSearch = biastarTotTime / biastarSearch;

        String biastarRes = "BIDIRECTIONAL A*\n(a): " +biastarSearch+" (b): "+biastarMoves+" (c): "+biastarTotExp+" ("+biastarNodeStdDev+"); (d): "+biastarTotTime+
                " ("+biastarTimeStdDev+") (e): "+biastarRuntimePerSearch;

        double aastarTotExp = getSum(expNodesAAstarTotals);
        double aastarNodeAvg = getAvg(expNodesAAstarTotals);
        double aastarNodeStdDev = getStandardDeviation(expNodesAAstarTotals,aastarNodeAvg);
        double aastarTotTime = getSum(ElapsTimeAAstarTotals);
        double aastarTimeAvg = getAvg(ElapsTimeAAstarTotals);
        double aastarTimeStdDev = getStandardDeviation(ElapsTimeAAstarTotals,aastarTimeAvg);
        double aastarRuntimePerSearch = aastarTotTime / aastarSearch;

        String aastarRes = "ADAPTIVE-A*\n(a): " +aastarSearch+" (b): "+aastarMoves+" (c): "+aastarTotExp+" ("+aastarNodeStdDev+"); (d): "+aastarTotTime+
                " ("+aastarTimeStdDev+") (e): "+aastarRuntimePerSearch;

        String res = "\n"+djkstrRes+"\n"+astarRes+"\n"+biastarRes+"\n"+aastarRes;
        return res;
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
