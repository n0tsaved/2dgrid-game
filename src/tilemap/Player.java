package tilemap;

import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultEdge;

import java.awt.*;
import java.util.LinkedList;
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


    private Random r = new Random(gameMap.WIDTH*gameMap.HEIGHT);
    
    public Player(String s) {
        image = s;
        r.setSeed(System.currentTimeMillis());
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
            int xp = (int) (Tile.TILE_SIZE * x);
            int yp = (int) (Tile.TILE_SIZE * y);
            g.setColor(Color.BLACK);

            // rotate the sprite based on the current angle and then
            // draw it
            //g.rotate(ang, xp, yp);
            g.drawString(image, xp, yp);

            //g.drawImage(image, (int) (xp - 16), (int) (yp - 16), null);
            //g.rotate(-ang, xp, yp);
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
        if (validLocation(nx, ny)) {
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

    public void showEvasionPath(){
        LinkedList<Entity> enemies = gameMap.getEnemies();
        Entity e = enemies.getFirst();
        Integer agentNode = e.getNode();
        Integer targetNode = this.getNode();
        Trailmax<Integer, DefaultEdge> t = new Trailmax<>(gameMap.getGraph());
        GraphPath<Integer, DefaultEdge> p = t.getShortestPath(agentNode, targetNode, null);
        colorEvasionPath(p, e);
    }

    private void colorEvasionPath(GraphPath<Integer, DefaultEdge> p, Entity e) {
        Tile[][] map = gameMap.getData();
        if(p == null) return;
        System.out.println("player evades enemy "+e.getImage());
        System.out.println(p.toString());
        for(Integer i : Graphs.getPathVertexList(p))
                map[i%gameMap.WIDTH][i/gameMap.WIDTH].isEvasion=true;
//        for(Integer i : Graphs.getPathVertexList(ThetaStarpath))
//            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isThetaPath=true;
    }
}
