package src.commands;

import src.context.Context;

import java.util.EmptyStackException;
import java.util.Stack;

@CommandInfo(name = "SQRT")
public class SqrtCommand implements Command {
    @Override
    public void execute(Context context, String... args) {
        if(context.getStack().isEmpty()){
            throw new EmptyStackException();
        }
        Double a = context.popFromStack();
        context.pushToStack(context.getStack(), context.getStackListeners(), Math.sqrt(a));
    }
}
