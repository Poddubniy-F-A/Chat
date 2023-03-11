package chat.controllers;

import chat.ChatLauncher;

import chat.background.Network;
import command.CommandType;
import command.commands.AuthOkCommandData;
import command.commands.ErrorCommandData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.Level;
import server.model.Server;

import java.io.IOException;

import static javafx.scene.control.Alert.AlertType.ERROR;

public class AuthController {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nickField;
    @FXML
    public Button authEnterButton;
    public ReadMessageListener readMessageListener;

    @FXML
    public void executeAuth() {
        execute(false);
    }

    @FXML
    public void executeReg() {
        execute(true);
    }

    public void execute(Boolean isReg) {
        if (!isConnectedToServer()) {
            Server.logger.log(Level.ERROR, "Не удалось установить соединение с сервером");
            return;
        }

        String login = loginField.getText();
        String password = passwordField.getText();
        if (login.isBlank() || password.isBlank()) {
            ChatLauncher.getInstance().makeErrorDialog(ERROR,
                    "Ошибка аутентификации",
                    "Логин и пароль должны быть указаны"
            );
            return;
        }

        try {
            Network.getInstance().sendAuthMessage(login, password, nickField.getText(), isReg);
        } catch (IOException e) {
            Server.logger.log(Level.ERROR, "Не удалось отправить сообщение");
        }
    }

    public void initializeMessageHandler() {
        readMessageListener = getNetwork().addReadMessageListener(command -> {
            if (command.getType() == CommandType.AUTH_OK) {
                Platform.runLater(() -> ChatLauncher.getInstance().switchToChatWindow(
                        ((AuthOkCommandData) command.getData()).getUserName(), loginField.getText()));
            } else if (command.getType() == CommandType.ERROR) {
                Platform.runLater(() -> {
                    ErrorCommandData data = (ErrorCommandData) command.getData();
                    ChatLauncher.getInstance().makeErrorDialog(data.getDialogType(),
                            data.getTitle(),
                            data.getMessage()
                    );
                });
            }
        });
    }

    public boolean isConnectedToServer() {
        Network network = getNetwork();
        return network.isConnected() || network.connect();
    }

    public void close() {
        getNetwork().removeReadMessageListener(readMessageListener);
    }

    private Network getNetwork() {
        return Network.getInstance();
    }
}