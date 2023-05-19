package me.lab7.server;

import me.lab7.client.Authentication;
import me.lab7.common.*;
import me.lab7.common.data.LabWork;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.CommandManager;
import me.lab7.server.manager.SqlManager;
import me.lab7.server.manager.SqlUserManager;
import org.postgresql.util.PSQLException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ServerInstance {
    private final Logger logger;
    private static final int TIMEOUTWRITE = 100;
    private static final int SOCKET_TIMEOUT = 10;
    private final HashSet<MessageHandler> clients;

    private final int port;
    private CommandManager commandManager;
    private final Scanner scanner;
    private SqlManager sqlManager;
    private SqlUserManager sqlUserManager;
    private Connection connection;
    private CollectionManager collectionManager;

    public ServerInstance(int port, CommandManager commandManager, CollectionManager collectionManager, Scanner scanner, String dbUser, String dbPassword) {
        this.port = port;
        this.commandManager = commandManager;
        this.collectionManager = collectionManager;
        this.scanner = scanner;
        clients = new HashSet<>();
        this.logger = Logger.getLogger("log");
        File lf = new File("server.log");
        FileHandler fh = null;
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/studs", "s368738", "XH9wjv2skRYOzgjX");
            this.sqlManager = new SqlManager(connection, logger);
            this.sqlUserManager = new SqlUserManager(connection, logger);
            fh = new FileHandler(lf.getAbsolutePath(), true);
            logger.addHandler(fh);
        } catch (IOException e) {
            System.out.println(e.getMessage() + "логер не записывает данные в файл");
        } catch (SQLException e) {
            System.out.println("ошибка подключения к базе данных");
            System.exit(1);
        }
    }

    public void run() throws IOException {
        int check = 0;
        try (ServerSocket serv = new ServerSocket(port)) {
            serv.setSoTimeout(SOCKET_TIMEOUT);
            start();
            System.out.println("Сервер начал работать хост=" + InetAddress.getLocalHost() + " порт=" + port);
            while (true) {
                if (acceptConsoleInput()) {
                    return;
                }
                try {
                    while (true) {
                        Socket newClient = serv.accept();
                        newClient.setSoTimeout(SOCKET_TIMEOUT);
                        clients.add(new MessageHandler(newClient));
                    }
                } catch (SocketTimeoutException e) {
                    if (check++ >= TIMEOUTWRITE) {
                        check = 0;
                    }
                }
                handleRequests();
            }
        } catch (BindException e) {
            System.out.println("Порт занят");
        }
    }

    private void start() {
        try {
            this.sqlUserManager.initUserTable();
            this.sqlManager.initTableOrExecuteLabWorks();
        } catch (SQLException e) {
            logger.severe("не удалось прочитать базу данных" + e);
        }
        collectionManager.initializeData(sqlManager.getCollection());
    }

    public void handleRequests() throws IOException {
        Iterator<MessageHandler> it = clients.iterator();
        while (it.hasNext()) {
            MessageHandler client = it.next();
            try {
                if (client.checkForMessage()) {
//                if (checkForMessage(client)){
                    Object message = client.getMessage();
                    if (message instanceof Request) {
                        logger.info("началась обработка команды");
                        Response response = selectCommand(((Request) message).getCommands());
                        logger.info("закончилась обработка команды обработка команды");
                        client.sendMessage(response);
                        logger.info("ответ клиенту отправлен");
                    } else if (message instanceof RequestWithLabWork) {
                        logger.info("добавление лабораторной работы");
                        Response response = selectCommand(((RequestWithLabWork) message).getCommands(), ((RequestWithLabWork) message).getLabWork());
                        logger.info("лабораторная работа добавлена");
                        client.sendMessage(response);
                        logger.info("ответ клиенту отправлен");
                    } else if (message instanceof RequestWithCommands) {
                        logger.info("началась обработка скрипта");
                        Response response = selectWithCommands(((RequestWithCommands) message).getName(), ((RequestWithCommands) message).getCommands());
                        logger.info("началась обработка скрипта");
                        client.sendMessage(response);
                        logger.info("ответ клиенту отправлен");
                    } else if (message instanceof RequestWithClient) {
                        logger.info("подключение нового клиента");
                        var responseWithBooleanType = selectAuthCommand(((RequestWithClient) message).getCommand(), ((RequestWithClient) message).getClient());
                        client.sendMessage(responseWithBooleanType);
                    }
                    client.clearBuffer();
                }
            } catch (IOException e) {
                client.getSocket().close();
                it.remove();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
//    private void checkForMessage(MessageHandler client)throws IOException{
//        boolean a =  new Thread(()->{
//            try {
//                boolean  b = client.checkForMessage();
//            } catch (IOException e) {
//                System.out.println();
//            }
//        }).start();
//    }

    private ResponseWithBooleanType selectAuthCommand(String command, Authentication client) {
        if (command.equals("reg")) {
            try {
                return new ResponseWithBooleanType(sqlUserManager.registration(client), true);
            } catch (PSQLException e) {
                logger.info(client.getUserName() + e);
                return new ResponseWithBooleanType("Пользователь с таким именем уже существует", false);
            } catch (SQLException e) {
                logger.info(client.getUserName() + e);
                return new ResponseWithBooleanType("не удалось получить данные с сервера", false);
            }
        }
        if (command.equals("login")) {
            try {
                if (sqlUserManager.login(client) != null)
                    return new ResponseWithBooleanType("пользователь авторизован", true);
            } catch (PSQLException e) {
                logger.info(client.getUserName() + e);
                return new ResponseWithBooleanType("Пользователь с таким именем уже существует", false);
            } catch (SQLException e) {
                logger.info(client.getUserName() + e);
                return new ResponseWithBooleanType("не удалось получить данные с сервера", false);
            }
        }
        return null;
    }

    private boolean acceptConsoleInput() throws IOException {
        if (System.in.available() > 0) {
            String command = scanner.nextLine().trim();
            switch (command) {
                case "save":
                    logger.info("сохранение коллекции");
                    commandManager.save();
                    break;
                case "exit":
                    logger.info("завершение работы");
                    commandManager.exit();
                default:
                    System.out.println("Команда не распознана, сервер распознаёт только команду 'save', 'exit'");
            }
        }
        return false;
    }

    public Response selectCommand(String[] command) {
        return commandManager.commandSelection(command);
    }

    public Response selectCommand(String command, LabWork labWork) {
        return commandManager.commandSelection(command, labWork);
    }

    public Response selectWithCommands(String command, ArrayList<String> commands) {
        return commandManager.commandSelectionFromScript(command, commands);
    }
}
