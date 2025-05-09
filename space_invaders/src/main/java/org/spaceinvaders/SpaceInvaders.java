package org.spaceinvaders;

import javax.swing.SwingUtilities;
import org.spaceinvaders.view.GameFrame;

public class SpaceInvaders {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame game = new GameFrame();
            game.setVisible(true);
        });
    }
} 