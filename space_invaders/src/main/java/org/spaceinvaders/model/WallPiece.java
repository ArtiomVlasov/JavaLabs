package org.spaceinvaders.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Represents a single piece of a protective wall/bunker
 */
public class WallPiece extends GameObject {
    private static final int PIECE_SIZE = 4;  // Size of each wall piece
    private static final Color WALL_COLOR = Color.GREEN;

    /**
     * Creates a new wall piece
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public WallPiece(int x, int y) {
        super(x, y, PIECE_SIZE, PIECE_SIZE);
        createWallImage();
    }

    private void createWallImage() {
        // Create a small square image for the wall piece
        BufferedImage wallImage = new BufferedImage(PIECE_SIZE, PIECE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = wallImage.createGraphics();
        g2d.setColor(WALL_COLOR);
        g2d.fillRect(0, 0, PIECE_SIZE, PIECE_SIZE);
        g2d.dispose();
        this.image = wallImage;
    }

    @Override
    public void update() {}
}