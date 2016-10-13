package tilemap;

import java.awt.*;


public class Tile {
    public static final int TILE_SIZE = 5;
    public boolean isBlocked;
    public boolean isStart;
    public boolean isThetaPath;
    public boolean isAstarPath;

    public Tile(boolean blocked){
        isBlocked=blocked;
    }
    public void paint(Graphics g, int x, int y){
        if(this.isBlocked) {
            g.setColor(Color.darkGray);
        }
        else if(isStart) g.setColor(Color.GRAY.brighter());
        else if(isAstarPath) g.setColor(Color.RED.brighter());
        else if(isThetaPath) g.setColor(Color.BLUE.brighter());
        else g.setColor(Color.GRAY);
        g.fillRect(x*TILE_SIZE+5,y*TILE_SIZE-5,TILE_SIZE,TILE_SIZE);

        g.setColor(g.getColor().darker());
        g.drawRect(x*TILE_SIZE+5,y*TILE_SIZE-5,TILE_SIZE,TILE_SIZE);
    }
}
