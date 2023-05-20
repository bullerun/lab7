package me.lab7.server.command;


import me.lab7.common.ResponseWithLabWork;
import me.lab7.server.manager.CollectionManager;


public class UpdateByIdCommand {
    CollectionManager collectionManager;

    public UpdateByIdCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;

    }

    public ResponseWithLabWork execute(String argument, Long client) {
        return new ResponseWithLabWork("Выберете что вы хотите изменить", collectionManager.getElementById(Long.parseLong(argument), client));
    }
}