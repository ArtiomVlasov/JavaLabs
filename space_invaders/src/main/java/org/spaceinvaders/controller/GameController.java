package org.spaceinvaders.controller;

import org.spaceinvaders.model.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Handles game input and controls
 */
public class GameController {
    private GameModel model;
    private boolean leftPressed;
    private boolean rightPressed;
    private static final int ALIENS_IN_ROW = 8;
    private static final int ALIENS_IN_COLUMN = 5;
    private Random random = new Random();

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

        int startX = (model.getBoardWidth() - ALIENS_IN_ROW) / 2;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < ALIENS_IN_ROW; j++) {
                model.getAliens().add(new Alien(startX + j * 80, 120 + (ALIENS_IN_COLUMN - 1 - i) * 60, i));
            }
        }

        for (int i = 2; i < 4; i++) {
            for (int j = 0; j < ALIENS_IN_ROW; j++) {
                model.getAliens().add(new Alien(startX + j * 80, 120 + (ALIENS_IN_COLUMN - 1 - i) * 60, i));
            }
        }

        for (int j = 0; j < ALIENS_IN_ROW; j++) {
            model.getAliens().add(new Alien(startX + j * 80, 120, 4));
        }

        createWalls();
    }

    private void createWalls() {
        int wallY = model.getBoardHeight() - 250;

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
        for (Shot shot : model.getPlayer().getShots()) {
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
            for (WallPiece wall : model.getWalls()) {
                if (shot.getBounds().intersects(wall.getBounds())) {
                    shot.setVisible(false);
                    model.getWalls().remove(wall);
                    break;
                }
            }
        }

        for (Shot shot : model.getAlienShots()) {
            if (shot.getBounds().intersects(model.getPlayer().getBounds())) {
                shot.setVisible(false);
                model.getPlayer().die();
                if (model.getPlayer().getLives() <= 0) {
                    model.setGameOver(true);
                }
            }

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
            case KeyEvent.VK_M:
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

    /**
     * Handles alien shooting logic
     */
    public void handleAlienShooting() {
        if (model.getAliens().isEmpty() || model.getAlienShots().size() >= model.getMaxAlienShots()) {
            return;
        }

        List<Alien> potentialShooters = new ArrayList<>();
        for (Alien alien : model.getAliens()) {
            boolean isLowest = true;
            int alienX = alien.getX();
            for (Alien other : model.getAliens()) {
                if (other != alien && 
                    Math.abs(other.getX() - alienX) < 30 && other.getY() > alien.getY()) {
                    isLowest = false;
                    break;
                }
            }
            if (isLowest) {
                potentialShooters.add(alien);
            }
        }

        if (!potentialShooters.isEmpty()) {
            potentialShooters.sort((a1, a2) -> {
                int d1 = Math.abs(a1.getX() - model.getPlayer().getX());
                int d2 = Math.abs(a2.getX() - model.getPlayer().getX());
                return Integer.compare(d1, d2);
            });

            int shooterIndex = random.nextInt(potentialShooters.size());

            if (random.nextInt(100) < model.getShotChance()) {
                Alien shooter = potentialShooters.get(shooterIndex);
                model.getAlienShots().add(shooter.shoot());
            }
        }
    }
}