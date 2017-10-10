package tilemap;



import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.*;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.DefaultWeightedEdge;
import java.awt.*;
import java.util.*;
import java.util.List;


public class Entity {
    /**
     * The x position of this entity in terms of grid cells
     */
    private float x;
    /**
     * The y position of this entity in terms of grid cells
     */
    private float y;
    /**
     * The image to draw for this entity
     */
    private String image;
    /**
     * The gameMap which this entity is wandering around
     */
    private GameMap gameMap;
    private static Random r = new Random(GameMap.WIDTH * GameMap.HEIGHT);
    private AStarShortestPath<Integer, DefaultWeightedEdge> astarPathFind;
    private DijkstraShortestPath<Integer, DefaultWeightedEdge> dijkstraPathFind;
    private ThetaStarShortestPath<Integer, DefaultWeightedEdge> thetaPathFind;
    private LazyMovingTargetAdaptiveAStarShortestPath<Integer, DefaultWeightedEdge> paastarPathFind;
    private GraphPath<Integer, DefaultWeightedEdge> AStarpath = null;
    private List<Integer> ThetaStarpath = null;
    private List<Integer> pathToFollow = null;
    private boolean nextNodeflag = false;
    private Pathfinder pathfinder;
    private AgentBehaviour behaviour;
    private boolean useTheta = false;
    private boolean verbose = false;
    public static final float COLLISION_RADIUS = 0.3f;
    public final float SPEED = 0.2f;
    private boolean thinking = false;


    public Entity(String s, GameMap m, Pathfinder p) {
        image = s;
        gameMap = m;
        pathfinder = p;
        spawn();

    }

    public int getNode() {
        return ((int) y) * GameMap.WIDTH + ((int) x);
    }


    /*public Vector2 getPosition() {
        return position;
    }*/

    public void setPath(Pathfinder p) {

        //astarPathFind=new AStarShortestPath<>(gameMap.getGraph());
        //path=astarPathFind.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        //System.out.println(path.toString());
        //path=new DijkstraShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph(),
        //        y*GameMap.WIDTH+x, gameMap.getPlayerNode()).getPath();
        //astarPathFind=new AStarShortestPath<>(gameMap.getGraph());
        //path=astarPathFind.getShortestPath(y*GameMap.WIDTH+x, gameMap.getPlayerNode(), new ManhattanDistance());
        //thetaPathFind=new ThetaStarShortestPath<Integer, DefaultWeightedEdge>(gameMap.getGraph());
        pathfinder = p;
        pathToFollow = null;
        //paastarPathFind=new LazyMovingTargetAdaptiveAStarShortestPath<>(gameMap.getGraph());
        //AStarpath=paastarPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        //ThetaStarpath=thetaPathFind.getShortestPath(new Integer(y*GameMap.WIDTH+x), new Integer(gameMap.getPlayerNode()), new ManhattanDistance());
        AStarpath = pathfinder.getShortestPath(getNode(), new Integer(gameMap.getPlayerNode()), new OctileDistance());
        if (AStarpath == null) return;
        pathToFollow = Graphs.getPathVertexList(AStarpath);
        //pathToFollow.remove(0);

        //nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
        colorPath(pathToFollow);

        report();
        nextNodeflag = true;
    }

    private void uncolorPath(List<Integer> pathToFollow) {
        Tile[][] map = gameMap.getData();
        if (AStarpath == null) return;
        for (Integer i : pathToFollow)
            map[i % gameMap.WIDTH][i / gameMap.WIDTH].isAstarPath = false;
        //for(Integer i : Graphs.getPathVertexList(ThetaStarpath))
        //  map[i%gameMap.WIDTH][i/gameMap.WIDTH].isThetaPath=false;
    }

