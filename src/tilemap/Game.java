package tilemap;


import org.apache.commons.cli.*;
import tilemap.jgrapht.alg.*;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.DefaultWeightedEdge;


import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.*;
import java.util.List;

public class Game extends Canvas{

    private long lastFpsTime;
    /** The buffered strategy used for accelerated rendering */
    private BufferStrategy strategy;
    /** The player entity that will be controlled with cursors */
    private Player player = new Player("☻");

    private LinkedList<Entity> enemies = new LinkedList<>();
    /** The gameMap our player will wander round */
    private GameMap gameMap = new GameMap(player, enemies, new IndoorMapGenerator());



    private GameController controller;

    private static final int DIJKSTRA_INT = 1;
    private static final int ASTAR_INT = 2;
    private static final int THETA_INT = 3;
    private static final int BIDI_INT = 4;
    private static final int AASTAR_INT = 5;
    private static final int CHASE_BEHAVIOUR = 1;
    private static final int EVADE_BEHAVIOUR = 2;
    private static final String SPRITES = "♈♉☠☢☣♿♻♝⚉⚛⚚☘☎♞⛷⚡☃☸♣♠♥♦♰";
    private static final Random rnd = new Random();

    private int currentAlgo;
    private int currentBehaviour;
    private boolean verbose;

    public Game(int algoType, int numberOfEntities, boolean showTrailmax, int behaviourType, boolean verbose) {

        // create the AWT frame. Its going to be fixed size (500x500)
        // and not resizable - this just gives us less to account for
        Frame frame = new Frame("Tile GameMap");
        frame.setLayout(null);
        setBounds(0,0,800,800);
        frame.add(this);
        frame.setSize(800,800);
        frame.setResizable(true);

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
        currentAlgo = algoType;
        this.verbose = verbose;
        if(verbose) player.setVerbose();
        if(showTrailmax) player.setEvasionPathVisible();
        for(int i = 0; i < numberOfEntities; i++){
            Pathfinder<Integer, DefaultWeightedEdge> p = null;
            if(currentAlgo == DIJKSTRA_INT)
                p = new DijkstraShortestPath<>(gameMap.getGraph());
            else if (currentAlgo == ASTAR_INT)
                p = new AStarShortestPath<>(gameMap.getGraph());
            else if (currentAlgo == BIDI_INT)
                p = new BidirectionalAStarShortestPath<>(gameMap.getGraph());
            else if (currentAlgo == AASTAR_INT)
                p = new LazyMovingTargetAdaptiveAStarShortestPath<>(gameMap.getGraph());
            char sprite = SPRITES.charAt(rnd.nextInt(SPRITES.length()));
            char[] arraySprite = new char[1];
            arraySprite[0] = sprite;
            Entity e = new Entity(new String(arraySprite), gameMap, p);
            if(currentAlgo == THETA_INT)
                e.setThetaPath();
            if(verbose == true) {
                e.setVerbose();
                //p.setVerbose();
            }
            if(behaviourType!=0) {
                if (behaviourType == EVADE_BEHAVIOUR) {
                    Trailmax<Integer, DefaultWeightedEdge> t = new Trailmax<>(gameMap.getGraph());
                    if (verbose) t.setVerbose();
                    e.setBehaviour(new FleeBehaviour(e, player, t));
                } else if (behaviourType == CHASE_BEHAVIOUR) {
                    LazyMovingTargetAdaptiveAStarShortestPath<Integer, DefaultWeightedEdge> l = new LazyMovingTargetAdaptiveAStarShortestPath<>(gameMap.getGraph());
                    if (verbose) l.setVerbose();
                    e.setBehaviour(new FollowBehaviour(e, player, l));
                }
                currentBehaviour = behaviourType;
            }
            enemies.add(e);

        }

        //enemies.add(new Entity("#", gameMap, new BidirectionalAStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph())));
        //enemies.add(new Entity("§", gameMap, new BidirectionalAStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph())));
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
            for(Entity e : enemies) {

                e.paint(g);
            }
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

            while (lastFpsTime >= 1000000000/2)
            {

                //System.out.println("(FPS: "+fps+")");

                lastFpsTime -= 1000000000/2;

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
            // after we've runStationaryTest through the segments if there is anything
            // left over we update for that
            if ((delta % 5) != 0)
                controller.logic(delta % 5);
            //}
            //player.showEvasionPath();
            for(Entity e : enemies)
                e.update(delta);
            player.setEvasionPath(enemies.getFirst().getNode());
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
            Pathfinder<Integer, DefaultWeightedEdge> p = null;
            if(currentAlgo == DIJKSTRA_INT)
                p = new DijkstraShortestPath<>(gameMap.getGraph());
            else if (currentAlgo == ASTAR_INT)
                p = new AStarShortestPath<>(gameMap.getGraph());
            else if (currentAlgo == BIDI_INT)
                p = new BidirectionalAStarShortestPath<>(gameMap.getGraph());
            else if (currentAlgo == AASTAR_INT)
                p = new LazyMovingTargetAdaptiveAStarShortestPath<>(gameMap.getGraph());
            if(currentAlgo == THETA_INT)
                e.setThetaPath();

            if(currentBehaviour!=0) {
                if (currentBehaviour == EVADE_BEHAVIOUR) {
                    Trailmax<Integer, DefaultWeightedEdge> t = new Trailmax<>(gameMap.getGraph());
                    if (verbose) t.setVerbose();
                    e.setBehaviour(new FleeBehaviour(e, player, t));
                } else if (currentBehaviour == CHASE_BEHAVIOUR) {
                    LazyMovingTargetAdaptiveAStarShortestPath<Integer, DefaultWeightedEdge> l = new LazyMovingTargetAdaptiveAStarShortestPath<>(gameMap.getGraph());
                    if (verbose) l.setVerbose();
                    e.setBehaviour(new FollowBehaviour(e, player, l));
                }
            }
            e.setPathfinder(p);
            if(verbose == true) {
                e.setVerbose();
                //p.setVerbose();
            }
            if(gameMap.lineOfSight(player.getCoords()[0], player.getCoords()[1], (int) e.getX(),(int) e.getY()))
                if (verbose) System.out.println("player is in the lof of "+ e.getImage());
        }
        //Entity e1 = enemies.getFirst();
        //Entity e2 = enemies.getLast();
        //e1.setBehaviour(new FollowBehaviour(e1, e2, new LazyMovingTargetAdaptiveAStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph())));
        //e2.setBehaviour(new FleeBehaviour(e2,e1, new Trailmax<Integer, DefaultWeightedEdge>(gameMap.getGraph())));
        //player.showEvasionPath();

    }

