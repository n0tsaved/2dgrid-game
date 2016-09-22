package tilemap;



import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.AStarShortestPath;
import tilemap.jgrapht.alg.DijkstraShortestPath;
import tilemap.jgrapht.alg.ThetaStarShortestPath;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.util.Iterator;
import java.util.Random;


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
    private ThetaStarShortestPath<Integer,DefaultWeightedEdge> thetaPathFind;
    private GraphPath<Integer, DefaultWeightedEdge> AStarpath;
    private GraphPath<Integer, DefaultWeightedEdge> ThetaStarpath;

    private Pathfinder pathfinder;

    public Entity(String s, GameMap m, Pathfinder p){
        image=s;
        gameMap=m;
        pathfinder=p;
        spawn();
        astarPathFind=new AStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph());
        thetaPathFind=new ThetaStarShortestPath<>(gameMap.getGraph());
        AStarpath=astarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        ThetaStarpath=thetaPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        //dijkstraPathFind = new DijkstraShortestPath<>(gameMap.getGraph(),
        //        y*GameMap.WIDTH+x, gameMap.getPlayerNode());
        //System.out.println(ptfnd.getPath().getEdgeList().toString());
        //thetaPathFind=new ThetaStarShortestPath<>(m);
        //path=thetaPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        Tile[][] map = gameMap.getData();
        //path= dijkstraPathFind.getPath();
        if(AStarpath==null) return;
        for(Integer i : Graphs.getPathVertexList(AStarpath))
            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isAstarPath=true;
        for(Integer i : Graphs.getPathVertexList(ThetaStarpath))
            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isThetaPath=true;

        System.out.println("A*: "+AStarpath.toString());
        System.out.println("Theta*: "+ThetaStarpath.toString());
    }

    public void setPath(Pathfinder p){

        //astarPathFind=new AStarShortestPath<>(gameMap.getGraph());
        //path=astarPathFind.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        //System.out.println(path.toString());
        //path=new DijkstraShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph(),
        //        y*GameMap.WIDTH+x, gameMap.getPlayerNode()).getPath();
        astarPathFind=new AStarShortestPath<>(gameMap.getGraph());
        //path=astarPathFind.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        thetaPathFind=new ThetaStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph());
        pathfinder = p;
        AStarpath=astarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        ThetaStarpath=thetaPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        Tile[][] map = gameMap.getData();

        if(AStarpath==null) return;
        for(Integer i : Graphs.getPathVertexList(AStarpath))
            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isAstarPath=true;
        for(Integer i : Graphs.getPathVertexList(ThetaStarpath))
            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isThetaPath=true;

        System.out.println("A*: "+AStarpath.toString());
        System.out.println("Theta*: "+ThetaStarpath.toString());
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }

    public String getImage(){
        return image;
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

    private class EuclideanDistance implements AStarAdmissibleHeuristic<Integer>{

        @Override
        public double getCostEstimate(Integer sourceVertex, Integer targetVertex) {
            int sourceX, sourceY, targetX, targetY;
            sourceX=sourceVertex%GameMap.WIDTH;
            sourceY=sourceVertex/GameMap.WIDTH;
            targetX=targetVertex%GameMap.WIDTH;
            targetY=targetVertex/GameMap.WIDTH;
            return Math.sqrt(Math.pow(sourceX - targetX,2)+Math.pow(sourceY - targetY,2));
        }
    }

    private class ManhattanDistance implements AStarAdmissibleHeuristic<Integer> {

        @Override
        public double getCostEstimate(Integer sourceVertex, Integer targetVertex) {
            int sourceX, sourceY, targetX, targetY;
            sourceX=sourceVertex%GameMap.WIDTH;
            sourceY=sourceVertex/GameMap.WIDTH;
            targetX=targetVertex%GameMap.WIDTH;
            targetY=targetVertex/GameMap.WIDTH;
            //System.out.println("Source: [" +sourceX+","+sourceY+"] Target: ["+
            //        targetX+","+targetY+"] EstimatedCost: "+Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY));
            return Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY);
        }
    }
}
