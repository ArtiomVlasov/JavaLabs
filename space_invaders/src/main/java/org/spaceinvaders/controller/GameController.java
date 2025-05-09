package org.spaceinvaders.controller;

import org.spaceinvaders.model.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


/**
 * Handles game input and controls
 */
public class GameController {
    private GameModel model;
    private boolean leftPressed;
    private boolean rightPressed;
    private static final int ALIENS_IN_ROW = 8;
    private static final int ALIENS_IN_COLUMN = 5;

    /**
     * Creates a new game controller
     * @param model The game model to control
     */
    public GameController(GameModel model) {
        this.model = model;
        initGame();
    }

    /**
     * Initializes or resets the game state
     */
    public void initGame() {
        model.setPlayer(new Player(model.getBoardWidth()/2, model.getBoardHeight() - 100));
        model.setAliens(new ArrayList<>());
        model.setAlienShots(new ArrayList<>());
        model.setWalls(new ArrayList<>());
        model.setScore(0);
        model.setGameOver(false);
        
        // Calculate starting position to center aliens horizontally
        int startX = (model.getBoardWidth() - ALIENS_IN_ROW) / 2;
        
        // Create alien formation with different types
        // First two rows - type 1 (bottom)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < ALIENS_IN_ROW; j++) {
                model.getAliens().add(new Alien(startX + j * 80, 120 + (ALIENS_IN_COLUMN - 1 - i) * 60, i));
            }
        }
        
        // Next two rows - type 2 (middle)
        for (int i = 2; i < 4; i++) {
            for (int j = 0; j < ALIENS_IN_ROW; j++) {
                model.getAliens().add(new Alien(startX + j * 80, 120 + (ALIENS_IN_COLUMN - 1 - i) * 60, i));
            }
        }
        
        // Last row - type 3 (top)
        for (int j = 0; j < ALIENS_IN_ROW; j++) {
            model.getAliens().add(new Alien(startX + j * 80, 120, 4));
        }

        createWalls();
    }

    private void createWalls() {
        int wallY = model.getBoardHeight() - 250;  // Higher wall position
        
        // Calculate wall positions to be evenly spaced
        int wallWidth = 16 * 6;
        int totalWidth = 4 * wallWidth;
        int spacing = (model.getBoardWidth() - totalWidth) / 5;
        int[] wallX = new int[4];
        for (int i = 0; i < 4; i++) {
            wallX[i] = spacing + i * (wallWidth + spacing);
        }
        
        for (int wall = 0; wall < 4; wall++) {
            int baseX = wallX[wall];
            for (int row = 0; row < 12; row++) {
                for (int col = 0; col < 16; col++) {
                    if ((row < 3 && (col < 3 || col >= 13)) ||
                        (row >= 9 && col >= 5 && col < 11)) {
                        continue;
                    }
                    model.getWalls().add(new WallPiece(baseX + col * 6, wallY + row * 6));
                }
            }
        }
    }

    public void checkCollisions() {
        // Check player shots hitting aliens or walls
        for (Shot shot : model.getPlayer().getShots()) {
            // Check aliens
            for (Alien alien : model.getAliens()) {
                if (shot.getBounds().intersects(alien.getBounds())) {
                    shot.setVisible(false);
                    alien.setVisible(false);
                    model.getAliens().remove(alien);

                    model.setScore(model.getScore() + alien.getPoints());
                    model.getSoundManager().playSound("invaderKilled");
                    break;
                }
            }
            // Check walls
            for (WallPiece wall : model.getWalls()) {
                if (shot.getBounds().intersects(wall.getBounds())) {
                    shot.setVisible(false);
                    model.getWalls().remove(wall);
                    break;
                }
            }
        }

        // Check alien shots hitting player or walls
        for (Shot shot : model.getAlienShots()) {
            // Check player
            if (shot.getBounds().intersects(model.getPlayer().getBounds())) {
                shot.setVisible(false);
                model.getPlayer().die();
                if (model.getPlayer().getLives() <= 0) {
                    model.setGameOver(true);
                }
            }
            
            // Check walls
            for (WallPiece wall : model.getWalls()) {
                if (shot.getBounds().intersects(wall.getBounds())) {
                    shot.setVisible(false);
                    model.getWalls().remove(wall);
                    break;
                }
            }
        }
    }

    /**
     * Handles key press events
     * @param e The key event
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                updatePlayerMovement();
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                updatePlayerMovement();
                break;
            case KeyEvent.VK_SPACE:
                if (!model.isGameOver()) {
                    model.playerShoot();
                }
                break;
            case KeyEvent.VK_R:
                if (model.isGameOver()) {
                    // Clean up old model
                    model.cleanup();
                    // Reset game
                    initGame();
                }
                break;
            case KeyEvent.VK_M:
                // Toggle mute
                model.getSoundManager().toggleMute();
                break;
        }
    }

    /**
     * Handles key release events
     * @param e The key event
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                updatePlayerMovement();
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                updatePlayerMovement();
                break;
        }
    }

    private void updatePlayerMovement() {
        if (leftPressed && !rightPressed) {
            model.getPlayer().moveLeft();
        } else if (rightPressed && !leftPressed) {
            model.getPlayer().moveRight();
        } else {
            model.getPlayer().stop();
        }
    }
}