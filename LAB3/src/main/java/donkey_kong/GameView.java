package donkey_kong;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameView extends JPanel {
    private GameModel model;
    private Image background;
    private Image playerTexture;

    public GameView(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        
        // Load textures
        playerTexture = TextureManager.getTexture("a.png");
        background = TextureManager.getTexture("background-2.jpg");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        if (background != null) {
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }

        // Draw platforms
        for (Platform platform : model.getCurrentLevel().getPlatforms()) {
            platform.render(g2d);
        }

        // Draw stairs
        for (Stairs stairs : model.getCurrentLevel().getStairs()) {
            stairs.render(g2d);
        }

        // Draw barrels
        for (Barrels barrel : model.getBarrels()) {
            barrel.render(g2d);
        }

        // Draw player
        PlayerModel player = model.getPlayer();
        if (playerTexture != null) {
            g2d.drawImage(playerTexture, 
                         player.getPosX(), 
                         player.getPosY(), 
                         30, 30, null);
        }

        // Draw score and lives
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + player.getScore(), 20, 30);
        g2d.drawString("Lives: " + player.getLives(), 20, 60);

        // Draw level number
        g2d.drawString("Level: " + model.getCurrentLevelNumber(), getWidth() - 100, 30);
    }

    public void update() {
        repaint();
    }
} 