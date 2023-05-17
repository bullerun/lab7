package me.lab7.common;

import me.lab7.common.data.LabWork;

import java.io.Serializable;

public class RequestWithLabWork implements Serializable {
    private String command;
    private LabWork labWork;

    public RequestWithLabWork(String command, LabWork labWork) {
        this.command = command;
        this.labWork = labWork;
    }

    public String getCommands() {
        return command;
    }

    public LabWork getLabWork() {
        return labWork;
    }
}