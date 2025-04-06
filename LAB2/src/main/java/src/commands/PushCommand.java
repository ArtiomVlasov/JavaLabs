package src.commands;

import src.context.Context;

@CommandInfo(name = "PUSH")
public class PushCommand implements Command {

    @Override
    public void execute(Context context, String... args) {
        if(args.length != 1){
            throw new ArrayIndexOutOfBoundsException("Bad arguments for PUSH.\nPUSH command requires one argument");
        }
        try{
            double num = Double.parseDouble(args[0]);
            context.pushToStack(context.getStack(), context.getStackListeners(), num);
        }
        catch (NumberFormatException e){
            if(context.getVars().containsKey(args[0])){
                context.pushToStack(context.getStack(), context.getStackListeners(), context.getVars().get(args[0]));
            }
            else{
                throw new IllegalArgumentException("Unknown parameter: " + args[0]);
            }
        }
    }
}
