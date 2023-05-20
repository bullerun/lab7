package me.lab7.server.command;

import me.lab7.common.Response;
import me.lab7.common.data.LabWork;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.SqlCollectionManager;
import me.lab7.server.utility.LabAsk;

/**
 * add lab command
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class AddCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final LabAsk labAsk;
    private final SqlCollectionManager sqlCollectionManager;

    public AddCommand(CollectionManager collectionManager, LabAsk labAsk, SqlCollectionManager sqlCollectionManager) {
        super("add", "добавляет новый элемент в коллекцию");
        this.collectionManager = collectionManager;
        this.labAsk = labAsk;
        this.sqlCollectionManager = sqlCollectionManager;
    }

    @Override
    public Response execute(String argument, Long client) {
        try {
            LabWork labWork = labAsk.addLabWork();
            labWork.setOwnerID(client);
            labWork.setId(sqlCollectionManager.addInDB(labWork, client));
            collectionManager.addToCollection(labWork);
            return new Response("Лабораторная успешно добавлена");
        } catch (Exception e) {
            return new Response("Лабораторная не была добавлена");
        }
    }
}
