package me.lab7.server;

import me.lab7.common.Authentication;
import me.lab7.common.Response;
import me.lab7.common.ResponseWithBooleanType;
import me.lab7.common.ResponseWithLabWork;
import me.lab7.common.data.LabWork;
import me.lab7.server.manager.CommandManager;
import me.lab7.server.manager.SqlUserManager;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SelectorResponse {

    private final Logger logger;
    private final SqlUserManager sqlUserManager;
    private final CommandManager commandManager;

    public SelectorResponse(SqlUserManager sqlUserManager, Logger logger, CommandManager commandManager) {
        this.logger = logger;
        this.sqlUserManager = sqlUserManager;
        this.commandManager = commandManager;
    }

    public ResponseWithBooleanType selectAuthCommand(String command, Authentication client) {
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
            return new Response("не удалось выполнить команду " + command[0] + " нехватка прав или лабораторная работа не обнаружена");
        }
    }

    public Response selectCommand(String command, LabWork labWork, Authentication client) {
        try {
            return commandManager.commandSelection(command, labWork, sqlUserManager.login(client));
        } catch (SQLException e) {
            return new Response("не удалось выполнить команду " + command + " нехватка прав или лабораторная работа не обнаружена");
        }
    }

    public Response selectWithCommands(String command, ArrayList<String> commands, Authentication client) {
        try {
            return commandManager.commandSelectionFromScript(command, commands, sqlUserManager.login(client));
        } catch (SQLException e) {
            return new Response("не удалось выполнить команду " + command + " нехватка прав или лабораторная работа не обнаружена");
        }
    }

    public ResponseWithLabWork update(String[] commands, Authentication client) {
        try {
            return commandManager.updateCommand(commands, sqlUserManager.login(client));
        } catch (SQLException e) {
            return new ResponseWithLabWork("не удалось выполнить команду update", null);
        }
    }
}
