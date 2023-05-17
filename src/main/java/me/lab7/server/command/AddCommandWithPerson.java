package me.lab7.server.command;

import me.lab7.common.data.LabWork;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;

public class AddCommandWithPerson extends AbstractCommandWithLabWork {
    private CollectionManager collectionManager;


    public AddCommandWithPerson(CollectionManager collectionManager) {
        super("add", "добавляет новый элемент в коллекцию");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(LabWork labWork) {
        collectionManager.addToCollection(labWork);
        return new Response("Лабораторная успешно добавлена");
    }
}
