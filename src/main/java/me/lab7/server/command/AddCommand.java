package me.lab7.server.command;

import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.utility.LabAsk;

import java.util.NoSuchElementException;

/**
 * add lab command
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class AddCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private LabAsk labAsk;


    public AddCommand(CollectionManager collectionManager, LabAsk labAsk) {
        super("add", "добавляет новый элемент в коллекцию");
        this.collectionManager = collectionManager;
        this.labAsk = labAsk;
    }

    @Override
    public Response execute(String argument) {
        try {
            collectionManager.addToCollection(labAsk.addLabWork());
            return new Response("Лабораторная успешно добавлена");
        }catch (NoSuchElementException e){
            return new Response("Лабораторная не была добавлена");
        }
    }
}
