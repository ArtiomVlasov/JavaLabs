package src;

import src.commands.*;
import src.context.Context;
import src.gui.CalculatorGui;

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
        Map<String, Command> factory = new HashMap<>();
        factory.put("push", new PushCommand());
        factory.put("define", new DefineCommand());
        factory.put("/", new DivisionCommand());
        factory.put("-", new MinusCommand());
        factory.put("+", new PlusCommand());
        factory.put("*", new MultiplicationCommand());
        factory.put("pop", new PopCommand());
        factory.put("sqrt", new SqrtCommand());
        SwingUtilities.invokeLater(() -> new CalculatorGui().createAndShowGUI(context, factory, logger));
        //FIXME class передавать в фабрику и фабрику сделать человеческую, и чтобы команды создавались по мере надобности.
    }
}
