package me.lab7.server.manager;

import me.lab7.common.data.LabWork;
import me.lab7.common.Response;
import me.lab7.server.command.*;
import me.lab7.server.utility.FileHanding;
import me.lab7.server.utility.LabAsk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * class for interacting with commands
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class CommandManager {
    private Scanner localScanner;
    ArrayList<Command> commandsForHelpCommand = new ArrayList<>();
    ArrayList<String> lastCommands = new ArrayList<>();
    Map<String, Command> commands = new HashMap<>();
    Map<String, CommandWithLabWork> commandsWithLabWork = new HashMap<>();
    private Command saveCommand;
    private ExitCommand exitCommand;
    private LabAsk labAsk;

    public CommandManager(CollectionManager collectionManager, FileHanding fileHanding, LabAsk labAsk) {
        this.labAsk = labAsk;
        localScanner = new Scanner("");
        commandsForHelpCommand.add(new InfoCommand(collectionManager));
        commandsForHelpCommand.add(new HistoryCommand(lastCommands));
        commandsForHelpCommand.add(new ShowCommand(collectionManager));
        commandsForHelpCommand.add(new AddCommand(collectionManager, labAsk));
        commandsForHelpCommand.add(new RemoveByIdCommand(collectionManager));
        commandsForHelpCommand.add(new SumOfMinimalPointCommand(collectionManager));
        commandsForHelpCommand.add(new AverageOfMinimalPointCommand(collectionManager));
        commandsForHelpCommand.add(new ClearCommand(collectionManager));
        commandsForHelpCommand.add(new ExecuteScriptCommand(fileHanding));
        commandsForHelpCommand.add(new PrintFieldDescendingDisciplineCommand(collectionManager));
        commandsForHelpCommand.add(new RemoveGreaterCommand(collectionManager));
        commandsForHelpCommand.add(new RemoveLowerCommand(collectionManager));

        commandsForHelpCommand.add(new HelpCommand(commandsForHelpCommand));
        commandsWithLabWork.put("add", new AddCommandWithPerson(collectionManager));
        commands.put("help", new HelpCommand(commandsForHelpCommand));
        commands.put("info", new InfoCommand(collectionManager));
        commands.put("history", new HistoryCommand(lastCommands));
        commands.put("show", new ShowCommand(collectionManager));
        commands.put("add", new AddCommand(collectionManager, labAsk));
        commands.put("remove_by_id", new RemoveByIdCommand(collectionManager));
        commands.put("sum_of_minimal_point", new SumOfMinimalPointCommand(collectionManager));
        commands.put("average_of_minimal_point", new AverageOfMinimalPointCommand(collectionManager));
        commands.put("clear", new ClearCommand(collectionManager));
        commands.put("print_field_descending_discipline", new PrintFieldDescendingDisciplineCommand(collectionManager));
        commands.put("remove_greater", new RemoveGreaterCommand(collectionManager));
        commands.put("remove_lower", new RemoveLowerCommand(collectionManager));
        saveCommand = new SaveCommand(fileHanding, collectionManager);
        exitCommand = new ExitCommand();
    }

    public Response commandSelection(String[] command) {
        addCommandHistory(command[0]);
        return commands.get(command[0]).execute(command[1]);
    }

    public Response commandSelection(String command, LabWork labWork) {
        addCommandHistory(command);
        return commandsWithLabWork.get(command).execute(labWork);
    }

    public Response commandSelectionFromScript(String command, ArrayList<String> commands) {
        addCommandHistory(command);
        String stringWithCommands = String.join("\n", commands);
        setScanner(new Scanner(stringWithCommands));
        return new Response(String.join("\n", localConsole()) + "\nКоманда execute_script выполнена");
    }
    public ArrayList<String> localConsole() {
        ArrayList<String> response = new ArrayList<>();
        String[] command;
        while (localScanner.hasNext()) {
            command = (localScanner.nextLine().trim() + " ").split(" ", 2);
            if (!command[0].equals("")) {
                if (command[0].equals("add")) {
                    labAsk.setScanner(getScanner());
                }
                Command commandSelect = commands.get(command[0]);
                if (commandSelect != null) {
                    response.add(commandSelect.execute(command[1]).getResponse());
                    addCommandHistory(command[0]);
                }
            }
        }
        return response;
    }

    public Scanner getScanner() {
        return localScanner;
    }

    public void setScanner(Scanner scanner) {
        localScanner = scanner;
    }

    public void addCommandHistory(String command) {
        lastCommands.add(command);
        if (lastCommands.size() > 9) {
            lastCommands.remove(0);
        }
    }

    public void save() {
        addCommandHistory("save");
        saveCommand.execute("");
    }

    public void exit() {
        addCommandHistory("exit");
        exitCommand.execute("");
    }
}
