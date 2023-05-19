package me.lab7.server;


import me.lab7.common.utilities.RunMode;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.CommandManager;
import me.lab7.server.utility.LabAsk;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;


public class Server {

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        CollectionManager collectionManager = new CollectionManager();
        CommandManager commandManager = new CommandManager(collectionManager, new LabAsk(scanner));
        ServerInstance serverInstance = new ServerInstance(10643, commandManager, collectionManager, scanner, args[0], args[1]);
        Runtime.getRuntime().addShutdownHook(new Thread(commandManager::save));
        serverInstance.run();


//        FileHanding fileHanding = new FileHanding(collectionManager, envVariable, runMode);

//        fileHanding.setCommandManager(commandManager);
//        fileHanding.xmlFileReader();
    }
}