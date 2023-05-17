package me.lab7.server.command;

import me.lab7.common.data.LabWork;
import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;

/**
 * command deleting all laboratory work less than the entered
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class RemoveLowerCommand extends AbstractCommand {
    CollectionManager collectionManager;

    public RemoveLowerCommand(CollectionManager collectionManager) {
        super("remove_lower {element}", "удаляет из коллекции все элементы, меньшие, чем заданный");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String argument) {
        try {
            if (argument.isEmpty()) throw new MustBeNotEmptyException();
            long removeLabWorksId = Long.parseLong(argument.trim());
            LabWork removeLabWorks = collectionManager.getElementById(removeLabWorksId);
            if (removeLabWorks == null) throw new NullPointerException();
            collectionManager.removeLower(removeLabWorks);
            return new Response("Удаления завершены успешно");
            
        } catch (MustBeNotEmptyException e) {
            return new Response("Id не введен");
            
        } catch (NullPointerException e) {
            return new Response("Лабораторной работы с таким Id отсутствует");
            
        } catch (NumberFormatException e) {
            return new Response("Некорректный ввод");
            
        }
    }
}