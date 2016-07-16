package tilemap;

import java.util.List;
import java.util.Random;

/**
 * Created by Hp Dell i7 on 07/06/2016.
 */
public class DungeonMapGenerator extends MapGenerator {
    private final int ROOM_MAX_SIZE=15;
    private final int ROOM_MIN_SIZE=6;
    private final int MAX_ROOMS=15;
    public void generate(GameMap map) {
        setAllBlocked(map.getData());
        placeRooms(map);
    }

    private void placeRooms(GameMap map) {
        Random r=new Random();
        for(int i=0;i<MAX_ROOMS;i++) {
            int w = r.nextInt((ROOM_MAX_SIZE - ROOM_MIN_SIZE) + 1)+ROOM_MIN_SIZE;
            int h = r.nextInt((ROOM_MAX_SIZE - ROOM_MIN_SIZE) + 1) + ROOM_MIN_SIZE;
            int x = r.nextInt(GameMap.WIDTH -w -1)+1;
            int y = r.nextInt(GameMap.HEIGHT - h -1)+1;
            map.addRoom(new Room(x,y,w,h));
        }
        List<Room> rooms = map.getRooms();
        createTunnels(rooms, map.getData());
    }

    private void createTunnels(List<Room> rooms, Tile[][] map) {
        Room prev = null;
        for(Room r : rooms){
            if(rooms.indexOf(r)>0) {
                prev = rooms.get(rooms.indexOf(r) - 1);
                if (new Random().nextInt(101) > 50) {
                    create_h_tunnel(map, prev.center()[0], r.center()[0], prev.center()[1]);
                    create_v_tunnel(map, prev.center()[1], r.center()[1], r.center()[0]);
                } else {
                    create_v_tunnel(map, prev.center()[1], r.center()[1], prev.center()[0]);
                    create_h_tunnel(map, prev.center()[0], r.center()[0], r.center()[1]);

                }
            }
        }
    }

    private void create_h_tunnel(Tile[][] map, int x1, int x2, int y) {
        for(int i=Math.min(x1,x2); i<=Math.max(x1,x2);i++)
            map[i][y].isBlocked=false;
        /*for x in range(min(x1, x2), max(x1, x2) + 1):
        map[x][y].blocked = False
        map[x][y].block_sight = False*/
    }

    private void create_v_tunnel(Tile[][] map, int y1, int y2, int x) {
        for(int j=Math.min(y1,y2);j<Math.max(y1,y2);j++)
            map[x][j].isBlocked=false;
    }


    private void setAllBlocked(Tile[][] map) {
        for(int i=0; i<GameMap.WIDTH;i++)
            for(int j=0; j<GameMap.HEIGHT; j++)
                map[i][j]=new Tile(true);
    }
}
