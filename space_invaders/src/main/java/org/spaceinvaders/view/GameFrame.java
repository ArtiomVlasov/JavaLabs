package org.spaceinvaders.view;

import org.spaceinvaders.model.GameModel;
import org.spaceinvaders.controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Main game window
 */
public class GameFrame extends JFrame {
    private GameModel model;
    private GamePanel gamePanel;
    private GameController controller;
    private MainMenu mainMenu;
    private JPanel cardPanel;
    private Timer gameTimer;
    private KeyAdapter keyAdapter;
    private static final int DELAY = 20;  // ~50 FPS
    private static final String MENU_CARD = "Menu";
    private static final String GAME_CARD = "Game";

    /**
     * Creates the main game window
     */
    public GameFrame() {
        cardPanel = new JPanel(new CardLayout());

        // Create main menu
        mainMenu = new MainMenu(
                () -> startGame(),  // Player 1 action
                () -> startGame()    // Player 2 action
        );

        cardPanel.add(mainMenu, MENU_CARD);
        add(cardPanel);

        setTitle("Space Invaders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);  // Allow resizing for full screen

        // Set up full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
    }

    private void startGame() {
        // Create game components
        model = new GameModel();
        gamePanel = new GamePanel(model);
        controller = new GameController(model);

        // Set up restart button
        JButton restartButton = gamePanel.getRestartButton();
        restartButton.addActionListener(e -> {
            restartGame();
            requestFocus(); // Return focus to frame after button click
        });

        // Position restart button
        gamePanel.setLayout(null);  // Use absolute positioning
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        restartButton.setBounds(screenSize.width/2 - 50, screenSize.height/2 + 50, 100, 40);

        // Remove old key listener if exists
        if (keyAdapter != null) {
            removeKeyListener(keyAdapter);
        }

        // Create and add new key listener
        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                controller.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    returnToMenu();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                controller.keyReleased(e);
            }
        };
        addKeyListener(keyAdapter);
        
        // Add game panel to card layout
        cardPanel.add(gamePanel, GAME_CARD);
        
        // Switch to game panel
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, GAME_CARD);
        
        // Start game loop
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new Timer(DELAY, e -> {
            model.update();
            controller.checkCollisions();
            gamePanel.repaint();
            restartButton.setVisible(model.isGameOver());
        });
        gameTimer.start();

        // Ensure focus is set correctly
        requestFocus();
        setFocusable(true);
    }

    private void restartGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // Clean up old game state
        if (model != null) {
            model.cleanup();
        }
        
        // Create new game components
        model = new GameModel();
        controller = new GameController(model);
        
        // Update GamePanel with new model
        gamePanel.setModel(model);
        
        // Start game timer
        gameTimer = new Timer(DELAY, e -> {
            model.update();
            controller.checkCollisions();
            gamePanel.repaint();
            gamePanel.getRestartButton().setVisible(model.isGameOver());
        });
        gameTimer.start();
        
        requestFocus();
    }

    private void returnToMenu() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // Clean up sounds
        if (model != null) {
            model.cleanup();
        }
        
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, MENU_CARD);
        
        // Remove game panel to free resources
        cardPanel.remove(gamePanel);
        
        // Ensure focus is set correctly
        requestFocus();
        setFocusable(true);
    }
} 