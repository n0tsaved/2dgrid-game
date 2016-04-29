package tilemap;

import java.util.Random;

/**
 * Created by merda on 29/04/16.
 */
public class IndoorMapGenerator extends MapGenerator {
    private final int ROOM_MAX_SIZE=15;
    private final int ROOM_MIN_SIZE=12;
    private final int MAX_ROOMS=15;
    private static Random r= new Random();
    @Override
    public void generate(GameMap map) {
        r.setSeed(System.currentTimeMillis());
        setAllClear(map.getData());
        splitArea(map.getData(), GameMap.WIDTH, GameMap.HEIGHT, 0,0, new Random().nextBoolean());

    }

    public void splitArea(Tile[][] m, int width, int height, int x_offset, int y_offset, Boolean vert){
        if(width*height<Math.pow(ROOM_MIN_SIZE,2 ) || width<12 || height < 10) return;
        if(vert){
            // split vertically
            int new_width=r.nextInt(((width/4)*3)-((width/4)+1))+(width/4);
            int x_start_wall=new_width+x_offset; //width/2 +x_offset;

            splitArea(m,new_width, height,x_offset, y_offset, !vert); //split sx area
            splitArea(m,width-new_width, height,x_start_wall+1, y_offset, !vert); //split dx area
            for(int j=y_offset; j<GameMap.HEIGHT && !m[x_start_wall][j].isBlocked; j++)
                m[x_start_wall][j].isBlocked=true;
            m[x_start_wall][r.nextInt(height/2)+y_offset].isBlocked=false;
        }else{
            //split horizontally
            int new_height=r.nextInt(((3/4)*height)-((1/4)*height)+1)+(height/4);
            int y_start_wall=new_height + y_offset;

            splitArea(m, width,new_height, x_offset, y_offset, !vert); //split up area
            splitArea(m, width, height-new_height, x_offset, y_start_wall+1, !vert); //split down area;
            for(int i=x_offset; i<GameMap.WIDTH && !m[i][y_start_wall].isBlocked; i++)
                m[i][y_start_wall].isBlocked=true;
            m[r.nextInt(width/2)+x_offset][y_start_wall].isBlocked=false;
        }

    }

    public void setAllClear(Tile[][] data) {
        for(int i=0;i<GameMap.WIDTH;i++)
            for(int j=0; j<GameMap.HEIGHT;j++)
                data[i][j]=new Tile(false);
    }
}
