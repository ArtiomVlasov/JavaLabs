package org.spaceinvaders.model;

import java.awt.Rectangle;
import java.awt.Image;

/**
 * Base class for all game objects in Space Invaders
 */
public abstract class GameObject {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible;
    protected Image image;

    /**
     * Creates a new game object
     * @param x The x coordinate
     * @param y The y coordinate
     * @param width The width of the object
     * @param height The height of the object
     */
    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
    }

    /**
     * Updates the game object's state
     */
    public abstract void update();

    /**
     * Gets the bounds of the game object for collision detection
     * @return Rectangle representing the bounds
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
} 