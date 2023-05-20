package me.lab7.server.command;


import me.lab7.common.exception.MustBeEmptyException;
import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.SqlCollectionManager;

/**
 * collection cleanup command
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class ClearCommand extends AbstractCommand{
    private final CollectionManager collectionManager;
    private final SqlCollectionManager sqlCollectionManager;
    public ClearCommand(CollectionManager collectionManager, SqlCollectionManager sqlCollectionManager){
        super("clear", "очищает коллекцию");
        this.collectionManager = collectionManager;
        this.sqlCollectionManager = sqlCollectionManager;
    }
    @Override
    public Response execute(String argument, Long client) {
        try {
            if (!argument.isEmpty()) throw new MustBeEmptyException();
            if (collectionManager.getLabWork().isEmpty()) throw new MustBeNotEmptyException();
            sqlCollectionManager.clear(client);
            collectionManager.clearCollection(client);
        } catch (MustBeEmptyException e) {
            return new Response("Команда вводится без аргумента");

        }catch (MustBeNotEmptyException e){
            return new Response("Коллекция и так пуста");
        }catch (Exception e){
            return new Response("Произошла при попытке очистить коллекцию");
        }
        return new Response("Все ваши лабораторные работы удалены");
    }
}
