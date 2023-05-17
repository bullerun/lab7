package me.lab7.common;

import java.io.Serializable;
import java.util.ArrayList;

public class RequestWithCommands implements Serializable {
    private ArrayList<String> commands;
    private String name;
    public RequestWithCommands(ArrayList<String> commands){
        this.name = "execute_script";
        this.commands = commands;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public String getName() {
        return name;
    }
}
