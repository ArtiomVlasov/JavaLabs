package org.spaceinvaders.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.spaceinvaders.sound.SoundManager;
import java.util.Timer;

/**
 * Main game model class that manages game state and logic
 */
public class GameModel {
    private Player player;
    private List<Alien> aliens;
    private List<Shot> alienShots;
    private List<WallPiece> walls;
    private int score;
    private boolean gameOver;
    private Random random;
    private long gameStartTime;
    private float speedMultiplier;
    private static final int BOARD_WIDTH = 1920;  // Standard full HD width
    private static final int BOARD_HEIGHT = 1080;  // Standard full HD height
    private static final float SPEED_INCREASE = 0.5f;  // 50% speed increase each time
    private static final int SPEED_INCREASE_INTERVAL = 9000;  // 9 seconds in milliseconds
    private long lastSpeedIncreaseTime;
    private SoundManager soundManager;
    private Timer alienMoveTimer;
    private Timer alienShootTimer;
    private Timer difficultyTimer;

    /**
     * Creates a new game model
     */
    public GameModel() {
        this.soundManager = SoundManager.getInstance();
        random = new Random();
        initGame();
        
        // Start background music
        soundManager.playBackgroundMusic();
    }

    private void initGame() {
        player = new Player(BOARD_WIDTH/2, BOARD_HEIGHT - 100);  // Adjusted for larger screen
        aliens = new ArrayList<>();
        alienShots = new ArrayList<>();
        walls = new ArrayList<>();
        score = 0;
        gameOver = false;
        gameStartTime = System.currentTimeMillis();
        lastSpeedIncreaseTime = gameStartTime;
        speedMultiplier = 1.0f;
        
        // Calculate starting position to center aliens horizontally
        int totalAlienWidth = 8 * 80;  // Increased spacing for larger screen
        int startX = (BOARD_WIDTH - totalAlienWidth) / 2;
        
        // Create alien formation with different types
        // First two rows - type 1 (bottom)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                aliens.add(new Alien(startX + j * 80, 120 + (5 - 1 - i) * 60, i));  // Higher position, increased spacing
            }
        }
        
        // Next two rows - type 2 (middle)
        for (int i = 2; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                aliens.add(new Alien(startX + j * 80, 120 + (5 - 1 - i) * 60, i));  // Higher position, increased spacing
            }
        }
        
        // Last row - type 3 (top)
        for (int j = 0; j < 8; j++) {
            aliens.add(new Alien(startX + j * 80, 120, 4));  // Higher position
        }

        createWalls();
    }

    private void createWalls() {
        int wallY = BOARD_HEIGHT- 250;  // Higher wall position
        
        // Calculate wall positions to be evenly spaced
        int wallWidth = 16 * 6;  // Increased wall piece size for larger screen
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
                    walls.add(new WallPiece(baseX + col * 6, wallY + row * 6));  // Increased piece size
                }
            }
        }
    }

    /**
     * Updates the game state
     */
    public void update() {
        if (gameOver) {
            return;
        }

        // Update speed multiplier every 9 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpeedIncreaseTime > SPEED_INCREASE_INTERVAL) {
            speedMultiplier += SPEED_INCREASE;
            lastSpeedIncreaseTime = currentTime;
        }

        player.update();
        updateAliens();
        updateShots();

        if (random.nextInt(200) < 2 && !aliens.isEmpty()) {
            Alien shooter = aliens.get(random.nextInt(aliens.size()));
            alienShots.add(shooter.shoot());
        }

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
        
        // Apply current speed multiplier to all aliens
        for (Alien alien : aliens) {
            alien.setSpeedMultiplier(speedMultiplier);
        }
        
        // Check if any alien hits the edges
        for (Alien alien : aliens) {
            alien.update();
            soundManager.playInvaderSound(alien.getRow());
            if (alien.isMovingRight() && alien.getX() > BOARD_WIDTH - 50 ||
                !alien.isMovingRight() && alien.getX() < 50) {
                needsToMoveDown = true;
                break;
            }
        }

        // Move all aliens down if needed
        if (needsToMoveDown) {
            for (Alien alien : aliens) {
                alien.moveDown();
            }
        }
    }

    private void updateShots() {
        // Update player shots
        player.getShots().forEach(Shot::update);
        player.getShots().removeIf(shot -> !shot.isVisible());

        // Update alien shots
        alienShots.forEach(Shot::update);
        alienShots.removeIf(shot -> !shot.isVisible());
    }

    /**
     * Attempts to make the player shoot
     * @return true if shot was fired, false if still in cooldown
     */
    public boolean playerShoot() {
        boolean shot = player.shoot();
        if (shot) {
            soundManager.playSound("shoot");
        }
        return shot;
    }

    public void cleanup() {
        // Stop any running timers
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
        
        // Reset sound manager
        if (soundManager != null) {
            soundManager.reset();
        }
        
        // Clear game objects
        if (aliens != null) aliens.clear();
        if (alienShots != null) alienShots.clear();
        if (walls != null) walls.clear();
        if (player != null && player.getShots() != null) player.getShots().clear();
    }

    // Getters
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

    // Setters
    public void setPlayer(Player player) { this.player = player; }
    public void setAliens(List<Alien> aliens) { this.aliens = aliens; }
    public void setWalls(List<WallPiece> walls) { this.walls = walls; }
    public void setAlienShots(List<Shot> alienShots) { this.alienShots = alienShots; }
    public void setScore(int score) { this.score = score; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
} 