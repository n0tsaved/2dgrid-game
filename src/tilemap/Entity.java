package tilemap;



import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.DefaultEdge;
import tilemap.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.util.*;
import java.util.List;


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
    private MovingTargetAdaptiveAStarShortestPath<Integer, DefaultWeightedEdge> paastarPathFind;
    private GraphPath<Integer, DefaultWeightedEdge> AStarpath=null;
    private GraphPath<Integer, DefaultWeightedEdge> ThetaStarpath=null;
    private List<Integer> pathToFollow = null;

    private Pathfinder pathfinder;

    public Entity(String s, GameMap m, Pathfinder p){
        image=s;
        gameMap=m;
        pathfinder=p;
        spawn();
        //astarPathFind=new AStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph());
        //thetaPathFind=new ThetaStarShortestPath<>(gameMap.getGraph());
        //AStarpath=astarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        //ThetaStarpath=thetaPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        paastarPathFind=new MovingTargetAdaptiveAStarShortestPath<>(gameMap.getGraph());
        AStarpath=paastarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        //dijkstraPathFind = new DijkstraShortestPath<>(gameMap.getGraph(),
        //        y*GameMap.WIDTH+x, gameMap.getPlayerNode());
        //System.out.println(ptfnd.getPath().getEdgeList().toString());
        //thetaPathFind=new ThetaStarShortestPath<>(m);
        //path=thetaPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        Tile[][] map = gameMap.getData();
        //path= dijkstraPathFind.getPath();

        if(AStarpath==null) return;
        pathToFollow=Graphs.getPathVertexList(AStarpath);

        colorPath();
        report();
    }

    public void setPath(Pathfinder p){

        //astarPathFind=new AStarShortestPath<>(gameMap.getGraph());
        //path=astarPathFind.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        //System.out.println(path.toString());
        //path=new DijkstraShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph(),
        //        y*GameMap.WIDTH+x, gameMap.getPlayerNode()).getPath();
        //astarPathFind=new AStarShortestPath<>(gameMap.getGraph());
        //path=astarPathFind.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        //thetaPathFind=new ThetaStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph());
        pathfinder = p;
        //paastarPathFind=new MovingTargetAdaptiveAStarShortestPath<>(gameMap.getGraph());
        //AStarpath=paastarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        //ThetaStarpath=thetaPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        AStarpath = pathfinder.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        if(AStarpath==null) return;
        pathToFollow=Graphs.getPathVertexList(AStarpath);

        colorPath();

        report();
    }

    private void uncolorPath() {
        Tile[][] map = gameMap.getData();
        if(AStarpath == null) return;
        for(Integer i : Graphs.getPathVertexList(AStarpath))
            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isAstarPath=false;
//        for(Integer i : Graphs.getPathVertexList(ThetaStarpath))
//            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isThetaPath=true;
    }

    private void colorPath() {
        Tile[][] map = gameMap.getData();
        if(AStarpath == null) return;
        for(Integer i : Graphs.getPathVertexList(AStarpath))
            if(!map[i%gameMap.WIDTH][i/gameMap.WIDTH].isFrontier)
                map[i%gameMap.WIDTH][i/gameMap.WIDTH].isAstarPath=true;
//        for(Integer i : Graphs.getPathVertexList(ThetaStarpath))
//            map[i%gameMap.WIDTH][i/gameMap.WIDTH].isThetaPath=true;
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
        int xp = (int) (Tile.TILE_SIZE * x +Tile.TILE_SIZE);
        int yp = (int) (Tile.TILE_SIZE * y);
        g.setColor(Color.BLUE);

        // rotate the sprite based on the current angle and then
        // draw it
        //g.rotate(ang, xp, yp);
        g.drawString(image, xp, yp);

        //g.drawImage(image, (int) (xp - 16), (int) (yp - 16), null);
        //g.rotate(-ang, xp, yp);
    }

    public void move(){
        //AStarpath=paastarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        Integer next = null;
        if(AStarpath == null || AStarpath.getEdgeList().isEmpty()){ System.out.println("path null"); return; }
        uncolorPath();
        //paastarPathFind.restoreCoherence(gameMap.getPlayerNode());
        pathToFollow=Graphs.getPathVertexList(AStarpath);
        if(!pathToFollow.isEmpty()) next=pathToFollow.remove(0);
        //Integer next=Graphs.getOppositeVertex(gameMap.getGraph(), AStarpath.getEdgeList().get(0), y*GameMap.WIDTH+x);
        //Integer next = (Integer) gameMap.getGraph().getEdgeTarget(AStarpath.getEdgeList().get(0));
        if(next.equals(new Integer(y*gameMap.WIDTH+x)) && !pathToFollow.isEmpty() ) next = pathToFollow.remove(0);
        this.x = next % GameMap.WIDTH;
        this.y = next / GameMap.WIDTH;
        report();
        System.out.println("Agent "+image+" moving to ("+x+","+y+") - ("+next+")");
        //if(!pathToFollow.contains(gameMap.getPlayerNode()))
        //if(!pathToFollow.get(pathToFollow.size()-1).equals(gameMap.getPlayerNode()))
        //AStarpath=paastarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        pathfinder = new BidirectionalAStarShortestPath<Integer,DefaultEdge>(gameMap.getGraph());
        AStarpath = pathfinder.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new OctileDistance());

        colorPath();
    }

    private void report(){
        if(AStarpath == null ) return;
        System.out.println("[AGENT]: "+image+" ("+x+","+y+")");
        System.out.println("A*: "+AStarpath.toString()+"\nExpanded nodes: "+pathfinder.getNumberOfExpandedNodes()+"\nWeight: "+AStarpath.getWeight());
        //System.out.println("Theta*: "+ThetaStarpath.toString()+"\nExpanded nodes: "+thetaPathFind.getNumberOfExpandedNodes()+"\nWeight: "+ThetaStarpath.getWeight());
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

    private class OctileDistance implements AStarAdmissibleHeuristic<Integer> {

        @Override
        public double getCostEstimate(Integer sourceVertex, Integer targetVertex) {
            int sourceX, sourceY, targetX, targetY;
            sourceX=sourceVertex%GameMap.WIDTH;
            sourceY=sourceVertex/GameMap.WIDTH;
            targetX=targetVertex%GameMap.WIDTH;
            targetY=targetVertex/GameMap.WIDTH;
            //System.out.println("Source: [" +sourceX+","+sourceY+"] Target: ["+
            //        targetX+","+targetY+"] EstimatedCost: "+Math.abs(sourceX - targetX)+Math.abs(sourceY - targetY));
            return Math.abs(Math.abs(sourceX - targetX) - Math.abs(sourceY - targetY))+
                    Math.sqrt(2)*Math.min(Math.abs(sourceX - targetX), Math.abs(sourceY - targetY));
        }
    }
}
