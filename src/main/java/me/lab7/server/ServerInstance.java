package me.lab7.server;

import me.lab7.common.*;
import me.lab7.common.data.LabWork;
import me.lab7.server.manager.CollectionManager;
import me.lab7.server.manager.CommandManager;
import me.lab7.server.manager.SqlCollectionManager;
import me.lab7.server.manager.SqlUserManager;
import me.lab7.server.utility.LabAsk;
import org.postgresql.util.PSQLException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ServerInstance {
    private final Logger logger;
    private static final int TIMEOUTWRITE = 100;
    private static final int SOCKET_TIMEOUT = 10;
    private final ExecutorService selectorCommand = Executors.newCachedThreadPool();
    private final int port;
    private CommandManager commandManager;
    private final Scanner scanner;
    private SqlCollectionManager sqlCollectionManager;
    private SqlUserManager sqlUserManager;
    private final CollectionManager collectionManager;

    public ServerInstance(int port, CollectionManager collectionManager, Scanner scanner, String dbUser, String dbPassword) {
        this.port = port;
        this.collectionManager = collectionManager;
        this.scanner = scanner;
        this.logger = Logger.getLogger("log");
        File lf = new File("server.log");
        FileHandler fh;
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/studs", dbUser, dbPassword);
            this.sqlCollectionManager = new SqlCollectionManager(connection, logger);
            this.sqlUserManager = new SqlUserManager(connection, logger);
            fh = new FileHandler(lf.getAbsolutePath(), true);
            logger.addHandler(fh);
            this.commandManager = new CommandManager(collectionManager, new LabAsk(scanner), sqlCollectionManager);
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
                    Socket newClient = serv.accept();
                    newClient.setSoTimeout(SOCKET_TIMEOUT);
                    handleRequests(new MessageHandler(newClient));
                } catch (SocketTimeoutException e) {
                    if (check++ >= TIMEOUTWRITE) {
                        check = 0;
                    }
                }
            }
        } catch (BindException e) {
            System.out.println("Порт занят");
        }
    }

    private void start() {
        try {
            this.sqlUserManager.initUserTable();
            this.sqlCollectionManager.initTableOrExecuteLabWorks();
        } catch (SQLException e) {
            logger.severe("не удалось прочитать базу данных" + e);
        }
        collectionManager.initializeData(sqlCollectionManager.getCollection());
    }

    public void handleRequests(MessageHandler client1) {
        new Thread(() -> {
            Client client = new Client(client1);
            client.handleRequests();
        }).start();
    }

    private boolean acceptConsoleInput() throws IOException {
        if (System.in.available() > 0) {
            String command = scanner.nextLine().trim();
            if (command.equals("exit")) {
                logger.info("завершение работы");
                commandManager.exit();
            }
            System.out.println("Команда не распознана, сервер распознаёт только команду 'exit'");
        }
        return false;
    }

    private class Client {
        private final MessageHandler client;
        private boolean running;

        Client(MessageHandler socket) {
            this.client = socket;
            running = true;
        }

        void stop() {
            running = false;
            logger.info("Client  " + client.getSocket().getRemoteSocketAddress() + " has been disconnected");
            try {
                client.getSocket().close();
            } catch (IOException e) {
                logger.severe("Failed to close connection with client " + client.getSocket().getRemoteSocketAddress() + e);
            }
        }

        public void handleRequests() {
            while (running) {
                try {
                    if (client.checkForMessage()) {
                        Object message = client.getMessage();
                        if (message instanceof Request) {
                            logger.info("началась обработка команды");
                            Response response = selectorCommand.submit(() -> {
                                return selectCommand(((Request) message).getCommands(), ((Request) message).getClient());
                            }).get();
                            logger.info("закончилась обработка команды обработка команды");
                            sendResponse(response);
                        } else if (message instanceof RequestWithLabWork) {
                            logger.info("добавление лабораторной работы");
                            Response response = selectorCommand.submit(() -> {
                                return selectCommand(((RequestWithLabWork) message).getCommands(), ((RequestWithLabWork) message).getLabWork(), ((RequestWithLabWork) message).getClient());
                            }).get();
                            logger.info(response.getResponse());
                            sendResponse(response);
                        } else if (message instanceof RequestWithCommands) {
                            logger.info("началась обработка скрипта");
                            Response response = selectorCommand.submit(() -> {
                                return selectWithCommands(((RequestWithCommands) message).getName(), ((RequestWithCommands) message).getCommands(), ((RequestWithCommands) message).getClient());
                            }).get();
                            logger.info("началась обработка скрипта");
                            sendResponse(response);
                        } else if (message instanceof RequestWithClient) {
                            logger.info("подключение нового клиента");
                            ResponseWithBooleanType responseWithBooleanType = selectorCommand.submit(() -> {
                                return selectAuthCommand(((RequestWithClient) message).getCommand(), ((RequestWithClient) message).getClient());
                            }).get();
                            sendResponse(responseWithBooleanType);
                        }
                        client.clearBuffer();
                    }
                } catch (IOException | InterruptedException | ExecutionException e) {
                    stop();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        private void sendResponse(Object response) {
            new Thread(() -> {
                try {
                    client.sendResponse(response);
                    logger.info("ответ клиенту отправлен");
                } catch (IOException e) {
                    logger.info("ответ клиенту не удалось отправить");
                    stop();
                }
            }).start();
        }

        private ResponseWithBooleanType selectAuthCommand(String command, Authentication client) {
            if (command.equals("reg")) {
                try {
                    return new ResponseWithBooleanType(sqlUserManager.registration(client), true);
                } catch (PSQLException e) {
                    logger.info(client.getUserName() + e);
                    return new ResponseWithBooleanType("Пользователь с таким именем уже существует", false);
                } catch (SQLException e) {
                    logger.info("sql недоступна " + e);
                    return new ResponseWithBooleanType("не удалось получить данные с сервера", false);
                }
            }
            if (command.equals("login")) {
                try {
                    if (sqlUserManager.login(client) != null)
                        return new ResponseWithBooleanType("пользователь авторизован", true);
                } catch (PSQLException e) {
                    logger.info(client.getUserName() + e);
                    return new ResponseWithBooleanType("Неверно веден логин или пароль", false);
                } catch (SQLException e) {
                    logger.info("sql недоступна " + e);
                    return new ResponseWithBooleanType("на сервере произошла неизвестная ошибка попытайтесь подключиться позже", false);
                }
            }
            return new ResponseWithBooleanType("пользователь не авторизован", false);
        }

        public Response selectCommand(String[] command, Authentication client) {
            try {
                return commandManager.commandSelection(command, sqlUserManager.login(client));
            } catch (SQLException e) {
                return new Response("не удалось выполнить команду" + command[0] + "пользователь не обнаружен");
            }
        }

        public Response selectCommand(String command, LabWork labWork, Authentication client) {
            try {
                return commandManager.commandSelection(command, labWork, sqlUserManager.login(client));
            } catch (SQLException e) {
                return new Response("не удалось выполнить команду" + command + "пользователь не обнаружен");
            }
        }

        public Response selectWithCommands(String command, ArrayList<String> commands, Authentication client) {
            try {
                return commandManager.commandSelectionFromScript(command, commands, sqlUserManager.login(client));
            } catch (SQLException e) {
                return new Response("не удалось выполнить команду" + command + "пользователь не обнаружен");
            }
        }

    }
}