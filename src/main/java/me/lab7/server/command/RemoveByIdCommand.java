package me.lab7.server.command;

import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.SqlCollectionManager;

/**
 * command that deletes the specified laboratory work
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class RemoveByIdCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final SqlCollectionManager sqlCollectionManager;

    public RemoveByIdCommand(CollectionManager collectionManager, SqlCollectionManager sqlCollectionManager) {
        super("remove_by_id id", "удаляет элемент из коллекции по его id");
        this.collectionManager = collectionManager;
        this.sqlCollectionManager = sqlCollectionManager;
    }

    @Override
    public Response execute(String argument, Long client) {
        try {
            if (argument.isEmpty()) throw new MustBeNotEmptyException();
            sqlCollectionManager.removeByID(Long.parseLong(argument.trim()), client);
            collectionManager.removeByID(Long.parseLong(argument.trim()), client);
            return new Response("Лабораторная работа c таким id теперь точно нету");
        } catch (MustBeNotEmptyException e) {
            return new Response("Id не введен");

        } catch (NumberFormatException e) {
            return new Response("некорректно введено число, число должно содержать только цифры и должно быть меньше или равно " + Long.MAX_VALUE);
        } catch (Exception e) {
            return new Response("Произошла какая-то ошибка");
        }
    }
}
