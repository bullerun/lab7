package me.lab7.server.command;
import me.lab7.common.data.LabWork;
import me.lab7.common.exception.MustBeEmptyException;
import me.lab7.common.Response;
import me.lab7.server.manager.CollectionManager;


/**
 * outputs the average value of the minimal Point field for all items in the collection
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class AverageOfMinimalPointCommand extends AbstractCommand {
    CollectionManager collectionManager;

    public AverageOfMinimalPointCommand(CollectionManager collectionManager) {
        super("average_of_minimal_point", "выводит среднее значение поля minimalPoint для всех элементов коллекции");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String argument) {
        try {
            if (!argument.isEmpty()) throw new MustBeEmptyException();
            long SumOfMinimalPoints = 0;
            long count = 0;
            for (LabWork i : collectionManager.getLabWork()) {
                SumOfMinimalPoints += i.getMinimalPoint();
                count += 1;
            }
            return new Response("среднее значение поля minimalPoint для всех элементов коллекции = " + SumOfMinimalPoints / count);

        } catch (MustBeEmptyException e) {
            return new Response("Команда вводится без аргумента");

        }catch (ArithmeticException e){
            return new Response("Нет лабораторных работ");

        }
    }
}