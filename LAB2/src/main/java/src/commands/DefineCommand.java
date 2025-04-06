package src.commands;

import src.context.Context;

@CommandInfo(name = "DEFINE")
public class DefineCommand implements Command{

    @Override
    public void execute(Context context, String... args) {
        String var;
        Double val;
        if (args.length != 2){
            throw new ArrayIndexOutOfBoundsException("Bad arguments for DEFINE.\nDEFINE command requires two argument.\nFirst - variable.\nSecond - value");
        }
        else if(!args[0].matches("[a-zA-Z_][a-zA-Z_0-9]*")){
            throw new IllegalArgumentException("Bad first argument.\nFirst argument - variable.");
        }
        var = args[0];
        try {
            val = Double.parseDouble(args[1]);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Bad second argument.\nSecond argument - value.");
        }
        context.getVars().put(var, val);
    }
    //FIXME
}
