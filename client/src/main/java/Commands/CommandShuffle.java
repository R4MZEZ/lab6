package Commands;

import Server.CollectionManager;

import java.util.Scanner;

public class CommandShuffle implements Command{

    CollectionManager manager;

    public CommandShuffle(CollectionManager manager) {
        this.manager = manager;
    }

    public CommandShuffle() {
    }

    @Override
    public void setManager(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public void execute() {
        manager.shuffle();
    }

    @Override
    public boolean validate(String argument, Scanner reader) {
        return true;
    }
}
