package me.lab7.server.command;

import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.SqlCollectionManager;


/**
 * command that deletes all laboratory works is greater than the entered
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class RemoveGreaterCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final SqlCollectionManager sqlCollectionManager;

    public RemoveGreaterCommand(CollectionManager collectionManager, SqlCollectionManager sqlCollectionManager) {
        super("remove_greater {element}", "удаляет из коллекции все элементы, превышающие заданный");
        this.collectionManager = collectionManager;
        this.sqlCollectionManager = sqlCollectionManager;
    }

    @Override
    public Response execute(String argument, Long client) {
        try {
            if (argument.isEmpty()) throw new MustBeNotEmptyException();
            sqlCollectionManager.removeGreater(Long.parseLong(argument.trim()), client);
            var count = collectionManager.removeGreater(Long.parseLong(argument.trim()), client);
            if (count > 0) {
                return new Response("при вызове команды remove_greater удалено " + count + " лабораторных работ");
            }
            return new Response("Неверные права доступа");


        } catch (MustBeNotEmptyException e) {
            return new Response("Id не введен");

        } catch (NullPointerException e) {
            return new Response("Лабораторной работы с таким Id отсутствует");

        } catch (NumberFormatException e) {
            return new Response("Некорректный ввод");

        } catch (Exception e) {
            return new Response("Ошибка при удалении");
        }
    }
}