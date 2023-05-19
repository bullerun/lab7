package me.lab7.common;

import me.lab7.client.Authentication;
import me.lab7.common.data.LabWork;

import java.io.Serializable;

public class RequestWithLabWork implements Serializable {
    private String command;
    private LabWork labWork;
    private Authentication client;

    public RequestWithLabWork(String command, LabWork labWork, Authentication client) {
        this.command = command;
        this.labWork = labWork;
        this.client = client;
    }

    public String getCommands() {
        return command;
    }

    public LabWork getLabWork() {
        return labWork;
    }

    public Authentication getClient(){
        return client;
    }
}