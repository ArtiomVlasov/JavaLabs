package src.gui;

import src.context.*;
import src.commands.*;
import src.logs.CalculatorLogger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculatorGui {
    class VariableTableModel extends AbstractTableModel {
        private final Map<String, Double> variables;
        private final String[] columnNames = {"Variable", "Value"};

        public VariableTableModel(Map<String, Double> variables) {
            this.variables = variables;
        }

        @Override
        public int getRowCount() {
            return variables.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String key = (String) variables.keySet().toArray()[rowIndex];
            if (columnIndex == 0) {
                return key;
            } else if (columnIndex == 1) {
                return variables.get(key);
            }
            return null;
        }
    }

    private <T> void updateListModel(Stack<T> stack, DefaultListModel<T> listModel) {
        listModel.clear();
        for (T value : stack) {
            listModel.addElement(value);
        }
    }



    public void createAndShowGUI(Context context, Map<String, Command> factory, Logger logger) {
        logger.info("Creating GUI components...");
        JFrame frame = new JFrame("Stack Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        DefaultListModel<String> historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        context.addListener(context.getHistoryListener(), () -> updateListModel(context.getHistory(), historyModel));
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Calls History"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.3; gbc.weighty = 1.0;
        frame.add(historyScrollPane, gbc);

        DefaultListModel<Double> stackModel = new DefaultListModel<>();
        JList<Double> stackList = new JList<>(stackModel);
        JScrollPane stackScrollPane = new JScrollPane(stackList);
        context.addListener(context.getStackListeners(), () -> updateListModel(context.getStack(), stackModel));
        stackScrollPane.setBorder(BorderFactory.createTitledBorder("Number Stack"));
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 2;
        gbc.weightx = 0.4; gbc.weighty = 0.7;
        frame.add(stackScrollPane, gbc);

        VariableTableModel tableModel = new VariableTableModel(context.getVars());
        JTable table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Variables"));
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridheight = 1;
        gbc.weightx = 0.4; gbc.weighty = 0.3;
        frame.add(tableScrollPane, gbc);

        JTextField inputField = new JTextField(); // FIXME валидировать по тайпингу
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        // inputField.addActionListener();
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        gbc.insets = new Insets(10, 10, 10, 10);
        frame.add(inputField, gbc);

        JButton executeButton = new JButton("Execute");
        gbc.gridx = 2; gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 10, 10);
        frame.add(executeButton, gbc);

        JButton saveButton = new JButton("Save state");
        gbc.gridx = 2; gbc.gridy = 4;
        frame.add(saveButton, gbc);

        JButton loadButton = new JButton("Load state");
        gbc.gridx = 2; gbc.gridy = 0;
        frame.add(loadButton, gbc);

        executeButton.addActionListener(e -> {
            String commandName = inputField.getText().trim();
            if (commandName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Command cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            inputField.setText("");

            logger.info("Executing command: " + commandName);
            context.pushToStack(context.getHistory(), context.getHistoryListener(), commandName);

            String[] parts = commandName.split(" ");
            Command command = factory.get(parts[0].toLowerCase());

            if (command == null) {
                logger.warning("Unknown command: " + parts[0]);
                JOptionPane.showMessageDialog(frame, "Unknown command!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    if (parts[0].equalsIgnoreCase("define")) {
                        command.execute(context, parts[1], parts[2]);
                        tableModel.fireTableDataChanged();
                    } else if (parts[0].equalsIgnoreCase("push")) {
                        command.execute(context, parts[1]);
                    } else {
                        command.execute(context);
                    }
                    logger.info("Command executed successfully: " + commandName);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error executing command: " + commandName, ex);
                    JOptionPane.showMessageDialog(frame, "Error executing " + parts[0] + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        saveButton.addActionListener(e -> {
            try {
                context.saveState("/home/archi/IdeaProjects/JavaLabs/LAB2/src/main/java/src/gui/saves");
                logger.info("State saved successfully.");
                JOptionPane.showMessageDialog(frame, "State saved successfully.", "State", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                logger.warning("Failed to save state."+ ex.getMessage());
                JOptionPane.showMessageDialog(frame, "Error saving state: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadButton.addActionListener(e -> {
            try {
                context.loadState("/home/archi/IdeaProjects/JavaLabs/LAB2/src/main/java/src/gui/saves");
                tableModel.fireTableDataChanged();
                logger.info("State loaded successfully.");
                JOptionPane.showMessageDialog(frame, "State loaded successfully.", "State", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException ex) {
                logger.log(Level.SEVERE, "Failed to load state.", ex);
                JOptionPane.showMessageDialog(frame, "Error loading state: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
        logger.info("GUI initialized successfully.");
    }
}
