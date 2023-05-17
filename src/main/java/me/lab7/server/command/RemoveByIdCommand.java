package me.lab7.server.command;

import me.lab7.common.data.LabWork;
import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;

/**
 * command that deletes the specified laboratory work
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class RemoveByIdCommand extends AbstractCommand {
    CollectionManager collectionManager;

    public RemoveByIdCommand(CollectionManager collectionManager) {
        super("remove_by_id id", "удаляет элемент из коллекции по его id");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String argument) {
        try {
            if (argument.isEmpty()) throw new MustBeNotEmptyException();
            long removeLabWorkId = Long.parseLong(argument.trim());
            LabWork removeLabWork = collectionManager.getElementById(removeLabWorkId);
            if (removeLabWork == null) throw new NullPointerException();
            collectionManager.removeLabWork(removeLabWork);
            return new Response("Лабораторная работа успешно удалена");
            
        } catch (MustBeNotEmptyException e) {
            return new Response("Id не введен");
            
        } catch (NullPointerException e) {
            return new Response("Лабораторной работы с таким Id отсутствует");
            
        } catch (NumberFormatException e) {
            return new Response("некорректно введено число, число должно содержать только цифры и должно быть меньше или равно " + Long.MAX_VALUE);
        }
    }
}
