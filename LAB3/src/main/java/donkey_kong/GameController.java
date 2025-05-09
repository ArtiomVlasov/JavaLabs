package donkey_kong;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameController implements ActionListener {
    private GameModel model;
    private GameView view;
    private PlayerController playerController;
    private Timer gameTimer;
    private static final int DELAY = 16; // approximately 60 FPS

    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;
        this.playerController = new PlayerController(model.getPlayer());
        
        // Set up game timer
        gameTimer = new Timer(DELAY, this);
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update game state
        model.update();
        
        // Update view
        view.update();

        // Check game over
        if (model.isGameOver()) {
            gameTimer.stop();
            showGameOver();
        }
    }

    private void showGameOver() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(view,
                "Game Over!\nFinal Score: " + model.getPlayer().getScore(),
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void startGame() {
        gameTimer.start();
    }

    public void pauseGame() {
        model.setPaused(!model.isPaused());
    }

    public void stopGame() {
        gameTimer.stop();
    }

    public PlayerController getPlayerController() {
        return playerController;
    }
} 