package command;

import command.commands.*;
import javafx.scene.control.Alert;

import java.io.Serializable;
import java.util.List;

public class Command implements Serializable {
    private CommandType type;
    private Serializable data;

    public static Command authCommand(String login, String password, String nick, Boolean isReg) {
        Command command = new Command();
        if (isReg) {
            command.type = CommandType.REG;
        } else {
            command.type = CommandType.ENTER;
        }
        command.data = new AuthCommandData(login, password, nick);

        return command;
    }

    public static Command authOkCommand(String userName) {
        Command command = new Command();
        command.type = CommandType.AUTH_OK;
        command.data = new AuthOkCommandData(userName);

        return command;
    }

    public static Command errorCommand(Alert.AlertType dialogType, String title, String message) {
        Command command = new Command();
        command.type = CommandType.ERROR;
        command.data = new ErrorCommandData(dialogType, title, message);

        return command;
    }

    public static Command publicMessageCommand(String message) {
        Command command = new Command();
        command.type = CommandType.PUBLIC_MESSAGE;
        command.data = new PublicMessageCommandData(message);

        return command;
    }

    public static Command privateMessageCommand(String recipient, String message) {
        Command command = new Command();
        command.type = CommandType.PRIVATE_MESSAGE;
        command.data = new PrivateMessageCommandData(recipient, message);

        return command;
    }

    public static Command incomingMessageCommand(String sender, String message) {
        Command command = new Command();
        command.type = CommandType.INCOMING_MESSAGE;
        command.data = new IncomingMessageCommandData(sender, message);

        return command;
    }

    public static Command updateUserListCommand(List<String> users) {
        Command command = new Command();
        command.type = CommandType.UPDATE_USERS_LIST;
        command.data = new UpdateUserListCommandData(users);

        return command;
    }

    public CommandType getType() {
        return type;
    }

    public Serializable getData() {
        return data;
    }
}