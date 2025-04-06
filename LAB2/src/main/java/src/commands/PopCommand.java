package src.commands;

import src.context.Context;

import java.util.EmptyStackException;
import java.util.Stack;

@CommandInfo(name = "POP")
public class PopCommand implements Command {

    @Override
    public void execute(Context context, String... args) {
        if(!context.getStack().isEmpty()){
            context.popFromStack();
        }
        else throw new EmptyStackException();

    }
}
