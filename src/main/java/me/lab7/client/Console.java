package me.lab7.client;


import me.lab7.common.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * console input processing class
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class Console {
    private Sender sender;
    private static final int TIMEOUT = 5;
    private static final int TIMEOUTMS = 100;
    private static final int MILLIS_IN_SECONDS = 1000;
    private String[] lastCommand;

    private Scanner scanner;
    private InetSocketAddress address;
    private LabAsk labAsk;
    private ScriptReader scriptReader;

    public Console(Scanner scanner, InetSocketAddress address, LabAsk labAsk, ScriptReader scriptReader) {
        this.scanner = scanner;
        this.address = address;
        this.labAsk = labAsk;
        this.sender = new Sender();
        this.scriptReader = scriptReader;
    }

    public void consoleReader() {
        if (lastCommand != null) selectCommand(lastCommand);
        String[] command;
        while (true) {
            command = (scanner.nextLine().trim() + " ").split("\s", 2);
            if (!command[0].equals("")) {
                command[1] = command[1].trim();
                lastCommand = command;
                selectCommand(command);
            }
        }
    }

    public void selectCommand(String[] command) {
        try {
            if (command[0].equals("add")) {
                sender.sendMessageWithLabWork(labAsk.addLabWork());
                hookResponse();
            } else if (command[0].equals("execute_script")) {
                scriptReader.scriptReader(command[1]);
                if (!scriptReader.getCommands().isEmpty()) {
                    sender.sendMessageWithCommands(scriptReader.getCommands());
                    scriptReader.clearCommands();
                    hookResponse();
                }
            } else if (command[0].equals("exit")) {
                System.out.println("Завершение работы");
                System.exit(0);
            } else if (ValidationChecker.checkValidation(command)) {
                sender.sendMessage(command);
                hookResponse();
            }
            lastCommand = null;
        } catch (IOException e) {
            System.out.println("Во время попытки отправить запрос произошла ошибка");
            waitForReconnection();
        }
    }

    private void hookResponse() throws IOException {
        Response response = waitForResponse();
        if (response != null) {
            System.out.println(response.getResponse());
            sender.clearInBuffer();
        } else System.out.println("С сервера ничего не пришло");
    }

    private Response waitForResponse() throws IOException {
        int seconds = 0;
        long start = System.currentTimeMillis();
        while (seconds < TIMEOUT) {
            if (sender.checkForMessage()) {
                Object received = sender.getPayload();
                if (received instanceof Response) {
                    return (Response) received;
                } else {
                    System.out.println("Получен неверный ответ с сервера");
                    break;
                }
            }
            if (System.currentTimeMillis() >= start + (long) (seconds + 1) * MILLIS_IN_SECONDS) {
                seconds++;
            }
        }
        System.out.println("Время ожидания превысило " + TIMEOUT + " секунд.");
        return null;
    }


    private void waitForReconnection() {
        System.out.println("Попытка переподключения");
        try {
            run();
        } catch (IOException e) {
            System.out.println("Сервер не отвечает, как только вы введете что-нибудь, будет осуществлена повторная попытка подключиться к серверу");
        }
    }


    public void run() throws IOException {
        try (SocketChannel socket = SocketChannel.open()) {
            if (!connection(socket)) {
                System.out.println("сервер не отвечает");
                return;
            }
            socket.configureBlocking(false);
            sender.setSocket(socket);
            consoleReader();
        }
    }

    private boolean connection(SocketChannel socket) {
        int second = 0;
        long start = System.currentTimeMillis();
        while (second < TIMEOUT) {
            try {
                socket.socket().connect(address, TIMEOUTMS);
                return true;
            } catch (IOException | IllegalBlockingModeException e) {

                if (System.currentTimeMillis() >= start + (long) (second + 1) * MILLIS_IN_SECONDS) {
                    System.out.print(".");
                    second++;
                }
            }
        }
        System.out.println("Время подключение вышло");
        return false;
    }


//    public void setCommandManager(CommandManager commandManager) {
//        this.commandManager = commandManager;
//    }


}
