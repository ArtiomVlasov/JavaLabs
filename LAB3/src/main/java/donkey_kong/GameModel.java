package donkey_kong;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class GameModel {
    private PlayerModel player;
    private List<EnemyModel> enemies;
    private List<Barrels> barrels;
    private List<Platform> platforms;
    private boolean isGameOver;
    private boolean isPaused;
    private static final String HIGH_SCORES_FILE = "highscores.txt";
    private List<Integer> highScores;
    private Level currentLevel;
    private int currentLevelNumber;
    private static final int MAX_LEVELS = 2;

    public GameModel() {
        player = new PlayerModel();
        enemies = new ArrayList<>();
        barrels = new ArrayList<>();
        platforms = new ArrayList<>();
        highScores = loadHighScores();
        isGameOver = false;
        isPaused = false;
        currentLevelNumber = 1;
        currentLevel = new Level(currentLevelNumber);
        initializeGame();
    }

    public void initializeGame() {
        // Reset game state
        isGameOver = false;
        isPaused = false;
        currentLevelNumber = 1;
        currentLevel = new Level(currentLevelNumber);
        
        // Clear existing game objects
        enemies.clear();
        barrels.clear();
        platforms.clear();
        
        // Set initial player position based on level
        player.setPos(currentLevel.getPlayerStartX(), currentLevel.getPlayerStartY());
        player.setScore(0);
        player.setLives(3);
        
        // Add initial enemies
        enemies.add(new EnemyModel(currentLevel.getDonkeyKongX(), 
                                 currentLevel.getDonkeyKongY(), 2));
    }

    public void nextLevel() {
        if (currentLevelNumber < MAX_LEVELS) {
            currentLevelNumber++;
            currentLevel = new Level(currentLevelNumber);
            player.setPos(currentLevel.getPlayerStartX(), 
                         currentLevel.getPlayerStartY());
            barrels.clear();
        } else {
            // Player won the game
            isGameOver = true;
        }
    }

    public void update() {
        if (isGameOver || isPaused) return;

        // Update player
        player.update(currentLevel);

        // Update enemy positions
        for (EnemyModel enemy : enemies) {
            enemy.move();
        }

        // Update barrels
        for (Barrels barrel : barrels) {
            barrel.update(currentLevel);
        }

        // Spawn new barrel every 350 frames
        if (barrels.size() < 5 && Math.random() < 0.01) {
            barrels.add(new Barrels(currentLevel.getDonkeyKongX(), 
                                  currentLevel.getDonkeyKongY()));
        }

        // Check collisions
        checkCollisions();

        // Check game over conditions
        if (player.getLives() <= 0) {
            gameOver();
        }
    }

    private void checkCollisions() {
        // Check collisions with enemies
        for (EnemyModel enemy : enemies) {
            if (Math.abs(player.getPosX() - enemy.getPosX()) < 30 &&
                Math.abs(player.getPosY() - enemy.getPosY()) < 30) {
                player.decrementLives();
            }
        }

        // Check collisions with barrels
        for (Barrels barrel : barrels) {
            if (barrel.isColliding(player.getPosX(), player.getPosY())) {
                player.decrementLives();
                barrels.remove(barrel);
                break;
            }
        }
    }

    private void gameOver() {
        isGameOver = true;
        updateHighScores(player.getScore());
        saveHighScores();
    }

    private List<Integer> loadHighScores() {
        List<Integer> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scores.add(Integer.parseInt(line.trim()));
            }
        } catch (IOException e) {
            // File doesn't exist yet or error reading
        }
        return scores;
    }

    private void saveHighScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGH_SCORES_FILE))) {
            for (Integer score : highScores) {
                writer.println(score);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHighScores(int newScore) {
        highScores.add(newScore);
        highScores.sort((a, b) -> b - a); // Sort in descending order
        if (highScores.size() > 10) {
            highScores = highScores.subList(0, 10); // Keep only top 10 scores
        }
    }

    // Getters and setters
    public PlayerModel getPlayer() {
        return player;
    }

    public List<EnemyModel> getEnemies() {
        return enemies;
    }

    public List<Barrels> getBarrels() {
        return barrels;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public List<Integer> getHighScores() {
        return highScores;
    }
} 