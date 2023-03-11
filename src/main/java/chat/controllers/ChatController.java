package chat.controllers;

import chat.background.Network;
import command.CommandType;
import command.commands.IncomingMessageCommandData;
import command.commands.UpdateUserListCommandData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.logging.log4j.Level;
import server.model.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatController {
    private static final int LAST_STRINGS = 100;

    private String login;

    @FXML
    public Label usernameLabel;
    @FXML
    public ListView userList;
    @FXML
    public TextField messageTextArea;
    @FXML
    public Button sendMessageButton;
    @FXML
    public TextArea chatTextArea;

    public void launchMessageHandler() {
        Network.getInstance().addReadMessageListener(command -> {
            if (command.getType() == CommandType.INCOMING_MESSAGE) {
                IncomingMessageCommandData data = (IncomingMessageCommandData) command.getData();

                updateHistory(data.getSender(), data.getMessage());
            } else if (command.getType() == CommandType.UPDATE_USERS_LIST) {
                Platform.runLater(() -> {
                    userList.setItems(FXCollections.observableArrayList(
                            ((UpdateUserListCommandData) command.getData()).getUsers()
                    ));
                });
            }
        });
    }

    public void initializeHistory(String login) {
        this.login = login;

        File file = new File("History/" + login);
        try {
            if (file.createNewFile()) {
                return;
            }
        } catch (Exception e) {
            Server.logger.log(Level.ERROR, "Ошибка при создании файла истории");
        }

        try {
            List<String> history = new ArrayList<>();

            ReversedLinesFileReader reader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8);
            for (int i = 0; i < LAST_STRINGS; i++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                history.add(line);
            }

            for (int i = history.size() - 1; i >= 0; i--) {
                chatTextArea.appendText(history.get(i) + System.lineSeparator());
            }
        } catch (Exception e) {
            Server.logger.log(Level.ERROR, "Ошибка при открытии файла истории");
        }
    }

    public void sendMessage() {
        String message = messageTextArea.getText();
        if (message.isEmpty()) {
            messageTextArea.clear();
            return;
        }

        try {
            Object recipient = userList.getSelectionModel().getSelectedItem();
            if (recipient != null) {
                String recipientName = recipient.toString();
                if (recipientName.equals("Чат")) {
                    Network.getInstance().sendPublicMessage(message);
                } else {
                    Network.getInstance().sendPrivateMessage(recipientName, message);
                }

                updateHistory("Я", message);

                Platform.runLater(() -> messageTextArea.requestFocus());
                messageTextArea.clear();
            }
        } catch (IOException e) {
            Server.logger.log(Level.ERROR, "Ошибка сети: не удалось отправить сообщение");
        }
    }

    private void updateHistory(String sender, String message) {
        String record = DateFormat.getInstance().format(new Date()) +
                System.lineSeparator() +
                sender + ":" +
                System.lineSeparator() +
                message +
                System.lineSeparator() + System.lineSeparator();

        chatTextArea.appendText(record);

        try {
            OutputStream outputStream = new FileOutputStream("History/" + login, true);
            outputStream.write(record.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (Exception e) {
            Server.logger.log(Level.ERROR, "Ошибка при открытии файла истории");
        }
    }

    public Label getUsernameLabel() {
        return usernameLabel;
    }
}