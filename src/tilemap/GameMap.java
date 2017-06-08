package tilemap;



import tilemap.jgrapht.graph.DefaultWeightedEdge;
import tilemap.jgrapht.graph.SimpleWeightedGraph;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
//import tilemap.jgrapht.ext.*;

public class GameMap {

    /** The width in grid cells of our map */
    public static final int WIDTH = 128;
    /** The height in grid cells of our map */
    public static final int HEIGHT = 128;

    /** The rendered size of the tile (in pixels) */


    /** The actual data for our map */
    public static Tile[][] data=new Tile[WIDTH][HEIGHT];
    public  MapGenerator mapGnrtr;



    private Player player;
    private LinkedList<Entity> enemies;

    private ArrayList<Room> rooms;
    private ArrayList<Obstacle> obstcls;
    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> completeGraph;
    private static final double SQRT2 = Math.sqrt(2);

    /**
     * Create a new map with some default contents
     */
    public GameMap(Player p, LinkedList<Entity> e, MapGenerator m) {
        rooms=new ArrayList<>();
        obstcls=new ArrayList<>();
        player=p;
        enemies = e;
        //DungeonMapGenerator mgnrt = new DungeonMapGenerator();
        mapGnrtr=m;
        mapGnrtr.generate(this);
        completeGraph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        for(int i=0; i< WIDTH*HEIGHT; i++) {
            Integer t = new Integer(i);
            if (!blocked(t%WIDTH, t/WIDTH))
                completeGraph.addVertex(t);
        }
        connectGraphVertex();

    //System.out.println(completeGraph.toString());
        // create some default map data - it would be way
        // cooler to load this from a file and maybe provide
        // a map editor of some sort, but since we're just doing
        // a simple tutorial here we'll manually fill the data
        // with a simple little map
        /*for (int y=0;y<HEIGHT;y++) {
            data[0][y] = BLOCKED;
            data[2][y] = BLOCKED;
            data[7][y] = BLOCKED;
            data[11][y] = BLOCKED;
            data[WIDTH-1][y] = BLOCKED;
        }
        for (int x=0;x<WIDTH;x++) {
            if ((x > 0) && (x < WIDTH-1)) {
                data[x][10] = CLEAR;
            }

            if (x > 2) {
                data[x][9] = BLOCKED;
            }
            data[x][0] = BLOCKED;
            data[x][HEIGHT-1] = BLOCKED;
        }

        data[4][9] = CLEAR;
        data[7][5] = CLEAR;
        data[7][4] = CLEAR;
        data[11][7] = CLEAR;*/
    }


    private void connectGraphVertex() {
        for(int i=0;i<WIDTH;i++) {
            for (int j = 0; j < HEIGHT; j++) {
                //System.out.println("pivot (" + i + "," + j + ")");
                if (blocked(i, j)) continue;
                if (!blocked(i - 1, j)) //add sx
                    completeGraph.addEdge(toNode(i, j), toNode(i - 1, j), new DefaultWeightedEdge());
                if (!blocked(i, j - 1)) //add up
                    completeGraph.addEdge(toNode(i, j), toNode(i, j - 1), new DefaultWeightedEdge());
                if (!blocked(i - 1, j - 1) ) { //add sxup
                    if(!blocked(i-1,j) && !blocked(i,j-1)) {
                        DefaultWeightedEdge e = new DefaultWeightedEdge();
                        completeGraph.setEdgeWeight(e, SQRT2);
                        completeGraph.addEdge(toNode(i, j), toNode(i - 1, j - 1), e);
                    }
                }
                if (!blocked(i + 1, j - 1)) { // add dxup
                    if(!blocked(i, j-1) && !blocked(i+1, j)) {
                        DefaultWeightedEdge e = new DefaultWeightedEdge();
                        completeGraph.setEdgeWeight(e, SQRT2);
                        completeGraph.addEdge(toNode(i, j), toNode(i + 1, j - 1), e);
                    }
                }
                if (!blocked(i + 1, j)) //add dx
                    completeGraph.addEdge(toNode(i, j), toNode(i + 1, j), new DefaultWeightedEdge());
                if (!blocked(i + 1, j + 1)){ //add dxdown
                    if(!blocked(i+1, j) && !blocked(i, j+1)) {
                        DefaultWeightedEdge e = new DefaultWeightedEdge();
                        completeGraph.setEdgeWeight(e, SQRT2);
                        completeGraph.addEdge(toNode(i, j), toNode(i + 1, j + 1), e);
                    }
            }
                if (!blocked(i, j + 1)) //add down
                    completeGraph.addEdge(toNode(i, j), toNode(i, j + 1), new DefaultWeightedEdge());
                if (!blocked(i - 1, j + 1)) { //add sxdown
                    if(!blocked(i-1, j) && !blocked(i, j+1)) {
                        DefaultWeightedEdge e = new DefaultWeightedEdge();
                        completeGraph.setEdgeWeight(e, SQRT2);
                        completeGraph.addEdge(toNode(i, j), toNode(i - 1, j + 1), e);
                    }
                }
            }
        }
    }



