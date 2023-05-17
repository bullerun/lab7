package me.lab7.server;


import me.lab7.common.utilities.RunMode;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.CommandManager;
import me.lab7.server.utility.FileHanding;
import me.lab7.server.utility.LabAsk;

import java.io.IOException;
import java.util.Scanner;


public class Server {

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        String envVariable = System.getenv("LAB");
        CollectionManager collectionManager = new CollectionManager();
        RunMode runMode = new RunMode();
        FileHanding fileHanding = new FileHanding(collectionManager, envVariable, runMode);
        CommandManager commandManager = new CommandManager(collectionManager, fileHanding, new LabAsk(scanner));
        fileHanding.setCommandManager(commandManager);
        fileHanding.xmlFileReader();
        ServerInstance serverInstance = new ServerInstance(10643, commandManager,collectionManager, scanner, args[0], args[1]);
        Runtime.getRuntime().addShutdownHook(new Thread(commandManager::save));
        serverInstance.run();
    }
}