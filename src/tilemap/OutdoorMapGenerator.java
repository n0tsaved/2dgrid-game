package tilemap;

import java.util.Random;

/**
 */
public class OutdoorMapGenerator extends MapGenerator {
    private static final int OBSTCL_MAX_SIZE= 20;
    private static final int OBSTCL_MIN_SIZE=10;
    private static final int MAX_OBSTCL=50;
    private static final int COVERAGE_PERCENT = 30;
    private static final int MIN_AREA_COVERAGE = ((GameMap.WIDTH*GameMap.HEIGHT)*COVERAGE_PERCENT)/100;
    @Override
    public void generate(GameMap map) {
        setAllClear(map.getData());
        placeObstacles(map);
    }

    private void placeObstacles(GameMap map) {
        Random r=new Random();
        int area=0;
        //for(int i=0;i<MAX_OBSTCL;i++)
        while(area<MIN_AREA_COVERAGE){
            int w = r.nextInt((OBSTCL_MAX_SIZE - OBSTCL_MIN_SIZE) + 1) + OBSTCL_MIN_SIZE;
            int h = r.nextInt((OBSTCL_MAX_SIZE - OBSTCL_MIN_SIZE) + 1) + OBSTCL_MIN_SIZE;
            int x = r.nextInt(GameMap.WIDTH - w - 1) + 1;
            int y = r.nextInt(GameMap.HEIGHT - h - 1) + 1;
            Obstacle o = new Obstacle(x, y, w, h);
            if(map.addObstcl(o))
                area+=o.getArea();
        }
    }

    public void setAllClear(Tile[][] data) {
        for(int i=0;i<GameMap.WIDTH;i++)
            for(int j=0; j<GameMap.HEIGHT;j++)
                data[i][j]=new Tile(false);
    }
}
