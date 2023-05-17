package me.lab7.server.command;

import me.lab7.common.Response;

/**
 * interface from which all commands are implemented
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public interface Command {
    String getDescription();
    String getName();
    Response execute(String argument);
}
