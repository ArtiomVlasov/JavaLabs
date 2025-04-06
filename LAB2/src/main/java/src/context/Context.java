package src.context;

import java.io.*;
import java.util.*;

public class Context implements Serializable{
    private Stack<Double> stack = new Stack<Double>();
    private Map<String, Double> vars = new HashMap<String, Double>();
    private List<Runnable> stackListeners = new ArrayList<>();
    private Stack<String> history = new Stack<String>();
    private List<Runnable> historyListener = new ArrayList<>();
    // Метод для добавления слушателя
    public void addListener(List<Runnable> listeners,Runnable listener) {
        listeners.add(listener);
    }

    // Уведомление слушателей
    private void notifyListeners(List<Runnable> listeners) {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public List<Runnable> getStackListeners() {
        return stackListeners;
    }

    public Stack<String> getHistory() {
        return history;
    }

    public void setHistory(Stack<String> history) {
        this.history = history;
    }

    public List<Runnable> getHistoryListener() {
        return historyListener;
    }

    // Методы для работы со стеком
    public<T> void pushToStack(Stack<T> stack, List<Runnable> listeners ,T value) {
        stack.push(value);
        notifyListeners(listeners); // Уведомляем слушателей об изменении
    }

    public Double popFromStack() {
        if (!stack.isEmpty()) {
            Double value = stack.pop();
            notifyListeners(stackListeners); // Уведомляем слушателей об изменении
            return value;
        }
        return null;
    }

    public void saveState(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(stack);
            out.writeObject(vars);
            out.writeObject(history);
        }
    }

    public void loadState(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            stack.clear();
            stack.addAll((Stack<Double>) in.readObject());
            notifyListeners(stackListeners);

            vars.clear();
            vars.putAll((Map<String, Double>) in.readObject());


            history.clear();
            history.addAll((Stack<String>) in.readObject());
            notifyListeners(historyListener);


        }
    }

    public Map<String, Double> getVars() {
        return vars;
    }

    public void setVars(Map<String, Double> vars) {
        this.vars = vars;
    }

    public  void setVar(String var, Double val){
        vars.put(var, val);
    }

    public Stack<Double> getStack() {
        return stack;
    }

    public void setStack(Stack<Double> stack) {
        this.stack = stack;
    }
}
