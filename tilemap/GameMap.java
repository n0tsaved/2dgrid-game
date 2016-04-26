package tilemap;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class GameMap {

    /** The width in grid cells of our map */
    public static final int WIDTH = 60;
    /** The height in grid cells of our map */
    public static final int HEIGHT = 45;

    /** The rendered size of the tile (in pixels) */


    /** The actual data for our map */
    private Tile[][] data;

    private AdjacencyMatrix graph;

    private ArrayList<Room> rooms;
    private ArrayList<Obstacle> obstcls;

    /**
     * Create a new map with some default contents
     */
    public GameMap() {
        data=new Tile[WIDTH][HEIGHT];
        graph= new AdjacencyMatrix(WIDTH*HEIGHT);
        rooms=new ArrayList<>();
        obstcls=new ArrayList<>();
        //IndoorMapGenerator mgnrt = new IndoorMapGenerator();
        Game.mapGnrtr.generate(this);
        generateGraph();
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

    private void generateGraph() {
        for(int i=0;i<Game.WIDTH;i++)
            for(int j=0;j<Game.HEIGHT;j++) {
                if (blocked(i, j)) continue;
                if(i>0 && !blocked(i-1, j)) //add sx
                    graph.addEdge(toNode(i,j), toNode(i-1,j));
                if(j>0 && !blocked(i,j-1)) //add up
                    graph.addEdge(toNode(i,j),toNode(i,j-1));
                if(j>0&&i>0&&!blocked(i-1,j-1)) //add sxup
                    graph.addEdge(toNode(i,j), toNode(i-1,j-1));
                if(j>0&&i<WIDTH&&!blocked(i+1,j-1)) // add dxup
                    graph.addEdge(toNode(i,j), toNode(i+1,j-1));
                if(i<WIDTH&&!blocked(i+1,j)) //add dx
                    graph.addEdge(toNode(i,j), toNode(i+1,j));
                if(i<WIDTH&&j<HEIGHT&&!blocked(i+1,j+1)) //add dxdown
                    graph.addEdge(toNode(i,j), toNode(i+1,j+1));
                if(j<HEIGHT&&!blocked(i,j+1)) //add down
                    graph.addEdge(toNode(i,j), toNode(i,j+1));
                if(j<HEIGHT&&i>0&&!blocked(i-1,j+1)) //add sxdown
                    graph.addEdge(toNode(i,j), toNode(i-1,j+1));
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
    public boolean blocked(int x, int y) {
        // look up the right cell (based on simply rounding the floating
        // values) and check the value
        if(x>= 0 && y >=0 && x<WIDTH && y<HEIGHT) return data[x][y].isBlocked==true;
            //return !grid.isTraversable(x,y);
        else return false;

    }

    public boolean addRoom(Object o){
        if(o.getClass()!=Room.class) return false;
        Room room=(Room) o;
        for (Room r : rooms)
            if(room.intersect(r)) return false;
        //grid.setTraversable(room);
        for (int i = room.x; i < room.x + room.width; i++)
            for (int j = room.y; j < room.y + room.height; j++) {
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
        for (int i = obst.x; i < obst.x + obst.width; i++)
            for (int j = obst.y; j < obst.y + obst.height; j++) {
                data[i][j].isBlocked = false;
            }
        return obstcls.add(obst);
    }
}
