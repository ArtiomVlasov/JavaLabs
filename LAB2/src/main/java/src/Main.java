package src;

import src.commands.*;
import src.context.Context;
import src.gui.CalculatorGui;
import src.factory.CommandsFactory;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import src.logs.CalculatorLogger;

public class Main {
    private static final Logger logger = CalculatorLogger.setupLogger(CalculatorGui.class.getName());

    public static void main(String[] args) {
        logger.info("Launching Stack Calculator GUI...");
        Context context = new Context();
        
        // Create command factory and load commands
        CommandsFactory factory = new CommandsFactory();
        Map<String, Command> commandMap = new HashMap<>();

        SwingUtilities.invokeLater(() -> new CalculatorGui().createAndShowGUI(context, commandMap, factory));
    }
}
