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
    private static final int DELAY = 20;
    private static final String MENU_CARD = "Menu";
    private static final String GAME_CARD = "Game";

    /**
     * Creates the main game window
     */
    public GameFrame() {
        cardPanel = new JPanel(new CardLayout());

        mainMenu = new MainMenu(
                () -> startGame(),
                () -> startGame()
        );

        cardPanel.add(mainMenu, MENU_CARD);
        add(cardPanel);

        setTitle("Space Invaders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
    }

    private void startGame() {
        model = new GameModel();
        gamePanel = new GamePanel(model, this::returnToMenu, this);
        controller = new GameController(model);

        JButton restartButton = gamePanel.getRestartButton();
        restartButton.addActionListener(e -> {
            restartGame();
            requestFocus();
        });

        gamePanel.setLayout(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        restartButton.setBounds(screenSize.width/2 - 50, screenSize.height/2 + 50, 100, 40);

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
        
        cardPanel.add(gamePanel, GAME_CARD);
        
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, GAME_CARD);
        
        startGameTimer();
        requestFocus();
        setFocusable(true);
    }

    private void startGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new Timer(DELAY, e -> {
            model.update();
            controller.handleAlienShooting();
            controller.checkCollisions();
            gamePanel.repaint();
            gamePanel.getRestartButton().setVisible(model.isGameOver());
        });
        gameTimer.start();
    }

    private void restartGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        if (model != null) {
            model.cleanup();
        }
        
        model = new GameModel();
        controller = new GameController(model);
        
        gamePanel.setModel(model);
        startGameTimer();
        requestFocus();
    }

    public void reloadGameState(GameModel loadedModel) {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        if (model != null) {
            model.cleanup();
        }
        
        model = loadedModel;
        controller = new GameController(model);
        
        gamePanel.setModel(model);
        startGameTimer();
        requestFocus();
    }

    private void returnToMenu() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
        
        if (keyAdapter != null) {
            removeKeyListener(keyAdapter);
            keyAdapter = null;
        }
        
        if (model != null) {
            model.cleanup();
            model = null;
        }
        
        if (gamePanel != null) {
            cardPanel.remove(gamePanel);
            gamePanel = null;
        }
        
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, MENU_CARD);
        
        requestFocus();
        setFocusable(true);
    }
} 