    private void colorPath(List<Integer> pathToFollow) {
        Tile[][] map = gameMap.getData();
        if (pathToFollow == null) return;
        if (pathfinder.getClass() == BidirectionalAStarShortestPath.class)
            for (Integer i : pathToFollow)
                if (!map[i % gameMap.WIDTH][i / gameMap.WIDTH].isFrontier)
                    map[i % gameMap.WIDTH][i / gameMap.WIDTH].isAstarPath = true;
        else
            for (Integer j : pathToFollow)
                map[j % gameMap.WIDTH][j / gameMap.WIDTH].isAstarPath = true;

        //if (ThetaStarpath != null)
        //  for (Integer t : Graphs.getPathVertexList(ThetaStarpath))
        //    map[t % gameMap.WIDTH][t / gameMap.WIDTH].isThetaPath = true;
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getImage() {
        return image;
    }

    public void spawn() {
        int node = r.nextInt();
        while (gameMap.blocked(node % gameMap.WIDTH, node / gameMap.WIDTH))
            node = r.nextInt();
        this.x = node % gameMap.WIDTH;
        this.y = node / gameMap.WIDTH;
        //position = new Vector2(this.x, this.y);
        gameMap.getData()[(int) x][(int) y].isStart = true;
    }

    public void setGameMap(GameMap m) {
        gameMap = m;
    }

    public void paint(Graphics2D g) {
        // work out the screen position of the entity based on the
        // x/y position and the size that tiles are being rendered at. So
        // if we're at 1.5,1.5 and the tile size is 10 we'd render on screen
        // at 15,15.
        int xp = (int) (Tile.TILE_SIZE * x + Tile.TILE_SIZE/2);
        int yp = (int) (Tile.TILE_SIZE * y + Tile.TILE_SIZE/2);
        g.setColor(Color.BLUE);

        // rotate the sprite based on the current angle and then
        // draw it
        //g.rotate(ang, xp, yp);
        g.drawString(image, xp, yp);

        //g.drawImage(image, (int) (xp - 16), (int) (yp - 16), null);
        //g.rotate(-ang, xp, yp);
        if (ThetaStarpath != null && !ThetaStarpath.isEmpty()) {
            g.setColor(Color.BLUE);
            Iterator<Integer> i = ThetaStarpath.iterator();
            int current_x = ThetaStarpath.get(0) % GameMap.WIDTH;
            int current_y = ThetaStarpath.get(0) / GameMap.WIDTH;
            while (i.hasNext()) {
                Integer next = i.next();
                int next_x = next % GameMap.WIDTH;
                int next_y = next / GameMap.WIDTH;
                g.drawLine(current_x * Tile.TILE_SIZE + Tile.TILE_SIZE+Tile.TILE_SIZE/2, current_y * Tile.TILE_SIZE-Tile.TILE_SIZE/2, next_x * Tile.TILE_SIZE + Tile.TILE_SIZE + Tile.TILE_SIZE/2, next_y * Tile.TILE_SIZE - Tile.TILE_SIZE/2);
                current_x = next_x;
                current_y = next_y;
            }
        }
        if(AStarpath != null){
            g.setColor(Color.RED);
            Iterator<Integer> i = pathToFollow.iterator();
            int current_x = pathToFollow.get(0) % GameMap.WIDTH;
            int current_y = pathToFollow.get(0) / GameMap.WIDTH;
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

    private Array<Vector2> getWaypoints(List<Integer> path) {
        Array<Vector2> newPath = new Array<>();
        for (Integer i : path)
            newPath.add(new Vector2(i % GameMap.WIDTH, i / GameMap.WIDTH));
        return newPath;
    }

    public void update(float delta) {
       /* if (steeringBehavior != null) {
            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);

			/*
			 * Here you might want to add a motor control layer filtering steering accelerations.
			 *
			 * For instance, a car in a driving game has physical constraints on its movement:
			 * - it cannot turn while stationary
			 * - the faster it moves, the slower it can turn (without going into a skid)
			 * - it can brake much more quickly than it can accelerate
			 * - it only moves in the direction it is facing (ignoring power slides)
			 */

        // Apply steering acceleration to move this agent
          /*  applySteering(steeringOutput, delta);
        }*/
         /* if(pathToFollow !=null && nextPosition!= null) {
              if (pathToFollow.contains(gameMap.getPlayerNode())) {
                  Vector2 step = nextPosition.cpy().sub(position).nor().scl(SPEED*delta);
                  setPosition(getX() + step.x, getY() + step.y);
                  if (nextPosition.cpy().sub(getX(), getY()).len() < COLLISION_RADIUS) {
                      nextPosition = getNextPosition();
                  }
              } else if (!pathToFollow.contains(gameMap.getPlayerNode())) {
                  pathToFollow = Graphs.getPathVertexList(pathfinder.getShortestPath(((int)nextPosition.y)*GameMap.WIDTH + ((int)nextPosition.x), gameMap.getPlayerNode(), new OctileDistance()));
                  //nextPosition = new Vector2(pathToFollow.get(0)%GameMap.WIDTH, pathToFollow.get(0)/GameMap.WIDTH);
                  Vector2 step = nextPosition.cpy().sub(position).nor().scl(SPEED*(delta));
                  setPosition(this.x + step.x, this.y + step.y);
                  if (nextPosition.cpy().sub(getX(), getY()).len() < COLLISION_RADIUS) {
                      nextPosition = getNextPosition();
                  }

              }
          }*/
         FollowBehaviour b = null;
        if (behaviour != null) {
            behaviour.doBehaviour(delta);
        }else setPath(gameMap.getPlayerNode());

    }

    public void setBehaviour(AgentBehaviour b) {
        this.behaviour = b;
    }

    public void setPosition(float v, float v1) {
        this.x = v;
        this.y = v1;
        //this.position = new Vector2(v, v1);
        //System.out.println(image+" - "+position);
    }

    /*public void move(double delta){
        if(image == "#") pathToFollow = ThetaStarpath;
        if(nextPos != null){
            Vector2 start = new Vector2(x, y);
            double distance = nextPos.dst(start);

            if(gotNext) {
                //if(endNode.equals(getNode())) endNode = pathToFollow.remove(0);
                direction = (nextPos.sub(start)).nor();
                gotNext = !gotNext;
            }
            Vector2 vec = direction.mul(speed * (float) delta);
            x += vec.x;
            y += vec.y;

            if(start.dst2(new Vector2(x, y))>= distance){
                y = nextPos.y;
                x = nextPos.x;
                pathToFollow.remove(0);
                Integer next = pathToFollow.get(0);
                nextPos = new Vector2(next%GameMap.WIDTH, next/GameMap.WIDTH);
                gotNext = !gotNext;
            }
        }

    }*/

    private void report() {
        if (AStarpath == null) return;
        System.out.println("[AGENT]: " + image + " (" + x + "," + y + ")");
        System.out.println("A*: " + AStarpath.toString() + "\nExpanded nodes: " + pathfinder.getNumberOfExpandedNodes() + "\nWeight: " + AStarpath.getWeight());
        //System.out.println("Theta*: "+ThetaStarpath.toString()+"\nExpanded nodes: "+thetaPathFind.getNumberOfExpandedNodes()+"\nWeight: "+ThetaStarpath.getWeight());
    }

    public void setThetaPath() {
        useTheta = true;
        thetaPathFind = new ThetaStarShortestPath<>(gameMap.getGraph());
        ThetaStarpath = thetaPathFind.getShortestPath(new Integer((int) (y * GameMap.WIDTH + x)), new Integer(gameMap.getPlayerNode()), new OctileDistance());
    }


    /*public Vector2 getNextPosition() {
        if (pathToFollow == null || pathToFollow.isEmpty()) return nextPosition;
        pathToFollow.remove(0);
        return !pathToFollow.isEmpty() ? new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH) : nextPosition;
    }*/

    public List getFollowPath() {
        return pathToFollow;
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    /*public void setNextPosition(Vector2 nextPosition) {
        this.nextPosition = nextPosition;
    }*/

    public void setPathfinder(Pathfinder<Integer, DefaultWeightedEdge> pathfinder) {
        this.pathfinder = pathfinder;
    }

    public void setPath(int node) {

        if(AStarpath != null)
            if(Graphs.getPathVertexList(AStarpath).contains(node))
                return;
            //else uncolorPath(pathToFollow);

        if(useTheta) {
            thetaPathFind = new ThetaStarShortestPath<>(gameMap.getGraph());
            ThetaStarpath = thetaPathFind.getShortestPath(new Integer((int) (y * GameMap.WIDTH + x)), new Integer(gameMap.getPlayerNode()), new OctileDistance());
        }
        else AStarpath = pathfinder.getShortestPath(getNode(), node, new OctileDistance());
        if (AStarpath == null) return;
        pathToFollow = Graphs.getPathVertexList(AStarpath);
        //pathToFollow.remove(0);
        //colorPath(pathToFollow);
        if(verbose) report();
        //nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
    }


    public void setVerbose(){
        this.verbose = true;
        if(useTheta)
            thetaPathFind.setVerbose();
        else pathfinder.setVerbose();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
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
