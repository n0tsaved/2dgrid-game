package tilemap;

import java.awt.*;

/**
 */
public class TileMap implements MatrixGraph {

    private Tile[][] data;

    public TileMap() {
        data=new Tile[GameMap.WIDTH][GameMap.HEIGHT];
        for (int i = 0; i < GameMap.WIDTH; i++)
            for (int j = 0; j < GameMap.HEIGHT; j++)
                data[i][j] = new Tile(true);
    }

    @Override
    public void addEdge(int i, int j) {
        data[i][j].isBlocked = false;
    }

    @Override
    public void removeEdge(int i, int j) {
        data[i][j].isBlocked = true;
    }

    @Override
    public boolean hasEdge(int i, int j) {
        return !data[i][j].isBlocked;
    }

    public Tile[][] getData() {
        return data;
    }

    public void paint(Graphics2D g) {
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                data[x][y].paint(g, x, y);
            }
        }
    }

    public void setTraversable(TileMapElement t) {
        if(t.getClass()!=Room.class) return;
        Room r= (Room) t;
        for (int i = r.x; i < r.x + r.width; i++)
            for (int j = r.y; j < r.y + r.height; j++) {
                data[i][j].isBlocked = false;
            }
    }

    public void setNotTraversable(TileMapElement t) {
        if(t.getClass()!=Obstacle.class) return;
        Obstacle obst=(Obstacle) t;
        for(int i=obst.x;i<obst.x+obst.width;i++)
            for(int j=obst.y;j<obst.y+obst.height;j++)
                data[i][j].isBlocked=true;
    }
}