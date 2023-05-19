package me.lab7.server.command;

import me.lab7.common.exception.MustBeEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;

/**
 * command of saving laboratory work to a file
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class SaveCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public SaveCommand(CollectionManager collectionManager) {
        super("save", "сохраняет коллекцию");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String argument) {
        try {
            if (!argument.isEmpty()) throw new MustBeEmptyException();
            System.out.println("Пока что ничего не делает");
            System.out.println("Коллекция сохранена");
            
        } catch (MustBeEmptyException e) {
            System.out.println("Команда вводится без аргумента");
            
        }
        return null;
    }
}