package me.lab7.server.command;

import me.lab7.common.exception.MustBeEmptyException;
import me.lab7.common.Response;

import java.util.ArrayList;
/**
 * command outputs the last 9 entered commands
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class HistoryCommand extends AbstractCommand {
    private ArrayList<String> lastCommands;
    public HistoryCommand(ArrayList<String> lastCommands) {
        super("history", "выводит последние 9 команд (без их аргументов)");
        this.lastCommands = lastCommands;
    }

    @Override
    public Response execute(String argument) {
        try {
            StringBuilder s = new StringBuilder();
            if (!argument.isEmpty()) throw new MustBeEmptyException();
            for (int i = lastCommands.size() -1; i >= 0; i--) {
                s.append(lastCommands.get(i)).append("\n");
            }
            return new Response(s.toString());
        }catch (MustBeEmptyException e) {
            return new Response("Команда вводится без аргумента");

        }
    }
}
