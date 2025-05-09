package org.spaceinvaders.view;

import org.spaceinvaders.model.GameModel;
import org.spaceinvaders.model.Alien;
import org.spaceinvaders.model.Shot;
import org.spaceinvaders.model.Player;
import org.spaceinvaders.model.WallPiece;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.*;

/**
 * Panel that renders the game
 */
public class GamePanel extends JPanel {
    private GameModel model;
    private JButton restartButton;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font SCORE_FONT = new Font("Helvetica", Font.BOLD, 14);
    private static final Font GAME_OVER_FONT = new Font("Helvetica", Font.BOLD, 32);

    /**
     * Creates a new game panel
     * @param model The game model to render
     */
    public GamePanel(GameModel model) {
        this.model = model;
        setBackground(BACKGROUND_COLOR);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);
        setDoubleBuffered(true);
        
        // Create restart button
        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        restartButton.setVisible(false);
        add(restartButton);
    }

    public JButton getRestartButton() {
        return restartButton;
    }

    public void setModel(GameModel model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        drawGame(g2d);
        drawScore(g2d);

        if (model.isGameOver()) {
            drawGameOver(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawGame(Graphics2D g2d) {
        // Draw walls
        for (WallPiece wall : model.getWalls()) {
            if (wall.isVisible() && wall.getImage() != null) {
                g2d.drawImage(wall.getImage(), wall.getX(), wall.getY(),
                             wall.getWidth(), wall.getHeight(), this);
            }
        }

        // Draw player
        Player player = model.getPlayer();
        if (player.getImage() != null) {
            g2d.drawImage(player.getImage(), player.getX(), player.getY(), 
                         player.getWidth(), player.getHeight(), this);
        }

        // Draw player shots
        for (Shot shot : player.getShots()) {
            if (shot.isVisible() && shot.getImage() != null) {
                g2d.drawImage(shot.getImage(), shot.getX(), shot.getY(),
                             shot.getWidth(), shot.getHeight(), this);
            }
        }

        // Draw aliens
        for (Alien alien : model.getAliens()) {
            if (alien.isVisible() && alien.getImage() != null) {
                g2d.drawImage(alien.getImage(), alien.getX(), alien.getY(),
                             alien.getWidth(), alien.getHeight(), this);
            }
        }

        // Draw alien shots
        for (Shot shot : model.getAlienShots()) {
            if (shot.isVisible() && shot.getImage() != null) {
                g2d.drawImage(shot.getImage(), shot.getX(), shot.getY(),
                             shot.getWidth(), shot.getHeight(), this);
            }
        }

        // Draw lives
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        for (int i = 0; i < model.getPlayer().getLives(); i++) {
            if (player.getImage() != null) {
                g2d.drawImage(player.getImage(), 10 + i * 40, screenSize.height - 40,
                             30, 20, this);
            }
        }
    }

    private void drawScore(Graphics2D g2d) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(SCORE_FONT);
        g2d.drawString("Score: " + model.getScore(), 20, 20);
    }

    private void drawGameOver(Graphics2D g2d) {
        String message = model.getAliens().isEmpty() ? "Victory!" : "Game Over";
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(GAME_OVER_FONT);
        
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        g2d.drawString(message, (screenSize.width - messageWidth) / 2, screenSize.height / 2);
    }
} 