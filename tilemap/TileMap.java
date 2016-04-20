package tilemap;

import java.awt.*;

/**
 */
public class TileMap implements MatrixGraph {

    private byte[][] grid;

    public TileMap(int n) {
        grid = new byte[n][n];
        for (int i = 0; i < GameMap.WIDTH; i++)
            for (int j = 0; j < GameMap.HEIGHT; j++)
                grid[i][j] = 0;
    }


    public void addEdge(int i, int j) {
        grid[i][j] = 1;
    }


    public void removeEdge(int i, int j) {
        grid[i][j] = 0;
    }


    public boolean hasEdge(int i, int j) {
        return grid[i][j] == 1;
    }
}