package org.spaceinvaders.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.spaceinvaders.sound.SoundManager;
import java.util.Timer;
import java.awt.Toolkit;
import java.awt.Dimension;
import org.spaceinvaders.model.Alien;
import java.io.Serializable;
import java.io.IOException;

/**
 * Main game model class that manages game state and logic
 */
public class GameModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Player player;
    private List<Alien> aliens;
    private List<Shot> alienShots;
    private List<WallPiece> walls;
    private int score;
    private boolean gameOver;
    private boolean isPaused;
    private long gameStartTime;
    private float speedMultiplier;
    private static final int BOARD_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private static final int BOARD_HEIGHT = (int)  Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private static final float SPEED_INCREASE = 0.5f;
    private static final int SPEED_INCREASE_INTERVAL = 12000;
    private long lastSpeedIncreaseTime;
    private transient SoundManager soundManager;
    private transient Timer alienMoveTimer;
    private transient Timer alienShootTimer;
    private transient Timer difficultyTimer;
    private static final int SHOT_CHANCE = 25;
    private static final int MAX_ALIEN_SHOTS = 6;

    /**
     * Creates a new game model
     */
    public GameModel() {
        this.soundManager = SoundManager.getInstance();
        this.isPaused = false;
        initGame();
    }

    /**
     * Custom deserialization method to reinitialize transient fields
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.soundManager = SoundManager.getInstance();
    }

    /**
     * Initializes or resets the game state
     */
    public void initGame() {
        player = new Player(BOARD_WIDTH/2, BOARD_HEIGHT - 100);
        aliens = new ArrayList<>();
        alienShots = new ArrayList<>();
        walls = new ArrayList<>();
        score = 0;
        gameOver = false;
        isPaused = false;
        gameStartTime = System.currentTimeMillis();
        lastSpeedIncreaseTime = gameStartTime;
        speedMultiplier = 1.0f;

        int totalAlienWidth = 8 * 80;
        int startX = (BOARD_WIDTH - totalAlienWidth) / 2;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                aliens.add(new Alien(startX + j * 80, 120 + (5 - 1 - i) * 60, i));
            }
        }

        for (int i = 2; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                aliens.add(new Alien(startX + j * 80, 120 + (5 - 1 - i) * 60, i));
            }
        }

        for (int j = 0; j < 8; j++) {
            aliens.add(new Alien(startX + j * 80, 120, 4));
        }

        createWalls();
    }

    private void createWalls() {
        int wallY = BOARD_HEIGHT- 250;

        int wallWidth = 16 * 6;
        int totalWidth = 4 * wallWidth;
        int spacing = (BOARD_WIDTH - totalWidth) / 5;
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
                    walls.add(new WallPiece(baseX + col * 6, wallY + row * 6));
                }
            }
        }
    }

    /**
     * Updates the game state
     */
    public void update() {
        if (gameOver || isPaused) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpeedIncreaseTime > SPEED_INCREASE_INTERVAL) {
            speedMultiplier += SPEED_INCREASE;
            lastSpeedIncreaseTime = currentTime;
        }

        player.update();
        updateAliens();
        updateShots();

        for (Alien alien : aliens) {
            if (alien.getY() > BOARD_HEIGHT - 200) {
                gameOver = true;
                break;
            }
        }

        if (aliens.isEmpty()) {
            gameOver = true;
        }
    }

    private void updateAliens() {
        boolean needsToMoveDown = false;

        for (Alien alien : aliens) {
            alien.setSpeedMultiplier(speedMultiplier);
            alien.update();
            soundManager.playInvaderSound(alien.getRow());
        }

        for (Alien alien : aliens) {

            if (alien.isMovingRight() && alien.getX() > BOARD_WIDTH - 60) {
                needsToMoveDown = true;
                break;
            }
            if (!alien.isMovingRight() && alien.getX() < 10) {
                needsToMoveDown = true;
                break;
            }
        }

        if (needsToMoveDown) {
            for (Alien alien : aliens) {
                alien.moveDown();
            }
        }
    }

    private void updateShots() {
        player.getShots().forEach(Shot::update);
        player.getShots().removeIf(shot -> !shot.isVisible());

        alienShots.forEach(Shot::update);
        alienShots.removeIf(shot -> !shot.isVisible());
    }

    /**
     * Attempts to make the player shoot
     */
    public void playerShoot() {
        boolean shot = player.shoot();
        if (shot) {
            soundManager.playSound("shoot");
        }
    }

    public void cleanup() {
        if (alienMoveTimer != null) {
            alienMoveTimer.cancel();
            alienMoveTimer = null;
        }
        if (alienShootTimer != null) {
            alienShootTimer.cancel();
            alienShootTimer = null;
        }
        if (difficultyTimer != null) {
            difficultyTimer.cancel();
            difficultyTimer = null;
        }
        
        if (soundManager != null) {
            soundManager.reset();
        }
        
        if (aliens != null) aliens.clear();
        if (alienShots != null) alienShots.clear();
        if (walls != null) walls.clear();
        if (player != null && player.getShots() != null) player.getShots().clear();
    }

    public Player getPlayer() { return player; }
    public List<Alien> getAliens() { return aliens; }
    public List<Shot> getAlienShots() { return alienShots; }
    public List<WallPiece> getWalls() { return walls; }
    public int getScore() { return score; }
    public boolean isGameOver() { return gameOver; }
    public int getBoardWidth() { return BOARD_WIDTH; }
    public int getBoardHeight() { return BOARD_HEIGHT; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public SoundManager getSoundManager() { return soundManager; }
    public int getShotChance() {
        return SHOT_CHANCE;
    }
    public int getMaxAlienShots() {
        return MAX_ALIEN_SHOTS;
    }

    public void setPlayer(Player player) { this.player = player; }
    public void setAliens(List<Alien> aliens) { this.aliens = aliens; }
    public void setWalls(List<WallPiece> walls) { this.walls = walls; }
    public void setAlienShots(List<Shot> alienShots) { this.alienShots = alienShots; }
    public void setScore(int score) { this.score = score; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void togglePause() {
        isPaused = !isPaused;
    }
} 