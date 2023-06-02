package me.lab7.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

public final class Client {
    private Client() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Неверное количество аргументов, введите хост и порт");
            return;
        }
        try {
            int port = Integer.parseInt(args[1]);
            String host;
            if (args[0].contains("/")) host = args[0].split("/")[1];
            else host = args[0];
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getLocalHost(), port);
            Scanner scanner = new Scanner(System.in);
            LabAsk labAsk = new LabAsk(scanner);
            ScriptReader scriptReader = new ScriptReader();
            Console console = new Console(scanner, addr, labAsk, scriptReader);
            ValidationChecker validationChecker = new ValidationChecker();
            console.run();


        } catch (NumberFormatException e) {
            System.out.println("Некорректный порт");
        } catch (IOException e) {
            System.out.println("Не удалось запустить приложение");
        }
    }
}