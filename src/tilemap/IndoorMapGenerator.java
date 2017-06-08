package tilemap;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 */
public class IndoorMapGenerator extends MapGenerator {
    private final int ROOM_MAX_SIZE = 15;
    private final int ROOM_MIN_SIZE = 15*15;
    private final int MAX_ROOMS = 15;
    private static Random r = new Random();

    @Override
    public void generate(GameMap map) {
        r.setSeed(System.currentTimeMillis());
        setAllClear(map.getData());
        //splitArea(map.getData(), GameMap.WIDTH, GameMap.HEIGHT, 0, 0, true);
        divide(map.getData(),0,0, GameMap.WIDTH, GameMap.HEIGHT, true);

    }

    public void splitArea(Tile[][] m, int width, int height, int x_offset, int y_offset, Boolean vert) {
        if (width * height < Math.pow(ROOM_MIN_SIZE, 2) || width < 12 || height < 10) return;
        if (vert) {
            // split vertically
            int new_width = r.nextInt(((width / 4) * 3) - ((width / 4) + 1)) + (width / 4);
            int x_start_wall = new_width + x_offset; //width/2 +x_offset;

            for (int j = y_offset; j < height && !m[x_start_wall][j].isBlocked; j++)
                m[x_start_wall][j].isBlocked = true;
            m[x_start_wall][r.nextInt(height / 2) + y_offset].isBlocked = false;

            splitArea(m, new_width, height, x_offset, y_offset, !vert); //split sx area
            splitArea(m, width - new_width, height, x_start_wall + 1, y_offset, !vert); //split dx area

        } else {
            //split horizontally
            int new_height = r.nextInt(((height / 4) * 3) - ((height / 4) + 1)) + (height / 4);
            int y_start_wall = new_height + y_offset;

            for (int i = x_offset; i < width && !m[i][y_start_wall].isBlocked; i++)
                m[i][y_start_wall].isBlocked = true;
            m[r.nextInt(width / 2) + x_offset][y_start_wall].isBlocked = false;

            splitArea(m, width, new_height, x_offset, y_offset, !vert); //split up area
            splitArea(m, width, height - new_height, x_offset, y_start_wall + 1, !vert); //split down area;

        }

    }

    public void divide(Tile[][] grid,int x, int y,int width, int height,boolean orientation){
        if (width*height<ROOM_MIN_SIZE) return;
        boolean horizontal =orientation ==true;
        //ThreadLocalRandom.current().nextInt(min, max + 1);
        int wx =x +(horizontal ? 0: r.nextInt(((width / 4) * 3) - ((width / 4))) + (width / 4)+1);
        int wy =y +(horizontal ? r.nextInt(((height / 4) * 3) - ((height / 4))) + (height / 4)+1:0);

        int px =wx +(horizontal ? r.nextInt(((width / 4) * 3) - (width / 4)) + (width / 4) :0);
        int py =wy +(horizontal ?0: r.nextInt(((height / 4) * 3) - (height / 4)) + (height / 4));

        int dx =horizontal ?1:0;
        int dy =horizontal ?0:1;

        int length =horizontal ? width :height;




        for (int i=0; i<length;i++){

                if ((wx != px || wy != py)) {
                    grid[wx][wy].isBlocked = true;
                }

            wx += dx;
            wy += dy;
        }

        int nx=x;
        int ny=y;
        int w= horizontal ? width : wx -x +1;
        int h= horizontal ? wy-y+1 : height;

        divide(grid, nx, ny, w, h, choose_orientation(w, h));

        nx = horizontal ? x: wx+1;
        ny = horizontal ? wy+1: y;
        w = horizontal ? width: x + width -wx -1;
        h = horizontal ? y+height-wy-1: height;
        
        divide(grid, nx, ny, w, h, choose_orientation(w, h));
        grid[px][py].isBlocked = false;
        if(!horizontal) {
            if (!GameMap.blocked(px, py) && GameMap.blocked(px + 1, py))
                grid[px + 1][py].isBlocked = false;
            if (!GameMap.blocked(px, py) && GameMap.blocked(px - 1, py))
                grid[px-1][py].isBlocked = false;
        }else{
            if (!GameMap.blocked(px, py) && GameMap.blocked(px, py+1))
                grid[px][py+1].isBlocked = false;
            if (!GameMap.blocked(px, py) && GameMap.blocked(px, py-1))
                grid[px][py-1].isBlocked = false;
            }
        //grid[px][py].isBlocked = false;
}

    private boolean choose_orientation(int w, int h) {
        if (w<h) return true;
        else return false;
    }

    public void setAllClear(Tile[][] data) {
        for(int i=0;i<GameMap.WIDTH;i++)
            for(int j=0; j<GameMap.HEIGHT;j++)
                data[i][j]=new Tile(false);
    }
}
