package org.spaceinvaders.model;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents an alien enemy in Space Invaders
 */
public class Alien extends GameObject {
    private static final float BASE_SPEED = 4.0f;
    private int points;
    private int row;
    private boolean movingRight;
    private AlienType type;
    private float speedMultiplier;

    public enum AlienType {
        FIRST(10, "first_type_enemy.png"),
        SECOND(20, "second_type_enemy.png"),
        THIRD(30, "third_type_enemy.png");

        private final int points;
        private final String imageFile;

        AlienType(int points, String imageFile) {
            this.points = points;
            this.imageFile = imageFile;
        }
    }

    /**
     * Creates a new alien
     * @param x The x coordinate
     * @param y The y coordinate
     * @param row The row number (affects alien type)
     */
    public Alien(int x, int y, int row) {
        super(x, y, 50, 50);
        this.row = row;
        this.speedMultiplier = 2.0f;
        
        // Determine alien type based on row
        if (row < 2) {
            this.type = AlienType.FIRST;
        } else if (row < 4) {
            this.type = AlienType.SECOND;
        } else {
            this.type = AlienType.THIRD;
        }
        
        this.points = type.points;
        this.movingRight = true;
        initAlien();
    }

    private void initAlien() {
        try {
            image = ImageIO.read(new File("src/main/java/images/" + type.imageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        float currentSpeed = BASE_SPEED * speedMultiplier;
        if (movingRight) {
            x += currentSpeed;
        } else {
            x -= currentSpeed;
        }
    }

    /**
     * Makes the alien move down and change direction
     */
    public void moveDown() {
        y += 50;
        movingRight = !movingRight;
    }

    /**
     * Creates a shot from the alien's position
     * @return A new shot object
     */
    public Shot shoot() {
        return new Shot(x + width / 2, y + height, true);
    }

    /**
     * Sets the speed multiplier for the alien
     * @param multiplier The speed multiplier
     */
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    // Getters
    public int getPoints() { return points; }
    public int getRow() { return row; }
    public boolean isMovingRight() { return movingRight; }
    public AlienType getType() { return type; }
} 