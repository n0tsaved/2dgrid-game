package tilemap;

import com.badlogic.gdx.math.Vector2;
import tilemap.jgrapht.GraphPath;
import tilemap.jgrapht.Graphs;
import tilemap.jgrapht.alg.interfaces.Pathfinder;
import tilemap.jgrapht.alg.util.Trailmax;
import tilemap.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

/**
 * Created by notsaved on 5/16/17.
 */
public class FleeBehaviour extends AgentBehaviour {

    private Entity myEntity;
    private Entity entityToFleeFrom;
    private List<Integer> pathToFollow;
    private Vector2 position;
    private Vector2 nextPosition;
    private boolean caught = false;
    private boolean thinking = false;
    private Pathfinder<Integer, DefaultWeightedEdge> pathfinder;

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

    public Vector2 getNextPosition() {
        if (pathToFollow == null || pathToFollow.isEmpty()) return nextPosition;
        pathToFollow.remove(0);
        return !pathToFollow.isEmpty() ? new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH) : position;
    }

    @Override
    public void doBehaviour(float delta) {
        if(thinking || caught) return;
        if(entityToFleeFrom.getNode() == myEntity.getNode()) caught=true;
        if(pathToFollow !=null && nextPosition!= null && pathToFollow.size()>2) {
            Vector2 step = nextPosition.cpy().sub(position).nor().scl((myEntity.SPEED-0.1f) * delta);
            position = new Vector2(myEntity.getX() + step.x, myEntity.getY() + step.y);
            myEntity.setPosition(myEntity.getX() + step.x, myEntity.getY() + step.y);
            if (nextPosition.cpy().sub(myEntity.getX(), myEntity.getY()).len() < myEntity.COLLISION_RADIUS) {
                nextPosition = getNextPosition();
            }
        } else {
            new Thread(new thinkEvasionPath()).start();
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
    }

    private class thinkEvasionPath implements Runnable {


        @Override
        public void run() {
            Integer agentNode = entityToFleeFrom.getNode();
            Integer targetNode = myEntity.getNode();
            thinking = true;
            GraphPath<Integer, DefaultWeightedEdge> path = pathfinder.getShortestPath(new Integer(agentNode), new Integer(targetNode), null);
            if(path!=null) {
                pathToFollow = Graphs.getPathVertexList(path);
                nextPosition = new Vector2(pathToFollow.get(0) % GameMap.WIDTH, pathToFollow.get(0) / GameMap.WIDTH);
            }
            thinking = false;
        }
    }
}
