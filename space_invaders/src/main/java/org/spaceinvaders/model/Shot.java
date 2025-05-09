package org.spaceinvaders.model;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a shot fired by either the player or aliens
 */
public class Shot extends GameObject {
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
            image = ImageIO.read(new File("src/main/java/images/shot.png"));
        } catch (IOException e) {
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

        // Set invisible when shot goes off screen
        if (y < 0 || y > 1080) {  // Using standard full HD height
            visible = false;
        }
    }
} 