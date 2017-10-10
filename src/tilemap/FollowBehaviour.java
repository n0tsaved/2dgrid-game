package tilemap;

import com.badlogic.gdx.math.Vector2;
import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

/**
 * Created by notsaved on 5/16/17.
 */
public class FollowBehaviour extends AgentBehaviour {

    private Entity myEntity;
    private Entity entityToFollow;
    private Player player;
    private List<Integer> pathToFollow;
    private Vector2 position;
    private Vector2 nextPosition;
    private boolean caught = false;
    private boolean dontmove = false;
    private Pathfinder<Integer, DefaultWeightedEdge> pathfinder;
    private boolean followPlayer = false;
    private boolean thinking = false;

    FollowBehaviour(Entity e, Entity e1, Pathfinder<Integer, DefaultWeightedEdge> p){
       behaviour="follow";
       this.myEntity = e;
       this.entityToFollow = e1;
       this.position = new Vector2(myEntity.getX(), myEntity.getY());
       pathfinder = p;
       GraphPath path = pathfinder.getShortestPath(myEntity.getNode(), entityToFollow.getNode(), new OctileDistance());
       if(path!=null) {
           pathToFollow = Graphs.getPathVertexList(path);
           nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
       }
    }

    FollowBehaviour(Entity e, Player p, Pathfinder<Integer, DefaultWeightedEdge> pathfinder){
        behaviour="follow";
        this.myEntity = e;
        //this.entityToFollow = e1;
        this.player = p;
        this.followPlayer = true;
        this.position = new Vector2(myEntity.getX(), myEntity.getY());
        this.pathfinder = pathfinder;
        GraphPath path = pathfinder.getShortestPath(myEntity.getNode(), player.getNode(), new OctileDistance());
        if(path!=null) {
            pathToFollow = Graphs.getPathVertexList(path);
            nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
        }
    }

    public Vector2 getNextPosition() {
        if (pathToFollow == null || pathToFollow.isEmpty()) return nextPosition;
        pathToFollow.remove(0);
        return !pathToFollow.isEmpty() ? new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH) : position;
    }

    @Override
    public void doBehaviour(float delta) {
        //if (caught) return;

        int targetNode;
        if (followPlayer) targetNode = player.getNode();
        else targetNode = entityToFollow.getNode();
        if( caught) return;
        if(pathToFollow !=null && nextPosition!= null && pathToFollow.size()>2) {
            Vector2 step = nextPosition.cpy().sub(position).nor().scl((myEntity.SPEED) * delta);
            position = new Vector2(myEntity.getX() + step.x, myEntity.getY() + step.y);
            myEntity.setPosition(myEntity.getX() + step.x, myEntity.getY() + step.y);
            if (nextPosition.cpy().sub(myEntity.getX(), myEntity.getY()).len() < myEntity.COLLISION_RADIUS) {
                nextPosition = getNextPosition();
            }
        }

        if(!pathToFollow.contains(targetNode) && !thinking) new Thread(new thinkPath()).start();
        if (new Integer(myEntity.getNode()).equals(targetNode))
            caught = true;

    }


    public boolean isCaught(){
        return caught;
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

    private class thinkPath implements Runnable{


        @Override
        public void run() {
            thinking = true;
            GraphPath p = null;
            int targetNode;
            if(followPlayer) targetNode = player.getNode();
            else targetNode = entityToFollow.getNode();
            if(!GameMap.blocked(targetNode%GameMap.WIDTH, targetNode / GameMap.WIDTH))
                if(!GameMap.blocked((int)myEntity.getX(),(int)myEntity.getY())) {
                    p = pathfinder.getShortestPath(myEntity.getNode(), targetNode, new OctileDistance());
                    synchronized (pathToFollow) {
                        pathToFollow = Graphs.getPathVertexList(p);
                        int index = pathToFollow.lastIndexOf(myEntity.getNode());
                        dontmove = true;
                        if (pathToFollow.contains(myEntity.getNode()))
                            pathToFollow.removeAll(pathToFollow.subList(0, index));
                        //nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
                        dontmove = false;
                    }
                }
            thinking = false;
        }
    }
}
