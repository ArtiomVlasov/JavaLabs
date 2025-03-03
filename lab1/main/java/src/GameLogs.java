package src;

import java.io.IOException;
import java.util.logging.*;

/**
 * Class to set up logging for the game
 */
public class GameLogs {

    /**
     * Sets up and returns a configured Logger
     *
     * @param name The name of the logger
     * @return Configured Logger instance
     */
    public static Logger setupLogger(String name) {
        Logger logger = Logger.getLogger(name);

        // Remove default console handlers
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        try {
            // Set up a file handler
            FileHandler fileHandler = new FileHandler("/home/archi/IdeaProjects/JavaLabs/lab1/game_logs.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Failed to set up file logging: " + e.getMessage());
        }

        // Add a console handler for immediate feedback
        /*ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
        */
        logger.setUseParentHandlers(false);
        // Set the logger level


        return logger;
    }
}