package tilemap;

import java.awt.*;


public class Tile {
    public static final int TILE_SIZE = 10;
    public boolean isBlocked;
    public boolean isStart;
    public boolean isPath;
    public Tile(boolean blocked){
        isBlocked=blocked;
    }
    public void paint(Graphics g, int x, int y){
        if(this.isBlocked) {
            g.setColor(Color.darkGray);
        }
        else if(isStart) g.setColor(Color.GRAY.brighter());
        else if(isPath) g.setColor(Color.GRAY.brighter());
        else g.setColor(Color.GRAY);
        g.fillRect(x*TILE_SIZE,y*TILE_SIZE-10,TILE_SIZE,TILE_SIZE);

        g.setColor(g.getColor().darker());
        g.drawRect(x*TILE_SIZE,y*TILE_SIZE-10,TILE_SIZE,TILE_SIZE);
    }
}
