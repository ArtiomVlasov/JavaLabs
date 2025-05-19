package org.spaceinvaders.view;

import org.spaceinvaders.model.GameModel;
import org.spaceinvaders.model.Alien;
import org.spaceinvaders.model.Shot;
import org.spaceinvaders.model.Player;
import org.spaceinvaders.model.WallPiece;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.*;
import java.io.*;
//FIXME MAIN длбавить кнопку паузы и в паузе опции выбрать выйти в меню или сохраниться и в шравном меню загрузить сост ояние игры
/**
 * Panel that renders the game
 */
public class GamePanel extends JPanel {
    private GameModel model;
    private JButton restartButton;
    private JButton pauseButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton exitButton;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font SCORE_FONT = new Font("Helvetica", Font.BOLD, 14);
    private static final Font GAME_OVER_FONT = new Font("Helvetica", Font.BOLD, 32);
    private static final Font BUTTON_FONT = new Font("Helvetica", Font.BOLD, 16);
    private Runnable returnToMenuAction;
    private GameFrame gameFrame;

    /**
     * Creates a new game panel
     * @param model The game model to render
     * @param returnToMenuAction Action to execute when returning to menu
     * @param gameFrame Reference to the game frame
     */
    public GamePanel(GameModel model, Runnable returnToMenuAction, GameFrame gameFrame) {
        this.model = model;
        this.returnToMenuAction = returnToMenuAction;
        this.gameFrame = gameFrame;
        setBackground(BACKGROUND_COLOR);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);
        setDoubleBuffered(true);
        
        initializeButtons();
    }

    private void initializeButtons() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int buttonWidth = 100;
        int buttonHeight = 40;
        int buttonSpacing = 20;
        int startX = screenSize.width - (buttonWidth * 5 + buttonSpacing * 4);
        int startY = 20;

        pauseButton = createButton("Pause", startX, startY, buttonWidth, buttonHeight);
        pauseButton.addActionListener(e -> {
            model.togglePause();
            pauseButton.setText(model.isPaused() ? "Resume" : "Pause");
        });

        saveButton = createButton("Save", startX + buttonWidth + buttonSpacing, startY, buttonWidth, buttonHeight);
        saveButton.addActionListener(e -> saveGame());

        loadButton = createButton("Load", startX + (buttonWidth + buttonSpacing) * 2, startY, buttonWidth, buttonHeight);
        loadButton.addActionListener(e -> loadGame());

        restartButton = createButton("Restart", startX + (buttonWidth + buttonSpacing) * 3, startY, buttonWidth, buttonHeight);
        restartButton.setVisible(false);

        exitButton = createButton("Exit", startX + (buttonWidth + buttonSpacing) * 4, startY, buttonWidth, buttonHeight);
        exitButton.addActionListener(e -> returnToMenuAction.run());
    }

    private JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBounds(x, y, width, height);
        button.setFocusPainted(false);
        add(button);
        return button;
    }

    private void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("game_save.dat"))) {
            oos.writeObject(model);
            JOptionPane.showMessageDialog(this,
                "Game saved successfully!",
                "Save Game",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving game: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
            System.err.println("Error saving game: " + e.getMessage());
        }
    }

    private void loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("game_save.dat"))) {
            GameModel savedModel = (GameModel) ois.readObject();
            gameFrame.reloadGameState(savedModel);
            JOptionPane.showMessageDialog(this,
                "Game loaded successfully!",
                "Load Game",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading game: " + e.getMessage(),
                "Load Error",
                JOptionPane.ERROR_MESSAGE);
            System.err.println("Error loading game: " + e.getMessage());
        }
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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        drawGame(g2d);
        drawScore(g2d);

        if (model.isGameOver()) {
            drawGameOver(g2d);
        }

        if (model.isPaused()) {
            drawPaused(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawPaused(Graphics2D g2d) {
        String message = "PAUSED";
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(GAME_OVER_FONT);
        
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        g2d.drawString(message, (screenSize.width - messageWidth) / 2, screenSize.height / 2);
    }

    private void drawGame(Graphics2D g2d) {
        for (WallPiece wall : model.getWalls()) {
            if (wall.isVisible() && wall.getImage() != null) {
                g2d.drawImage(wall.getImage(), wall.getX(), wall.getY(),
                             wall.getWidth(), wall.getHeight(), this);
            }
        }

        Player player = model.getPlayer();
        if (player.getImage() != null) {
            g2d.drawImage(player.getImage(), player.getX(), player.getY(), 
                         player.getWidth(), player.getHeight(), this);
        }

        for (Shot shot : player.getShots()) {
            if (shot.isVisible() && shot.getImage() != null) {
                g2d.drawImage(shot.getImage(), shot.getX(), shot.getY(),
                             shot.getWidth(), shot.getHeight(), this);
            }
        }

        for (Alien alien : model.getAliens()) {
            if (alien.isVisible() && alien.getImage() != null) {
                g2d.drawImage(alien.getImage(), alien.getX(), alien.getY(),
                             alien.getWidth(), alien.getHeight(), this);
            }
        }

        for (Shot shot : model.getAlienShots()) {
            if (shot.isVisible() && shot.getImage() != null) {
                g2d.drawImage(shot.getImage(), shot.getX(), shot.getY(),
                             shot.getWidth(), shot.getHeight(), this);
            }
        }


        int lifeX = 10;
        int lifeY = getHeight() - 40;
        int lifeWidth = 30;
        int lifeHeight = 20;
        int spacing = 10;
        
        for (int i = 0; i < model.getPlayer().getLives(); i++) {
            if (player.getImage() != null) {
                g2d.drawImage(player.getImage(), 
                             lifeX + (i * (lifeWidth + spacing)), 
                             lifeY, 
                             lifeWidth, 
                             lifeHeight, 
                             this);
            }
        }
        
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(SCORE_FONT);
        g2d.drawString("Lives: " + model.getPlayer().getLives(),lifeX,lifeY - 5);
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