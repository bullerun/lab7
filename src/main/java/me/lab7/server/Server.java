package me.lab7.server;

import me.lab7.server.manager.CollectionManager;

import java.io.IOException;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Нужно ввести название таблицы и пароль");
        }
        Scanner scanner = new Scanner(System.in);
        CollectionManager collectionManager = new CollectionManager();
        ServerInstance serverInstance = new ServerInstance(10642, collectionManager, scanner, args[0], args[1]);
        serverInstance.run();
    }
}
