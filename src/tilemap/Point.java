package tilemap;

import tilemap.GameMap;

/**
 * Created by notsaved on 1/23/17.
 */
public class Point{
    int x,y;

    public Point(int x, int y){
        this.x=x;
        this.y=y;
    }

    public int getX() {
        return x;
    }

    public int getY(){
        return y;
    }

    public Integer toNode(){

        return new Integer(GameMap.WIDTH*y+x);
    }
}
