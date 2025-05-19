package org.spaceinvaders.view;

import org.spaceinvaders.sound.SoundManager;
import javax.swing.*;
import java.awt.*;

public class MainMenu extends JPanel {
    private JButton player1Button;
    private JButton player2Button;
    private JButton exitButton;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.GREEN;
    private static final Font TITLE_FONT = new Font("Helvetica", Font.BOLD, 36);
    private static final Font BUTTON_FONT = new Font("Helvetica", Font.BOLD, 20);
    private SoundManager soundManager;

    public MainMenu(Runnable firstPlayerAction, Runnable secondPlayerAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BACKGROUND_COLOR);
        
        soundManager = SoundManager.getInstance();
        
        add(Box.createVerticalStrut(50));
        
        JLabel titleLabel = new JLabel("SPACE INVADERS");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        
        add(Box.createVerticalStrut(50));
        
        player1Button = createButton("FIRST PLAYER", () -> {firstPlayerAction.run();
        });
        add(player1Button);
        
        add(Box.createVerticalStrut(20));
        
        player2Button = createButton("SECOND PLAYER", () -> {
            secondPlayerAction.run();
        });
        add(player2Button);
        
        add(Box.createVerticalStrut(20));
        
        JButton highScoresButton = createButton("HIGH SCORES", () -> {
        });
        add(highScoresButton);
        
        add(Box.createVerticalStrut(20));
        
        exitButton = createButton("EXIT", () -> {
            System.exit(0);
        });
        add(exitButton);
        
        add(Box.createVerticalGlue());
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createLineBorder(TEXT_COLOR, 2));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 50));
        
        button.addActionListener(e -> action.run());
        
        return button;
    }
} 