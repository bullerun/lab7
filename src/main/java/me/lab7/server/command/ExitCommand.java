package me.lab7.server.command;


import me.lab7.common.exception.MustBeEmptyException;
import me.lab7.common.Response;

/**
 * commands the shutdown
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class ExitCommand extends AbstractCommand {
    public ExitCommand() {
        super("exit", "завершает программу (без сохранения в файл)");
    }

    @Override
    public Response execute(String argument, Long clint) {
        try {
            if (!argument.isEmpty()) throw new MustBeEmptyException();
            System.exit(0);

        } catch (MustBeEmptyException e) {
            System.out.println("Команда вводится без аргумента");
        }
        return null;
    }
}
