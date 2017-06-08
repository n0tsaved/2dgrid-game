package tilemap.jgrapht.alg.interfaces;

import tilemap.jgrapht.GraphPath;

/**
 * Created by notsaved on 9/21/16.
 */
public interface Pathfinder<V,E> {
    GraphPath<V,E> getShortestPath(V sourceVertex, V targetVertex, AStarAdmissibleHeuristic<V> admissibleHeuristic);

    int getNumberOfExpandedNodes();
    long getElapsedTime();
}
