package tilemap;

import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Hp Dell i7 on 07/06/2016.
 */
public class Player {
    private int x;
    /** The y position of this entity in terms of grid cells */
    private int y;
    /** The image to draw for this entity */
    private String image;
    private GameMap gameMap;

    private Integer previousEvadeFrom=-1;


    private Random r = new Random(gameMap.WIDTH*gameMap.HEIGHT);

    private boolean showEvasionPath = false;
    private boolean verbose = false;
    private List<Integer> evasionPath;


    public Player(String s) {
        image = s;
        r.setSeed(System.currentTimeMillis());
    }

    public void setEvasionPathVisible(){
        this.showEvasionPath=true;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void spawn() {
        int x,y;
        do{
            int aux=r.nextInt();
            x= aux % gameMap.WIDTH;
            y=aux/gameMap.WIDTH;
        }while(gameMap.blocked(x,y));

        this.x=x;
        this.y=y;
    }

    public void paint(Graphics2D g) {
            // work out the screen position of the entity based on the
            // x/y position and the size that tiles are being rendered at. So
            // if we're at 1.5,1.5 and the tile size is 10 we'd render on screen
            // at 15,15.
            int xp = (int) (Tile.TILE_SIZE * x + Tile.TILE_SIZE/2);
            int yp = (int) (Tile.TILE_SIZE * y + Tile.TILE_SIZE/2);
            g.setColor(Color.BLACK);

            // rotate the sprite based on the current angle and then
            // draw it
            //g.rotate(ang, xp, yp);
            g.drawString(image, xp, yp);

            //g.drawImage(image, (int) (xp - 16), (int) (yp - 16), null);
            //g.rotate(-ang, xp, yp);
        //if(evasionPath!=null)
        if(evasionPath != null){
            g.setColor(Color.GREEN);
            Iterator<Integer> i = evasionPath.iterator();
            int current_x = evasionPath.get(0) % GameMap.WIDTH;
            int current_y = evasionPath.get(0) / GameMap.WIDTH;
            while (i.hasNext()) {
                Integer next = i.next();
                int next_x = next % GameMap.WIDTH;
                int next_y = next / GameMap.WIDTH;
                g.drawLine(current_x * Tile.TILE_SIZE + Tile.TILE_SIZE+Tile.TILE_SIZE/2, current_y * Tile.TILE_SIZE-Tile.TILE_SIZE/2, next_x * Tile.TILE_SIZE + Tile.TILE_SIZE + Tile.TILE_SIZE/2, next_y * Tile.TILE_SIZE - Tile.TILE_SIZE/2);
                current_x = next_x;
                current_y = next_y;
            }
        }
    }


    public int[] getCoords() {
        int[] coords = new int[2];
        coords[0]=x; coords[1]=y;
        return coords;
    }

    /**
     * Check if the entity would be at a valid location if its position
     * was as specified
     *
     * @param nx The potential x position for the entity
     * @param ny The potential y position for the entity
     * @return True if the new position specified would be valid
     */
    public boolean validLocation(int nx, int ny) {
        // here we're going to check some points at the corners of
        // the player to see whether we're at an invalid location
        // if any of them are blocked then the location specified
        // isn't valid
        if (gameMap.blocked(nx, ny)) {
            return false;
        }
        if(nx>=GameMap.WIDTH || ny >GameMap.HEIGHT || nx<0 || ny<0)
            return false;
        /*if (gameMap.blocked(nx + size, ny - size)) {
            return false;
        }
        if (gameMap.blocked(nx - size, ny + size)) {
            return false;
        }
        if (gameMap.blocked(nx + size, ny + size)) {
            return false;
        }*/

        // if all the points checked are unblocked then we're in an ok
        // location
        return true;
    }

    public boolean move(int dx, int dy) {
        // work out what the new position of this entity will be
        int nx = x + dx;
        int ny = y + dy;

        // check if the new position of the entity collides with
        // anything
        if (validLocation(nx, ny) &&  GameMap.lineOfSight(x,y,nx,ny)) {
            // if it doesn't then change our position to the new position
            x = nx;
            y = ny;
//System.out.println("Position: "+x+","+y+"blocked: "+gameMap.getData()[x][y].isBlocked);
            // and calculate the angle we're facing based on our last move
            //ang = (float) (Math.atan2(dy, dx) - (Math.PI / 2));
            return true;
        }
        // if it wasn't a valid move don't do anything apart from
        // tell the caller
        return false;
    }

    public int getNode(){
        return gameMap.WIDTH*y +x;
    }

    private void uncolorPath() {
        Tile[][] map = gameMap.getData();
        if (evasionPath == null) return;
        for (Integer i : evasionPath)
            map[i % gameMap.WIDTH][i / gameMap.WIDTH].isEvasion = false;
    }

    public void setEvasionPath(int node) {
        if(!showEvasionPath) return;
        if(node == previousEvadeFrom)
            return;
        previousEvadeFrom = node;

         GraphPath<Integer,DefaultWeightedEdge> p = new Trailmax<>(gameMap.getGraph()).getShortestPath( node, getNode(),null);
        if (p == null) return;
        evasionPath = Graphs.getPathVertexList(p);
        //pathToFollow.remove(0);
        //colorPath(pathToFollow);
        if(verbose) report(p);
        //nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
    }

    private void report(GraphPath<Integer, DefaultWeightedEdge> p) {
        System.out.println("EvasionPath: "+p.toString());
    }

    private void colorEvasionPath() {
        Tile[][] map = gameMap.getData();
        for(Integer i : evasionPath)
                map[i%gameMap.WIDTH][i/gameMap.WIDTH].isEvasion=true;
//        for(Integer i : Graphs.getPathVertexList(ThetaStarpath))
//            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isThetaPath=true;
    }

    public void setVerbose() {
        this.verbose= true;
    }
}
