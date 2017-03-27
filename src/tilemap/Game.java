package tilemap;

import sun.awt.image.ImageWatched;
import tilemap.Test.OctileDistance;
import tilemap.Test.ManhattanDistance;
import tilemap.jgrapht.alg.AStarShortestPath;
import tilemap.jgrapht.alg.BidirectionalAStarShortestPath;
import tilemap.jgrapht.alg.DijkstraShortestPath;
import tilemap.jgrapht.alg.ThetaStarShortestPath;
import tilemap.jgrapht.graph.DefaultEdge;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.*;

public class Game extends Canvas{
    private long lastFpsTime;
    /** The buffered strategy used for accelerated rendering */
    private BufferStrategy strategy;
    private static Random r = new Random(GameMap.WIDTH*GameMap.HEIGHT);

    /** The gameMap our player will wander round */
    /** The player entity that will be controlled with cursors */
    private Player player = new Player("☻");

    private LinkedList<Entity> enemies = new LinkedList<>();

    private GameController controller;
    private int fps;
    private GameMap gameMap = new GameMap(player, enemies, new IndoorMapGenerator());
    private final static int MS_PER_UPDATE = 1000000000/2;


    public Game() {


        // create the AWT frame. Its going to be fixed size (500x500)
        // and not resizable - this just gives us less to account for
        Frame frame = new Frame("Tile GameMap");
        frame.setLayout(null);
        setBounds(0,0,800,800);
        frame.add(this);
        frame.setSize(800,800);
        frame.setResizable(false);

        // add a listener to respond to the window closing so we can
        // exit the game
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // add a key listener that allows us to respond to player
        // key presses. We're actually just going to set some flags
        // to indicate the current player direciton

        /*JButton b = new JButton("Generate new Map");
        ActionListener l= new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mapGnrtr.generate(gameMap);

            }
        };
        JPanel btnPnl = new JPanel();
        btnPnl.setLayout(new FlowLayout());
        b.addActionListener(l);
        b.setLayout(new FlowLayout());
        b.setBackground(Color.WHITE);
        b.setForeground(Color.RED);
        b.setBounds(0,0,100,50);
        btnPnl.add(b);
        frame.add(btnPnl);
      //  btnPnl.setLocation(0,0);*/

        // show the frame before creating the buffer strategy!
        frame.setVisible(true);

        // create the strategy used for accelerated rendering. More details
        // in the space invaders 2D tutori
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // create our game objects, a gameMap for the player to wander around
        // and an entity to represent out player
        //if(mapGnrtr.getClass() == DungeonMapGenerator.class) {
          //  int[] spawnCoords = gameMap.getRooms().get(0).center();
            //player = new Player("@", gameMap, spawnCoords[0], spawnCoords[1]);

        player.setGameMap(gameMap);
        player.spawn();
        controller= new GameController( this, player);
        enemies.add(new Entity("#", gameMap, new BidirectionalAStarShortestPath<Integer, DefaultEdge>(gameMap.getGraph())));
        enemies.add(new Entity("§", gameMap, new BidirectionalAStarShortestPath<Integer, DefaultEdge>(gameMap.getGraph())));
        //for(Entity e : enemies)
        //    e.spawn();

        frame.addKeyListener(controller);
        addKeyListener(controller);
    }

    /**
     * The game loop handles the basic rendering and tracking of time. Each
     * loop it calls off to the game logic to perform the movement and
     * collision checking.
     */
    public final void gameLoop() throws InterruptedException {
        boolean gameRunning = true;
        long last = System.nanoTime();
        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
        int lag =0;

        // keep looking while the game is running
        while (gameRunning) {
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

            // clear the screen
            g.setColor(Color.black);
            g.fillRect(0,0,800,800);

            // render our game objects
            g.translate(100,100);
            gameMap.paint(g);
            player.paint(g);
            for(Entity e : enemies)
                    e.paint(g);

            // flip the buffer so we can see the rendering
            g.dispose();
            strategy.show();

            // pause a bit so that we don't choke the system
           // try { Thread.sleep(1); } catch (Exception e) {};

            // calculate how long its been since we last ran the
            // game logic
            long now=System.nanoTime();
            long updateLenght = (now - last);


            last = now;

            long delta=updateLenght/(OPTIMAL_TIME);
            lastFpsTime+=updateLenght;
            fps++;

            while (lastFpsTime >= 1000000000/2)
            {

                //System.out.println("(FPS: "+fps+")");
                for(Entity e : enemies)
                    //e.move();
                lastFpsTime -= 1000000000/2;
                fps = 0;

            }


            // now this needs a bit of explaining. The amount of time
            // passed between rendering can vary quite alot. If we were
            // to move our player based on the normal delta it would
            // at times jump a long distance (if the delta value got really
            // high). So we divide the amount of time passed into segments
            // of 5 milliseconds and update based on that
            for (int i=0;i<delta / 5;i++) {
                controller.logic(5);
            }
            // after we've run through the segments if there is anything
            // left over we update for that
            if ((delta % 5) != 0)
                controller.logic(delta % 5);
            //}
            Thread.sleep( Math.abs((last-System.nanoTime() + OPTIMAL_TIME)/1000000) );

        }
    }

