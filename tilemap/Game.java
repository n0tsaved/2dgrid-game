package tilemap;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Game extends Canvas{
    private long lastFpsTime;
    /** The buffered strategy used for accelerated rendering */
    private BufferStrategy strategy;


    /** The gameMap our player will wander round */
    /** The player entity that will be controlled with cursors */
    private Entity player;

    private GameController controller;
    private int fps;
    public static MapGenerator mapGnrtr=new IndoorMapGenerator();
    private static final GameMap gameMap = new GameMap();

    public Game() {


        // create the AWT frame. Its going to be fixed size (500x500)
        // and not resizable - this just gives us less to account for
        Frame frame = new Frame("Tile GameMap");
        frame.setLayout(null);
        setBounds(0,0,800,600);
        frame.add(this);
        frame.setSize(800,600);
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
        if(mapGnrtr.getClass() == IndoorMapGenerator.class) {
            int[] spawnCoords = gameMap.getRooms().get(0).center();
            player = new Entity("@", gameMap, spawnCoords[0], spawnCoords[1]);
        }else player = new Entity("@", gameMap, 3, 3);
        controller= new GameController( player);
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

        // keep looking while the game is running
        while (gameRunning) {
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

            // clear the screen
            g.setColor(Color.black);
            g.fillRect(0,0,800,600);

            // render our game objects
            g.translate(100,100);
            gameMap.paint(g);
            player.paint(g);

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
            if (lastFpsTime >= 1000000000)
            {
                System.out.println("(FPS: "+fps+")");
                lastFpsTime = 0;
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
            if ((delta % 5) != 0) {
                controller.logic(delta % 5);
            }
            Thread.sleep( Math.abs((last-System.nanoTime() + OPTIMAL_TIME)/1000000) );

        }
    }


    /**
     * The entry point to our example code
     *
     * @param argv The arguments passed into the program
     */
    public static void main(String[] argv) throws InterruptedException {

        final Game g = new Game();
        g.gameLoop();
    }
}