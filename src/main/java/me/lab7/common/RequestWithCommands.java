package me.lab7.common;

import me.lab7.client.Authentication;

import java.io.Serializable;
import java.util.ArrayList;

public class RequestWithCommands implements Serializable {
    private ArrayList<String> commands;
    private String name;
    private Authentication client;
    public RequestWithCommands(ArrayList<String> commands, Authentication authentication){
        this.name = "execute_script";
        this.commands = commands;
        this.client = authentication;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public String getName() {
        return name;
    }
    public Authentication getClient(){
        return client;
    }
}
