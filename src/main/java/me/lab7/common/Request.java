package me.lab7.common;

import java.io.Serializable;

public class Request implements Serializable {
    private String[] commands;
    public Request(String[] commands){
        this.commands = commands;
    }

    public String[] getCommands() {
        return commands;
    }
}
