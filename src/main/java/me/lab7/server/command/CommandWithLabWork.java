package me.lab7.server.command;

import me.lab7.common.data.LabWork;
import me.lab7.common.Response;

public interface CommandWithLabWork {
    String getDescription();
    String getName();
    Response execute(LabWork labWork);
}
