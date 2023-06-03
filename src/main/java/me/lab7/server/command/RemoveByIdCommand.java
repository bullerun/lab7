package me.lab7.server.command;

import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.SqlCollectionManager;

import java.sql.SQLException;

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
            if (collectionManager.removeByID(Long.parseLong(argument.trim()), client)) {
                return new Response("Лабораторная работа c таким id удалена");
            }
            return new Response("Удаление не было осуществлено, проверьте наличие данной лабораторной и права на эту лабораторную работу");
        } catch (MustBeNotEmptyException e) {
            return new Response("Id не введен");

        } catch (NumberFormatException e) {
            return new Response("некорректно введено число, число должно содержать только цифры и должно быть меньше или равно " + Long.MAX_VALUE);
        } catch (SQLException e) {
            return new Response("такой лабораторной нет");
        } catch (Exception e) {
            return new Response("Произошла неизвестная ошибка");
        }
    }
}