    /**
     * The entry point to our example code
     *
     * @param argv The arguments passed into the program
     */
    public static void main(String[] argv) throws InterruptedException {

        Option helpOpt = Option.builder("h")
                .longOpt("help")
                .desc("print this help")
                .build();
        Option algOpt = Option.builder("a")
                .longOpt("algo")
                .desc("set algorithm to use in the simulation")
                .hasArg()
                .argName("algorithm")
                .build();
        Option entOpt = Option.builder("e")
                .longOpt("entities")
                .desc("set how many entities in the simulation")
                .hasArg()
                .argName("numberOfEntities")
                .build();
        Option trailmaxOpt = Option.builder("t")
                .longOpt("show-trailmax")
                .desc("show trailmax path")
                .build();
        Option behaviourOpt = Option.builder("b")
                .longOpt("behaviour")
                .desc("set behaviour type")
                .hasArg()
                .argName("behaviourtype")
                .build();
        Option verboseOpt = Option.builder("v")
                .longOpt("verbose")
                .desc("run in verbose mode")
                .build();
        Options options = new Options();

        options.addOption(helpOpt);
        options.addOption(algOpt);
        options.addOption(entOpt);
        options.addOption(trailmaxOpt);
        options.addOption(behaviourOpt);
        options.addOption(verboseOpt);

        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, argv );

            if(line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("2dgrid-game", options);
                System.exit(0);
            }
            int pathfindertype=DIJKSTRA_INT;
            if(line.hasOption("algo")){
                String algo = line.getOptionValue("algo");
                switch(algo){
                    case "dijkstra":
                        pathfindertype = DIJKSTRA_INT;
                        break;
                    case "astar":
                        pathfindertype = ASTAR_INT;
                        break;
                    case "theta":
                        pathfindertype = THETA_INT;
                        break;
                    case "bidi":
                        pathfindertype = BIDI_INT;
                        break;
                    case "aastar":
                        pathfindertype = AASTAR_INT;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid algorithm name: "+algo);
                }
            }
            int nentities =1;
            if(line.hasOption("entities")) {
                nentities = Integer.parseInt(line.getOptionValue("entities"));
                if (nentities <= 0 || nentities > 20)
                    throw new IllegalArgumentException("Invalid number of entities: " + nentities);
            }
            boolean showtrailmax = false;
            if(line.hasOption("show-trailmax"))
                showtrailmax = true;
            int behaviourType = 0;
            if(line.hasOption("behaviour")){
                String behaviourStr = line.getOptionValue("behaviour");
                switch(behaviourStr) {
                    case "evade":
                        behaviourType = EVADE_BEHAVIOUR;
                        break;
                    case "chase":
                        behaviourType = CHASE_BEHAVIOUR;
                        break;
                }
            }

            boolean verbose = false;
            if(line.hasOption("verbose"))
                verbose = true;

            Game g = new Game(pathfindertype, nentities, showtrailmax, behaviourType, verbose);
            g.gameLoop();

        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }



        //final Game g = new Game(pathfindertype, );
        //g.gameLoop();
        //GlobalTest t = new GlobalTest();
        //t.runStationaryTest();
        //t.runMovingTest();
    }
}
