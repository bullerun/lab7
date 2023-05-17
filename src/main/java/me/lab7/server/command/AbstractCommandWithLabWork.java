package me.lab7.server.command;

public abstract class AbstractCommandWithLabWork implements CommandWithLabWork{
    private final String name;
    private final String description;
    public AbstractCommandWithLabWork(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
