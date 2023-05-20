package me.lab7.server.command;


import me.lab7.common.Response;
import me.lab7.common.ResponseWithLabWork;
import me.lab7.common.data.LabWork;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.SqlCollectionManager;

import java.sql.SQLException;

public class UpdateByIdCommandWithPerson extends AbstractCommandWithLabWork {

    private final CollectionManager collectionManager;
    private final SqlCollectionManager sqlCollectionManager;

    public UpdateByIdCommandWithPerson(CollectionManager collectionManager, SqlCollectionManager sqlCollectionManager) {
        super("update");
        this.collectionManager = collectionManager;
        this.sqlCollectionManager = sqlCollectionManager;
    }

    @Override
    public Response execute(LabWork labWork, Long client) {
        try {
            sqlCollectionManager.update(labWork, client);
            collectionManager.update(labWork, client);
            return new Response("лабораторная работа обновлена");
        } catch (Exception e) {
            return new Response("не получилось обновить лабораторную работу"+e);
        }
    }
}