    private int toNode(int x, int y) {
        return WIDTH*y+x;
    }

    /**
     * Render the map to the graphics context provided. The rendering
     * is just simple fill rectangles
     *
     * @param g The graphics context on which to draw the map
     */
    public void paint(Graphics2D g) {
        // loop through all the tiles in the map rendering them
        // based on whether they block or not
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                data[x][y].paint(g, x, y);
            }
        }
        //grid.paint(g);
    }

    /**
     * Check if a particular location on the map is blocked. Note
     * that the x and y parameters are floating point numbers meaning
     * that we can be checking partially across a grid cell.
     *
     * @param x The x position to check for blocking
     * @param y The y position to check for blocking
     * @return True if the location is blocked
     */
    public static boolean blocked(int x, int y) {
        // look up the right cell (based on simply rounding the floating
        // values) and check the value
        if(x>= 0 && y >=0 && x<WIDTH && y<HEIGHT){
            //if(data[x][y].isBlocked==true) System.out.println("("+x+","+y+") is blocked");
            //else System.out.println("("+x+","+y+") is not blocked");
            return GameMap.data[x][y].isBlocked==true;
        }
            //return !grid.isTraversable(x,y);
        else return true;

    }

    public boolean addRoom(Object o){
        if(o.getClass()!=Room.class) return false;
        Room room=(Room) o;
        for (Room r : rooms)
            if(room.intersect(r)) return false;
        //grid.setTraversable(room);
        for (int i = room.getX(); i < room.getX() + room.getWidth(); i++)
            for (int j = room.getY(); j < room.getY() + room.getHeight(); j++) {
                data[i][j].isBlocked = false;
            }
        return rooms.add(room);
    }

    public Tile[][] getData(){
        return data;
    }

    public ArrayList<Room> getRooms(){
        return rooms;
    }


    public boolean addObstcl(Object o) {
        if(o.getClass()!=Obstacle.class) return false;
        Obstacle obst = (Obstacle) o;
        for(Obstacle x : obstcls)
            if(obst.intersect(x)) return false;
        //grid.setNotTraversable(obst);
        for (int i = obst.getX(); i < obst.getX() + obst.getWidth(); i++)
            for (int j = obst.getY(); j < obst.getY() + obst.getHeight(); j++) {
                data[i][j].isBlocked = true;
            }
        return obstcls.add(obst);
    }

    public SimpleWeightedGraph<Integer, DefaultWeightedEdge> getGraph() {
        return completeGraph;
    }

    public int getPlayerNode() {
        int[] coords = player.getCoords();
        return toNode(coords[0], coords[1]);
    }
     public LinkedList<Entity> getEnemies(){
        return enemies;
     }

    public static boolean lineOfSight(int x1, int y1, int x2, int y2) {
        int dy = y2 - y1;
        int dx = x2 - x1;

        int f = 0;

        int signY = 1;
        int signX = 1;
        int offsetX = 0;
        int offsetY = 0;

        if (dy < 0) {
            dy *= -1;
            signY = -1;
            offsetY = -1;
        }
        if (dx < 0) {
            dx *= -1;
            signX = -1;
            offsetX = -1;
        }

        if (dx >= dy) {
            while (x1 != x2) {
                f += dy;
                if (f >= dx) {
                    if (blocked(x1 + offsetX, y1 + offsetY))
                        return false;
                    y1 += signY;
                    f -= dx;
                }
                if (f != 0 && blocked(x1 + offsetX, y1 + offsetY))
                    return false;
                if (dy == 0 && blocked(x1 + offsetX, y1) && blocked(x1 + offsetX, y1 - 1))
                    return false;

                x1 += signX;
            }
        }
        else {
            while (y1 != y2) {
                f += dx;
                if (f >= dy) {
                    if (blocked(x1 + offsetX, y1 + offsetY))
                        return false;
                    x1 += signX;
                    f -= dy;
                }
                if (f != 0 && blocked(x1 + offsetX, y1 + offsetY))
                    return false;
                if (dx == 0 && blocked(x1, y1 + offsetY) && blocked(x1 - 1, y1 + offsetY))
                    return false;

                y1 += signY;
            }
        }
        return true;
    }
}
