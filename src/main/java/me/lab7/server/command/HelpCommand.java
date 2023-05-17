package me.lab7.server.command;

import me.lab7.common.exception.MustBeEmptyException;
import me.lab7.common.Response;

import java.util.ArrayList;

/**
 * command outputs help on
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class HelpCommand extends AbstractCommand {
    private ArrayList<Command> commandsForHelpCommand;

    public HelpCommand(ArrayList<Command> commandsForHelpCommand) {
        super("help", "выводит справку по доступным командам");
        this.commandsForHelpCommand = commandsForHelpCommand;
    }

    @Override
    public Response execute(String argument) {
        try {
            StringBuilder s = new StringBuilder();
            if (!argument.isEmpty()) throw new MustBeEmptyException();
            for (Command i : commandsForHelpCommand) {
                s.append("Команда ").append(i.getName()).append(" ").append(i.getDescription()).append("\n");
            }
            return new Response(s.toString());
        } catch (MustBeEmptyException e) {
            return new Response("Команда вводится без аргумента");
        }
    }
}
