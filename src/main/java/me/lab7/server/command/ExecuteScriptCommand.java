package me.lab7.server.command;

import me.lab7.common.Response;
import me.lab7.server.utility.FileHanding;

/**
 * command reading and executing the script
 *
 * @author Nikita and Vlad
 * @version 0.1
 */
public class ExecuteScriptCommand extends AbstractCommand {
    private FileHanding fileHanding;

    public ExecuteScriptCommand(FileHanding fileHanding) {
        super("execute_script file_name", "считывает и исполняет скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
        this.fileHanding = fileHanding;
    }

    @Override
    public Response execute(String argument) {
        return null;
    }
}