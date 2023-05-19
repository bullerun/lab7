package me.lab7.server;


import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.CommandManager;
import me.lab7.server.utility.LabAsk;
import java.io.IOException;
import java.util.Scanner;



public class Server {

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        CollectionManager collectionManager = new CollectionManager();
        CommandManager commandManager = new CommandManager(collectionManager, new LabAsk(scanner));
        ServerInstance serverInstance = new ServerInstance(10643, commandManager, collectionManager, scanner, args[0], args[1]);
        Runtime.getRuntime().addShutdownHook(new Thread(commandManager::save));
        serverInstance.run();
    }
}