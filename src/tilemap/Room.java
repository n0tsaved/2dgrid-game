package tilemap;

public class Room implements TileMapElement{
    private int x,y,width,height, area;


    public Room(int x,int y, int width, int height){
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.area=width*height;
    }

    public int[] center(){
        int[] coords= new int[2];
        coords[0]=((width)/2) +x;
        coords[1]=((height)/2) +y;
        return coords;
    }

    public boolean intersect(Object other){
        if(other.getClass()!=Room.class)
            return false;
        Room r= (Room) other;
        return !(this.x+ this.width < (r.x) || (r.x + r.width) < this.x ||
                this.y+this.height < (r.y) || (r.y + r.height) < this.y);
    }

    public void setTraversable(Tile[][] map){
        //System.out.println(x+","+width);
        for(int i=x; i<x+width;i++)
            for(int j=y;j<y+height;j++ ) {
                map[i][j]=new Tile(false);
                //System.out.println("("+i+","+j+") Blocked = "+map[i][j].isBlocked);
            }
    }

    public int getArea() {
        return area;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}
