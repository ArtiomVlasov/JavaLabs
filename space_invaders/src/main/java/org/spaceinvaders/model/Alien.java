package org.spaceinvaders.model;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents an alien enemy in Space Invaders
 */

//FIXME создать класс командир который отслежовает состояние всех в его строке, решает что нужно спуститься вниз
public class Alien extends GameObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final float BASE_SPEED = 4.0f;
    private int points;
    private int row;
    private boolean movingRight;
    private AlienType type;
    private float speedMultiplier;

    public enum AlienType {
        FIRST(10, "/images/first_type_enemy.png"),
        SECOND(20, "/images/second_type_enemy.png"),
        THIRD(30, "/images/third_type_enemy.png");

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
            image = ImageIO.read(getClass().getResourceAsStream(type.imageFile));
        } catch (IOException e) {
            System.err.println("Error loading alien image: " + e.getMessage());
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

    //FIXME убрать shoot в update, логику стрельбы внести в класс alien
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

    public int getPoints() { return points; }
    public int getRow() { return row; }
    public boolean isMovingRight() { return movingRight; }
    public AlienType getType() { return type; }
} 