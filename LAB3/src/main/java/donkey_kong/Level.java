package donkey_kong;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private int levelNumber;
    private Image background;
    private List<Platform> platforms;
    private List<Stairs> stairs;
    private int donkeyKongX;
    private int donkeyKongY;
    private int princessX;
    private int princessY;
    private int playerStartX;
    private int playerStartY;

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.platforms = new ArrayList<>();
        this.stairs = new ArrayList<>();
        initializeLevel();
    }

    private void initializeLevel() {
        background = TextureManager.getTexture("background");
        
        switch (levelNumber) {
            case 1:
                setupLevel1();
                break;
            case 2:
                setupLevel2();
                break;
        }
    }

    private void setupLevel1() {
        // Level 1 layout - similar to original DK first level
        playerStartX = 50;
        playerStartY = 550;
        donkeyKongX = 100;
        donkeyKongY = 100;
        princessX = 400;
        princessY = 80;

        // Bottom platform
        platforms.add(new Platform(0, 580, 800, 20, 0));
        
        // Inclined platforms
        platforms.add(new Platform(100, 480, 600, 20, 7));  // 7 degrees incline
        platforms.add(new Platform(200, 380, 500, 20, -7)); // -7 degrees incline
        platforms.add(new Platform(100, 280, 600, 20, 7));
        platforms.add(new Platform(200, 180, 500, 20, -7));
        
        // Add stairs
        stairs.add(new Stairs(150, 480, 100));
        stairs.add(new Stairs(550, 380, 100));
        stairs.add(new Stairs(250, 280, 100));
        stairs.add(new Stairs(450, 180, 100));
    }

    private void setupLevel2() {
        // Level 2 layout - more complex with moving platforms
        playerStartX = 50;
        playerStartY = 550;
        donkeyKongX = 700;
        donkeyKongY = 100;
        princessX = 100;
        princessY = 80;

        // Static platforms with steeper inclines
        platforms.add(new Platform(0, 580, 800, 20, 0));
        platforms.add(new Platform(0, 180, 200, 20, 10));
        platforms.add(new Platform(600, 180, 200, 20, -10));
        
        // Moving platforms
        platforms.add(new MoveablePlatform(300, 380, 200, 20, 200, 600));
        platforms.add(new MoveablePlatform(100, 280, 200, 20, 100, 500));
        
        // Add stairs
        stairs.add(new Stairs(150, 480, 100));
        stairs.add(new Stairs(650, 280, 100));
        stairs.add(new Stairs(50, 180, 100));
    }

    // Getters
    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Stairs> getStairs() {
        return stairs;
    }

    public int getPlayerStartX() {
        return playerStartX;
    }

    public int getPlayerStartY() {
        return playerStartY;
    }

    public int getDonkeyKongX() {
        return donkeyKongX;
    }

    public int getDonkeyKongY() {
        return donkeyKongY;
    }

    public int getPrincessX() {
        return princessX;
    }

    public int getPrincessY() {
        return princessY;
    }

    public Image getBackground() {
        return background;
    }
} 