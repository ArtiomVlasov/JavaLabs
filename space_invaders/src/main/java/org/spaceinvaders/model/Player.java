package org.spaceinvaders.model;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serializable;

/**
 * Represents the player's ship in Space Invaders
 */
public class Player extends GameObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private int dx;
    private int lives;
    private List<Shot> shots;
    private static final int PLAYER_SPEED = 3;
    private static final int INITIAL_LIVES = 3;
    private static final int SHOOT_COOLDOWN = 750;
    private long lastShotTime;

    /**
     * Creates a new player
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public Player(int x, int y) {
        super(x, y, 50, 30);
        this.lives = INITIAL_LIVES;
        this.shots = new ArrayList<>();
        this.lastShotTime = 0;
        initPlayer();
    }

    private void initPlayer() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/images/player.png"));
        } catch (IOException e) {
            System.err.println("Error loading player image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        x += dx;

        if (x < 0) {
            x = 0;
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (x > screenSize.width - width) {
            x = screenSize.width - width;
        }
    }

    /**
     * Fires a shot from the player's position if cooldown has elapsed
     * @return true if shot was fired, false if still in cooldown
     */
    public boolean shoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= SHOOT_COOLDOWN) {
            shots.add(new Shot(x + width / 2, y, false));
            lastShotTime = currentTime;
            return true;
        }
        return false;
    }

    /**
     * Moves the player left
     */
    public void moveLeft() {
        dx = -PLAYER_SPEED;
    }

    /**
     * Moves the player right
     */
    public void moveRight() {
        dx = PLAYER_SPEED;
    }

    /**
     * Stops the player's movement
     */
    public void stop() {
        dx = 0;
    }

    /**
     * Reduces player's lives by 1
     */
    public void die() {
        lives--;
    }

    public List<Shot> getShots() { return shots; }
    public int getLives() { return lives; }
} 