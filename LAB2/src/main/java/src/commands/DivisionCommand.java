package src.commands;

import src.context.Context;

import java.util.EmptyStackException;
import java.util.Stack;

@CommandInfo(name = "/")
public class DivisionCommand implements Command{
    @Override
    public void execute(Context context, String... args) {
        if(context.getStack().isEmpty()){
            throw new EmptyStackException();
        }
        if(context.getStack().size() < 2){
            throw new IllegalArgumentException("Attempt to access an element from stack which size less then 2");
        }
        Double a = context.popFromStack();
        Double b = context.popFromStack();
        if(b == 0.0){
            context.pushToStack(context.getStack(), context.getStackListeners(), b);
            context.pushToStack(context.getStack(), context.getStackListeners(), a);
            throw new IllegalArgumentException("Division by zero");
        }
        context.pushToStack(context.getStack(), context.getStackListeners(), a / b);
    }
}
