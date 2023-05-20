package me.lab7.server.command;


import me.lab7.common.data.LabWork;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.SqlCollectionManager;

public class AddCommandWithPerson extends AbstractCommandWithLabWork {
    private final CollectionManager collectionManager;
    private final SqlCollectionManager sqlCollectionManager;

    public AddCommandWithPerson(CollectionManager collectionManager, SqlCollectionManager sqlCollectionManager) {
        super("add");
        this.collectionManager = collectionManager;
        this.sqlCollectionManager = sqlCollectionManager;
    }

    @Override
    public Response execute(LabWork labWork, Long client) {

        try {
            labWork.setId(sqlCollectionManager.addInDB(labWork, client));
            labWork.setOwnerID(client);
            collectionManager.addToCollection(labWork);
            return new Response("Лабораторная успешно добавлена");
        } catch (Exception e) {
            return new Response("К сожалению лабораторная работа не была добавлена");
        }
    }
}
