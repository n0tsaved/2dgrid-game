package tilemap;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 */
public class GameController implements KeyListener{
    private Player player;
    private Game g;
    private boolean left, right, up, down;

    public GameController( Game g, Player p)
    {
        this.g=g;
        player=p;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // check the keyboard and record which keys are pressed
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_F2)
            g.setNewGameMap(new IndoorMapGenerator());
        if (e.getKeyCode() == KeyEvent.VK_F3)
            g.setNewGameMap(new OutdoorMapGenerator());
        if (e.getKeyCode() == KeyEvent.VK_F4)
            g.setNewGameMap(new DungeonMapGenerator());

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // check the keyboard and record which keys are released
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = false;
        }
    }
    public void logic(double delta) {
        // check the keyboard and record which way the player
        // is trying to move this loop
        int dx = 0;
        int dy = 0;
        if (left) {
            dx -= 1;
        }
        if (right) {
            dx += 1;
        }
        if (up) {
            dy -= 1;
        }
        if (down) {
            dy += 1;
        }

        // if the player needs to move attempt to move the entity
        // based on the keys multiplied by the amount of time thats
        // passed
        if ((dx != 0 ) || (dy != 0 )) {
            player.move(  (int) (dx*delta*0.5f),(int) (dy*delta*0.5f));
        }
        /*if(dx!=0||dy!=0){
            player.move(dx,dy);
        }*/
    }
}