    public void setNewGameMap(MapGenerator m){
        gameMap=new GameMap(player, enemies, m);
        player.setGameMap(gameMap);
        player.spawn();
        for(Entity e : enemies) {
            e.setGameMap(gameMap);
            e.spawn();
            if(e.getImage().equals("#")) e.setPath(new BidirectionalAStarShortestPath<Integer, DefaultEdge>(gameMap.getGraph()));
            else e.setPath(new BidirectionalAStarShortestPath<Integer, DefaultEdge>(gameMap.getGraph()));
            if(gameMap.lineOfSight(player.getCoords()[0], player.getCoords()[1], e.getX(), e.getY()))
                System.out.println("player is in the lof of "+ e.getImage());
        }
        player.showEvasionPath();

    }

    /**
     * The entry point to our example code
     *
     * @param argv The arguments passed into the program
     */
    public static void main(String[] argv) throws InterruptedException {

        final Game g = new Game();
        g.gameLoop();
       /* System.out.println("\nIndoor Map [120x120]\n");
       TestPerMap(new IndoorMapGenerator());
        System.out.println("\nOutdoor Map [120x120]\n");
        TestPerMap(new OutdoorMapGenerator());
        System.out.println("\nDungeon Map [120x120]\n");
        TestPerMap(new DungeonMapGenerator());*/
       //FinalTest();
    }

    private static Point chooseRandomPoint(GameMap map){
        int node = r.nextInt(GameMap.HEIGHT*GameMap.WIDTH);
        while(map.blocked(node%map.WIDTH, node/map.WIDTH))
            node=r.nextInt(GameMap.HEIGHT*GameMap.WIDTH);
        return new Point(node%map.WIDTH,node/map.WIDTH);
    }

    private static void TestPerMap(MapGenerator mapgnrtr){

        GameMap m = new GameMap(null,null, mapgnrtr);
        LinkedList<Point[]> points = new LinkedList<>();


        for(int i=0; i<20; i++) {
            Point a, b;
            //  AStarShortestPath<Integer, DefaultEdge> pathfinder = new AStarShortestPath<>(m.getGraph());
            DijkstraShortestPath<Integer, DefaultEdge> pathfinder;
            if (mapgnrtr.getClass() == IndoorMapGenerator.class) {
                do {
                    // System.out.println("1");

                    a = chooseRandomPoint(m);
                    b = chooseRandomPoint(m);
                    pathfinder = new DijkstraShortestPath<Integer, DefaultEdge>(m.getGraph(), a.toNode(), b.toNode());

                } while (pathfinder.getPath() == null);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);

            }else{
                a = chooseRandomPoint(m);
                b = chooseRandomPoint(m);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
        }
        Test t1 = new DijkstraTest(m, points);
        Test t2 = new AStarTest(m, points);
        Test t3 = new BidirectionalAstarTest(m, points);System.out.println("Testing Dijkstra...");
        t1.run(); System.out.println("Testing A*...");
        t2.run(); System.out.println("Testing Bidirectional A*...");
        t3.run();
        System.out.println("Dijkstra\nTotal Expandend Cell: "+t1.getTotalExpandedCells()+"; Average Expandend Cell: "+t1.getExpandedCellPerRun()+" ["+t1.getExpandedNodesStandardDeviation()+
                "]; Elapsed Time: "+t1.getTotalElapsedTime()+"ms; Average Elapsed Time: "+String.valueOf(t1.getElapsedTimePerRun())+"ms ["+t1.getTimeStandardDeviation()+"]");
        System.out.println("A*\nTotal Expandend Cell: "+t2.getTotalExpandedCells()+"; Average Expandend Cell: "+t2.getExpandedCellPerRun()+" ["+t2.getExpandedNodesStandardDeviation()+
                "]; Elapsed Time: "+t2.getTotalElapsedTime()+"ms; Average Elapsed Time: "+String.valueOf(t2.getElapsedTimePerRun())+"ms ["+t2.getTimeStandardDeviation()+"]");
        System.out.println("Bidirectional A*\nTotal Expandend Cell: "+t3.getTotalExpandedCells()+"; Average Expandend Cell: "+t3.getExpandedCellPerRun()+" ["+t3.getExpandedNodesStandardDeviation()+
                "]; Elapsed Time: "+t3.getTotalElapsedTime()+"ms; Average Elapsed Time: "+String.valueOf(t3.getElapsedTimePerRun())+"ms ["+t3.getTimeStandardDeviation()+"]");

    }

