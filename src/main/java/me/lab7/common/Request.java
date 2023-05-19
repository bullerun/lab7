package me.lab7.common;

import me.lab7.client.Authentication;

import java.io.Serializable;

public class Request implements Serializable {
    private String[] commands;
    private Authentication client;
    public Request(String[] commands, Authentication client){
        this.commands = commands;
        this.client = client;
    }

    public String[] getCommands() {
        return commands;
    }

    public Authentication getClient(){
        return client;
    }
}
