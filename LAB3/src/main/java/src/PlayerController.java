package src;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlayerController {
    private PlayerModel player;

    private boolean up, down, left, right;

    public PlayerController(PlayerModel player) {
        this.player = player;
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: // вверх
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_S: // вниз
            case KeyEvent.VK_DOWN:
                down = true;
                break;
            case KeyEvent.VK_A: // влево
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_D: // вправо
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            case KeyEvent.VK_SPACE:
                player.shoot(); // выстрел
                break;
        }
        updateMovement();
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
        }
        updateMovement();
    }

    private void updateMovement() {
        int dx = 0, dy = 0;
        if (up) dy -= player.getSpeed();
        if (down) dy += player.getSpeed();
        if (left) dx -= player.getSpeed();
        if (right) dx += player.getSpeed();
        player.setPos(dx, dy);
    }

}
