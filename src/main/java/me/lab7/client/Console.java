package me.lab7.client;


import me.lab7.common.Authentication;
import me.lab7.common.Response;
import me.lab7.common.ResponseWithBooleanType;
import me.lab7.common.ResponseWithLabWork;
import me.lab7.common.data.LabWork;
import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.exception.RangeException;

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
    private final Sender sender;
    private static final int TIMEOUT = 10;
    private static final int TIMEOUTMS = 100;
    private static final int MILLIS_IN_SECONDS = 1000;
    private String[] lastCommand;
    private Authentication client;
    private final Scanner scanner;
    private final InetSocketAddress address;
    private final LabAsk labAsk;
    private final ScriptReader scriptReader;

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
                command = (scanner.nextLine().trim() + " ").split(" ", 2);
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
                sender.sendMessageWithLabWork("add", labAsk.addLabWork(), client);
                hookResponse();
            } else if (command[0].equals("update")) {
                if (ValidationChecker.checkValidation(command)) {
                    sender.sendMessageWithUpdate(command, client);
                    hookUpdateResponse();
                }
            } else if (command[0].equals("execute_script")) {
                scriptReader.scriptReader(command[1]);
                if (!scriptReader.getCommands().isEmpty()) {
                    sender.sendMessageWithCommands(scriptReader.getCommands(), client);
                    scriptReader.clearCommands();
                    hookResponse();
                }
            } else if (command[0].equals("exit")) {
                System.out.println("Завершение работы");
                System.exit(0);
            } else if (ValidationChecker.checkValidation(command)) {
                sender.sendMessage(command, client);
                hookResponse();
            }
            lastCommand = null;
        } catch (IOException e) {
            System.out.println("Во время попытки отправить запрос произошла ошибка");
            waitForReconnection();
        }
    }

    private void hookUpdateResponse() throws IOException {
        ResponseWithLabWork response = (ResponseWithLabWork) waitForResponse();
        if (response != null && response.getLabWork() != null) {
            System.out.println(response.getResponse());
            sender.clearInBuffer();
            update(response.getLabWork());
        } else {
            System.out.println("Такой лабораторной работы нет или у вас нет прав изменять её");
        }
    }

    private void update(LabWork labWork) throws IOException {
        labAsk.setLabWork(labWork);
        if (labAsk.updateById("Хотите изменить имя?")) labAsk.nameAsk();
        if (labAsk.updateById("Хотите изменить координаты?")) labAsk.coordinatesAsk();
        if (labAsk.updateById("Хотите изменить минимальный балл?")) labAsk.minimalPointAsk();
        if (labAsk.updateById("Хотите изменить сложность?")) labAsk.difficultyAsk();
        if (labAsk.updateById("Хотите изменить дисциплину?")) labAsk.disciplineAsk();
        System.out.println(labAsk.getLabWork());
        sender.sendMessageWithLabWork("update", labAsk.getLabWork(), client);
        hookResponse();
    }

    private void hookResponse() throws IOException {
        Response response = (Response) waitForResponse();
        if (response != null) {
            System.out.println(response.getResponse());
            sender.clearInBuffer();
        }
    }

    private Object waitForResponse() throws IOException {
        int seconds = 0;
        long start = System.currentTimeMillis();
        while (seconds < TIMEOUT) {
            if (sender.checkForMessage()) {
                Object received = sender.getPayload();
                if (received instanceof Response) {
                    return received;
                } else if (received instanceof ResponseWithBooleanType) {
                    return received;
                } else if (received instanceof ResponseWithLabWork) {
                    return received;
                }
            }
            if (System.currentTimeMillis() >= start + (long) (seconds + 1) * MILLIS_IN_SECONDS) {
                seconds++;
            }
        }
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


    private void authentication() throws IOException {
        var flag = true;
        while (flag) {
            System.out.println("Если вы новый пользователь зарегистрируйтесь написав команду reg, если вы уже зарегистрированы войдите в аккаунт написав команду login");
            client = new Authentication();
            var command = "";
            var checkCommand = true;
            while (checkCommand) {
                command = scanner.nextLine().trim();
                if (command.equals("reg")) checkCommand = false;
                if (command.equals("login")) checkCommand = false;
                if (checkCommand)
                    System.out.println("неверно введённая команда введите reg, если вы хотите зарегистрироваться или login, если вы уже зарегистрированы ");
            }
            while (true) {
                try {
                    System.out.println("Введите логин");
                    client.setUserName(scanner.nextLine().trim());
                    break;
                } catch (RangeException e) {
                    System.out.println("имя пользователя должно быть меньше 127 символов");
                } catch (MustBeNotEmptyException e) {
                    System.out.println("имя пользователя не должно быть пустым");
                }
            }
            while (true) {
                try {
                    System.out.println("Введите пароль, он должен состоять из 8 символов");
                    client.setPassword(scanner.nextLine().trim());
                    break;
                } catch (RangeException e) {
                    System.out.println("Пароль должен быть равен 8 символам");
                } catch (MustBeNotEmptyException e) {
                    System.out.println("Пароль не должен быть пустым");
                }
            }
            try {
                sender.sendAuth(command, client);
                flag = !hookResponseWithBooleanType();
                if (flag) {
                    System.out.println("Пожалуйста проведите авторизацию заново");
                }
            } catch (IOException e) {
                System.out.println("Во время отправки запроса произошла ошибка");
                run();
            }

        }
    }

    private boolean hookResponseWithBooleanType() throws IOException {
        ResponseWithBooleanType responseWithBooleanType = (ResponseWithBooleanType) waitForResponse();
        if (responseWithBooleanType != null) {
            System.out.println(responseWithBooleanType.getResponse());
            sender.clearInBuffer();
            return responseWithBooleanType.getAuth();
        } else System.out.println("С сервера ничего не пришло");
        return false;
    }

    public void run() throws IOException {
        try (SocketChannel socket = SocketChannel.open()) {
            if (!connection(socket)) {
                System.out.println("сервер не отвечает");
                return;
            }
            socket.configureBlocking(false);
            sender.setSocket(socket);
            authentication();
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
}
