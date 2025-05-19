package org.spaceinvaders.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a shot fired by either the player or aliens
 */
//FIXME проверку коллизии добавить в сам класс пули
public class Shot extends GameObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SHOT_SPEED = 4;
    private boolean isAlienShot;

    /**
     * Creates a new shot
     * @param x The x coordinate
     * @param y The y coordinate
     * @param isAlienShot Whether this shot was fired by an alien
     */
    public Shot(int x, int y, boolean isAlienShot) {
        super(x, y, 3, 15);
        this.isAlienShot = isAlienShot;
        initShot();
    }

    private void initShot() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/images/shot.png"));
        } catch (IOException e) {
            System.err.println("Error loading shot image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if (isAlienShot) {
            y += SHOT_SPEED;
        } else {
            y -= SHOT_SPEED;
        }

        if (y < 0 || y > (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()) {
            visible = false;
        }
    }
} 