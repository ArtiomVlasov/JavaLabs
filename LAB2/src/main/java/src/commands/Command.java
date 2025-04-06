package src.commands;

import src.context.Context;

public interface Command {
    void execute(Context context, String... args);
    //boolean validateArgs(); //FIXME написать валидатор для считьывания из файла
}

