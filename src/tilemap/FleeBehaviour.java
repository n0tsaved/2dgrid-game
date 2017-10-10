package tilemap;

import com.badlogic.gdx.math.Vector2;
import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultWeightedEdge;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by notsaved on 5/16/17.
 */
public class FleeBehaviour extends AgentBehaviour {

    private Player player;
    private Entity myEntity;
    private Entity entityToFleeFrom;
    private List<Integer> pathToFollow;
    private Vector2 position;
    private Vector2 nextPosition;
    private boolean caught = false;
    private boolean thinking = false;
    private Pathfinder<Integer, DefaultWeightedEdge> pathfinder;
    private boolean evadeFromPlayer = false;
    private Integer oldPosition;

    FleeBehaviour(Entity e, Entity e1, Trailmax<Integer, DefaultWeightedEdge> t){
        behaviour="flee";
        this.myEntity = e;
        this.entityToFleeFrom = e1;
        this.position = new Vector2(myEntity.getX(), myEntity.getY());
        pathfinder = t;
        GraphPath path = pathfinder.getShortestPath(entityToFleeFrom.getNode(), myEntity.getNode() , null);
        if(path!=null) {
            pathToFollow = Graphs.getPathVertexList(path);
            nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
        }
    }

    FleeBehaviour(Entity e, Player p, Trailmax<Integer, DefaultWeightedEdge> t){
        behaviour="flee";
        this.myEntity = e;
        //this.entityToFleeFrom = e1;
        evadeFromPlayer = true;
        player = p;
        this.position = new Vector2(myEntity.getX(), myEntity.getY());
        pathfinder = t;
        GraphPath path = pathfinder.getShortestPath(player.getNode(), myEntity.getNode() , null);
        oldPosition =player.getNode();
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
        //if(thinking || caught) return;
        int enemyNode;
        if (evadeFromPlayer) enemyNode = player.getNode();
        else enemyNode = entityToFleeFrom.getNode();
        if(enemyNode == myEntity.getNode()) caught=true;

        if(pathToFollow !=null && nextPosition!= null) {
            Vector2 step = nextPosition.cpy().sub(position).nor().scl((myEntity.SPEED) * delta);
            position = new Vector2(myEntity.getX() + step.x, myEntity.getY() + step.y);
            myEntity.setPosition(myEntity.getX() + step.x, myEntity.getY() + step.y);
            if (nextPosition.cpy().sub(myEntity.getX(), myEntity.getY()).len() < myEntity.COLLISION_RADIUS) {
                nextPosition = getNextPosition();
            }
        }

        if(oldPosition != null && oldPosition != enemyNode && ! thinking) new Thread(new thinkEvasionPath()).start();
            /*int myNode = ((int)nextPosition.y)*GameMap.WIDTH + ((int)nextPosition.x);
            //int myNode = myEntity.getNode();
            int agentNode = entityToFleeFrom.getNode();
            GraphPath path=null;
            if(!GameMap.blocked(myNode % GameMap.WIDTH, myNode / GameMap.WIDTH) && !GameMap.blocked(agentNode%GameMap.WIDTH, agentNode / GameMap.WIDTH))
                path = pathfinder.getShortestPath(new Integer(agentNode), new Integer(myNode), null);
            //long timeElapsedforPathfinding = TimeUnit.NANOSECONDS.convert(pathfinder.getElapsedTime(), TimeUnit.MILLISECONDS);

            if(path!= null) pathToFollow = Graphs.getPathVertexList(path);
            //nextPosition = position;
            nextPosition = new Vector2(pathToFollow.get(0)%GameMap.WIDTH, pathToFollow.get(0)/GameMap.WIDTH);
            //Vector2 step = nextPosition.cpy().sub(position).nor();
            //position = new Vector2(myEntity.getX() + step.x, myEntity.getY() + step.y);
            //myEntity.setPosition(myEntity.getX() + step.x, myEntity.getY() + step.y);
            if (nextPosition.cpy().sub(myEntity.getX(), myEntity.getY()).len() < myEntity.COLLISION_RADIUS) {
                nextPosition = getNextPosition();
            }*/


    }

    private class thinkEvasionPath implements Runnable {


        @Override
        public void run() {
            thinking = true;
            Integer agentNode;
            Integer targetNode = myEntity.getNode();
            if(evadeFromPlayer) agentNode = player.getNode();
            else agentNode = entityToFleeFrom.getNode();
            if(!GameMap.blocked(targetNode%GameMap.WIDTH, targetNode / GameMap.WIDTH))
                if(!GameMap.blocked((int)myEntity.getX(),(int)myEntity.getY())) {
                    GraphPath<Integer, DefaultWeightedEdge> path = pathfinder.getShortestPath( agentNode, targetNode, null);
                    System.out.println("player node: "+agentNode+" entity node: "+ targetNode+"\npath: "+path);
                    oldPosition = agentNode;
                    synchronized (pathToFollow) {
                        if (path != null) {
                            pathToFollow = Graphs.getPathVertexList(path);
                            int index = pathToFollow.lastIndexOf(myEntity.getNode());
                            if (pathToFollow.contains(myEntity.getNode()))
                                pathToFollow.removeAll(pathToFollow.subList(0, index));
                            nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
                        }
                    }
                }
            thinking = false;
        }
    }
}
