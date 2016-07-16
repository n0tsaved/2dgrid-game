package tilemap;

/**
 */
public class Obstacle implements TileMapElement{
    public int x,y,width,height;

    public Obstacle(int x,int y, int width, int height){
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
    }

    public boolean intersect(Object other){
        if(other.getClass()!=Obstacle.class)
            return false;
        Obstacle o= (Obstacle) other;
        return !(this.x+ this.width < (o.x) || (o.x + o.width) < this.x ||
                this.y+this.height < (o.y) || (o.y + o.height) < this.y);
    }

    public void setNotTraversable(Tile[][] map){
        //System.out.println(x+","+width);
        for(int i=x; i<x+width;i++)
            for(int j=y;j<y+height;j++ ) {
                map[i][j]=new Tile(true);
                //System.out.println("("+i+","+j+") Blocked = "+map[i][j].isBlocked);
            }
    }
}