    public static void FinalTest(){
        LinkedList<GameMap> indoorMaps = new LinkedList<>();
        LinkedList<GameMap> outdoorMaps = new LinkedList<>();
        LinkedList<GameMap> dngMaps = new LinkedList<>();
        DijkstraShortestPath<Integer, DefaultEdge> pathfinder;
        HashMap<GameMap, LinkedList<Point[]>> mapToPoints = new HashMap<>();
        for(int i = 0; i<50; i++){
            GameMap m = new GameMap(null, null, new IndoorMapGenerator());
            indoorMaps.add(m);
            LinkedList<Point[]> points = new LinkedList<>();
            for(int j=0; j<200; j++){
                Point a, b;
                do {
                    // System.out.println("1");

                    a = chooseRandomPoint(m);
                    b = chooseRandomPoint(m);
                    pathfinder = new DijkstraShortestPath<Integer, DefaultEdge>(m.getGraph(), a.toNode(), b.toNode());

                } while (pathfinder.getPath() == null);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
            mapToPoints.put(m, points);
        }

        for(int i=0; i<50; i++){
            GameMap m = new GameMap(null, null, new OutdoorMapGenerator());
            outdoorMaps.add(m);
            LinkedList<Point[]> points = new LinkedList<>();
            for(int j = 0; j<200; j++){
                Point a = chooseRandomPoint(m);
                Point b = chooseRandomPoint(m);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
            mapToPoints.put(m, points);
        }

        for(int i=0; i<50; i++){
            GameMap m = new GameMap(null, null,  new DungeonMapGenerator());
            dngMaps.add(m);
            LinkedList<Point[]> points = new LinkedList<>();
            for(int j = 0; j<200; j++){
                Point a = chooseRandomPoint(m);
                Point b = chooseRandomPoint(m);
                Point[] pair = new Point[2];
                pair[0] = a;
                pair[1] = b;
                points.add(pair);
            }
            mapToPoints.put(m, points);
        }
        System.out.println("Testing 50 indoor maps [120x120], 20 points for each map");
        TestMaps(indoorMaps, mapToPoints);
        System.out.println("\nTesting 50 outdoor maps [120x120], 20 points for each map");
        TestMaps(outdoorMaps, mapToPoints);
        System.out.println("\nTesting 50 dungeon maps [120x120], 20 points for each map");
        TestMaps(dngMaps, mapToPoints);

    }

    public static void TestMaps(LinkedList<GameMap> maps, HashMap<GameMap, LinkedList<Point[]>> mapToPoints){
        LinkedList expNodesDijkstraAverages = new LinkedList<Double>();
        LinkedList expNodesAstarAverages = new LinkedList<Double>();
        LinkedList expNodesBiAstarAverages = new LinkedList<Double>();
        LinkedList ElapsTimeDijkstraAverages = new LinkedList<Double>();
        LinkedList ElapsTimeAstarAverages = new LinkedList<Double>();
        LinkedList ElapsTimeBiAstarAverages = new LinkedList<Double>();

        for(GameMap m : maps){
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
        double djkstrTotExp = getTot(expNodesDijkstraAverages);
        double astarTotExp = getTot(expNodesAstarAverages);
        double biastarTotExp = getTot(expNodesBiAstarAverages);
        System.out.println("Dijkstra\nTotal Expandend Cell: "+djkstrTotExp+"; Average Expandend Cell: "+getAvg(expNodesDijkstraAverages)+" ["+getStandardDeviation(expNodesDijkstraAverages,getAvg(expNodesDijkstraAverages))+
                "]; Elapsed Time: "+getTot(ElapsTimeDijkstraAverages)+"ms; Average Elapsed Time: "+getAvg(ElapsTimeDijkstraAverages)+"ms ["+getStandardDeviation(ElapsTimeDijkstraAverages,getAvg(ElapsTimeDijkstraAverages))+"]");
        System.out.println("A*\nTotal Expandend Cell: "+astarTotExp+"; Average Expandend Cell: "+getAvg(expNodesAstarAverages)+" ["+getStandardDeviation(expNodesAstarAverages,getAvg(expNodesAstarAverages))+
                "]; Elapsed Time: "+getTot(ElapsTimeAstarAverages)+"ms; Average Elapsed Time: "+getAvg(ElapsTimeAstarAverages)+"ms ["+getStandardDeviation(ElapsTimeAstarAverages,getAvg(ElapsTimeAstarAverages))+"]");
        System.out.println("Bidirectional A*\nTotal Expandend Cell: "+biastarTotExp+"; Average Expandend Cell: "+getAvg(expNodesBiAstarAverages)+" ["+getStandardDeviation(expNodesBiAstarAverages,getAvg(expNodesBiAstarAverages))+
                "]; Elapsed Time: "+getTot(ElapsTimeBiAstarAverages)+"ms; Average Elapsed Time: "+getAvg(ElapsTimeBiAstarAverages)+"ms ["+getStandardDeviation(ElapsTimeBiAstarAverages,getAvg(ElapsTimeBiAstarAverages))+"]");
    }

    private static double getTot(LinkedList<Double> l) {
        double d=0;
        for(Double i : l)
            d+=i;
        return d;
    }

    public static double getAvg(LinkedList<Double> l){
        double d=0;
        for(Double i : l){
            d += i;
        }
        return d/l.size();
    }

    private static double getVariance(LinkedList<Double> l, double avg){
        double d=0;
        for(Double i : l) {
            if(i<0) System.out.println("osservazione negativa");
            d += (i - avg) * (i - avg);
        }
        return d/(l.size()-1);
    }

    private static double getStandardDeviation(LinkedList<Double> l, double avg){
        return Math.sqrt(getVariance(l,avg));
    }
}
