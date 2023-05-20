package me.lab7.server.command;


import me.lab7.common.data.LabWork;
import me.lab7.common.exception.MustBeEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;

/**
 * class sum of minimal point command
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class SumOfMinimalPointCommand extends AbstractCommand {
    CollectionManager collectionManager;

    public SumOfMinimalPointCommand(CollectionManager collectionManager) {
        super("sum_of_minimal_point", "выводит сумму значений поля minimalPoint для всех элементов коллекции");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String argument, Long client) {
        try {
            if (!argument.isEmpty()) throw new MustBeEmptyException();
            long SumOfMinimalPoints = 0;
            for (LabWork i : collectionManager.getLabWork()) {
                SumOfMinimalPoints += i.getMinimalPoint();
            }
            return new Response("сумму значений поля minimalPoint для всех элементов коллекции = " + SumOfMinimalPoints);
            
        } catch (MustBeEmptyException e) {
            return new Response("Команда вводится без аргумента");
            
        }
    }
}