package me.lab7.common;

import me.lab7.client.Authentication;

import java.io.Serializable;

public class RequestWithClient implements Serializable {
    private String command;
    private Authentication client;
    public RequestWithClient(String command, Authentication client) {
        this.command = command;
        this.client = client;
    }

    public String getCommand(){
        return command;
    }
    public Authentication getClient(){
        return client;
    }
}
