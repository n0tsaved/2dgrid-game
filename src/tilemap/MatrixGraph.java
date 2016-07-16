package tilemap;

import java.awt.*;

/**
 */
public interface MatrixGraph {
    void addEdge(int i, int j);
    void removeEdge(int i, int j);
    boolean hasEdge(int i, int j);


}
