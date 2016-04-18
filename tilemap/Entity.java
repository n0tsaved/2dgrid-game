package tilemap;

import java.awt.*;


public class Entity {
    /** The x position of this entity in terms of grid cells */
    private int x;
    /** The y position of this entity in terms of grid cells */
    private int y;
    /** The image to draw for this entity */
    private String image;
    /** The gameMap which this entity is wandering around */
    private GameMap gameMap;
    /** The angle to draw this entity at */
    //private float ang;
    /** The size of this entity, this is used to calculate collisions with walls */
    //private float size = 0.3f;

    /**
     * Create a new entity in the game
     *
     * @param image The image to represent this entity (needs to be 32x32)
     * @param gameMap The gameMap this entity is going to wander around
     * @param x The initial x position of this entity in grid cells
     * @param y The initial y position of this entity in grid cells
     */
    public Entity(String image, GameMap gameMap, int x, int y) {
        this.image = image;
        this.gameMap = gameMap;
        this.x = x;
        this.y = y;
    }

    /**
     * Move this entity a given amount. This may or may not succeed depending
     * on collisions
     *
     * @param dx The amount to move on the x axis
     * @param dy The amount to move on the y axis
     * @return True if the move succeeded
     */
    public boolean move(int dx, int dy) {
        // work out what the new position of this entity will be
        int nx = x + dx;
        int ny = y + dy;

        // check if the new position of the entity collides with
        // anything
        if (validLocation(nx, ny)) {
            // if it doesn't then change our position to the new position
            x = nx;
            y = ny;
//System.out.println("Position: "+x+","+y+"blocked: "+gameMap.getData()[x][y].isBlocked);
            // and calculate the angle we're facing based on our last move
            //ang = (float) (Math.atan2(dy, dx) - (Math.PI / 2));
            return true;
        }

        // if it wasn't a valid move don't do anything apart from
        // tell the caller
        return false;
    }

    /**
     * Check if the entity would be at a valid location if its position
     * was as specified
     *
     * @param nx The potential x position for the entity
     * @param ny The potential y position for the entity
     * @return True if the new position specified would be valid
     */
    public boolean validLocation(int nx, int ny) {
        // here we're going to check some points at the corners of
        // the player to see whether we're at an invalid location
        // if any of them are blocked then the location specified
        // isn't valid
        if (gameMap.blocked(nx, ny)) {
            return false;
        }
        if(nx>=GameMap.WIDTH || ny >GameMap.HEIGHT || nx<0 || ny<=0)
            return false;
        /*if (gameMap.blocked(nx + size, ny - size)) {
            return false;
        }
        if (gameMap.blocked(nx - size, ny + size)) {
            return false;
        }
        if (gameMap.blocked(nx + size, ny + size)) {
            return false;
        }*/

        // if all the points checked are unblocked then we're in an ok
        // location
        return true;
    }

    /**
     * Draw this entity to the graphics context provided.
     *
     * @param g The graphics context to which the entity should be drawn
     */
    public void paint(Graphics2D g) {
        // work out the screen position of the entity based on the
        // x/y position and the size that tiles are being rendered at. So
        // if we're at 1.5,1.5 and the tile size is 10 we'd render on screen
        // at 15,15.
        int xp = (int) (Tile.TILE_SIZE * x);
        int yp = (int) (Tile.TILE_SIZE * y);
        g.setColor(Color.RED);

        // rotate the sprite based on the current angle and then
        // draw it
        //g.rotate(ang, xp, yp);
        g.drawString(image, xp, yp);

        //g.drawImage(image, (int) (xp - 16), (int) (yp - 16), null);
        //g.rotate(-ang, xp, yp);
    }
}
