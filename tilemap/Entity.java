package tilemap;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.AStarShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by merda on 25/05/16.
 */
public class Entity {
    /** The x position of this entity in terms of grid cells */
    private int x;
    /** The y position of this entity in terms of grid cells */
    private int y;
    /** The image to draw for this entity */
    private String image;
    /** The gameMap which this entity is wandering around */
    private GameMap gameMap;
    private static Random r = new Random(GameMap.WIDTH*GameMap.HEIGHT);
    private AStarShortestPath<Integer, DefaultWeightedEdge> astarPathFind;
    private DijkstraShortestPath<Integer, DefaultWeightedEdge> dijkstraPathFind;
    private GraphPath<Integer, DefaultWeightedEdge> path;

    public Entity(String s, GameMap m){
        image=s;
        gameMap=m;
        spawn();
        //pathfinder=new AStarShortestPath<>(gameMap.getGraph());
        //path=pathfinder.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        dijkstraPathFind = new DijkstraShortestPath<>(gameMap.getGraph(),
                y*GameMap.WIDTH+x, gameMap.getPlayerNode());
        //System.out.println(ptfnd.getPath().getEdgeList().toString());
        Tile[][] map = gameMap.getData();
        path= dijkstraPathFind.getPath();
        if(path==null) return;
        for(Integer i : Graphs.getPathVertexList(path))
            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isPath=true;

        System.out.println(path.toString());
    }

    public void setPath(){

        //astarPathFind=new AStarShortestPath<>(gameMap.getGraph());
        //path=astarPathFind.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        //System.out.println(path.toString());
        path=new DijkstraShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph(),
                y*GameMap.WIDTH+x, gameMap.getPlayerNode()).getPath();
        Tile[][] map = gameMap.getData();
        if(path == null) return;
        for(Integer i : Graphs.getPathVertexList(path))
            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isPath=true;
        System.out.println(path.toString());

    }

    public void spawn(){
        int node = r.nextInt();
        while(gameMap.blocked(node%gameMap.WIDTH, node/gameMap.WIDTH))
            node=r.nextInt();
        this.x=node%gameMap.WIDTH;
        this.y=node/gameMap.WIDTH;
        gameMap.getData()[x][y].isStart=true;
    }
    public void setGameMap(GameMap m){
        gameMap=m;
    }
    public void paint(Graphics2D g) {
        // work out the screen position of the entity based on the
        // x/y position and the size that tiles are being rendered at. So
        // if we're at 1.5,1.5 and the tile size is 10 we'd render on screen
        // at 15,15.
        int xp = (int) (Tile.TILE_SIZE * x);
        int yp = (int) (Tile.TILE_SIZE * y);
        g.setColor(Color.BLUE);

        // rotate the sprite based on the current angle and then
        // draw it
        //g.rotate(ang, xp, yp);
        g.drawString(image, xp, yp);

        //g.drawImage(image, (int) (xp - 16), (int) (yp - 16), null);
        //g.rotate(-ang, xp, yp);
    }

    private class ManhattanDistance implements AStarAdmissibleHeuristic<Integer>{

        @Override
        public double getCostEstimate(Integer sourceVertex, Integer targetVertex) {
            int sourceX, sourceY, targetX, targetY;
            sourceX=sourceVertex%GameMap.WIDTH;
            sourceY=sourceVertex/GameMap.WIDTH;
            targetX=targetVertex%GameMap.WIDTH;
            targetY=targetVertex/GameMap.WIDTH;
            return Math.sqrt(Math.pow(sourceX- targetX,2)+Math.pow(sourceY- targetY,2));
        }
    }
}
