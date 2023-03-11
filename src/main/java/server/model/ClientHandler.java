package server.model;

import command.Command;
import command.CommandType;
import command.commands.AuthCommandData;
import command.commands.PrivateMessageCommandData;
import command.commands.PublicMessageCommandData;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Arrays;

import static command.CommandType.PRIVATE_MESSAGE;
import static command.CommandType.PUBLIC_MESSAGE;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class ClientHandler {
    private final Server server;
    private final Socket clientSocket;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private static Connection connection;
    private static Statement statement;

    private String userName;
    private String[] LPN = null;

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        Server.executorService.execute(() -> {
            try {
                authenticate();
                readMessages();
            } catch (IOException e) {
                Server.logger.log(Level.WARN, "Не удалось обработать сообщение от {}", userName);
            } finally {
                try {
                    outputStream.close();
                    inputStream.close();
                    server.unsubscribe(ClientHandler.this);
                    clientSocket.close();
                } catch (IOException e) {
                    Server.logger.log(Level.ERROR, "Не удалось закрыть соединение");
                }
            }
        });
    }

    private void authenticate() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            if (command.getType() == CommandType.REG) {
                try {
                    AuthCommandData data = (AuthCommandData) command.getData();
                    String login = data.getLogin();
                    String password = data.getPassword();
                    String nick = data.getNick();

                    openSQLConnection();
                    if (statement.executeQuery(String.format(
                            "SELECT Nickname FROM LogPass WHERE Login = '%s';",
                            login
                    )).getString(1) != null) {
                        closeSQLConnection();
                        sendCommand(Command.errorCommand(ERROR,
                                "Ошибка аутентификации",
                                "Пользователь с таким логином уже существует"
                        ));
                    } else if (!Arrays.equals(LPN, new String[]{login, password, nick})
                            && nick.isBlank()) {
                        closeSQLConnection();
                        sendCommand(Command.errorCommand(WARNING,
                                "Пустой ник",
                                "Будет установлен ник по умолчанию"
                        ));
                        LPN = new String[]{login, password, nick};
                    } else {
                        if (nick.isBlank()) {
                            userName = login;
                        } else {
                            userName = nick;
                        }
                        statement.execute(String.format(
                                "INSERT INTO LogPass (Nickname, Login, Password) VALUES ('%s', '%s', '%s');",
                                userName, login, password
                        ));
                        closeSQLConnection();

                        finishAuthenticate();
                        return;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (command.getType() == CommandType.ENTER) {
                try {
                    AuthCommandData data = (AuthCommandData) command.getData();
                    String login = data.getLogin();
                    String password = data.getPassword();
                    String nick = data.getNick();

                    openSQLConnection();
                    ResultSet resultSet = statement.executeQuery(String.format(
                            "SELECT Password, Nickname FROM LogPass WHERE Login = '%s';",
                            login
                    ));
                    if (!password.equals(resultSet.getString(1))) {
                        closeSQLConnection();
                        sendCommand(Command.errorCommand(ERROR,
                                "Ошибка аутентификации",
                                "Неправильный логин или пароль"
                        ));
                    } else if (!Arrays.equals(LPN, new String[]{login, password, nick})
                            && !nick.isBlank()) {
                        closeSQLConnection();
                        sendCommand(Command.errorCommand(WARNING,
                                "Смена ника",
                                "Будет установлен новый ник"
                        ));
                        LPN = new String[]{login, password, nick};
                    } else {
                        if (nick.isBlank()) {
                            userName = resultSet.getString(2);
                        } else {
                            statement.execute(String.format(
                                    "UPDATE LogPass SET Nickname = '%s' WHERE Login = '%s';",
                                    nick, login
                            ));
                            userName = nick;
                        }
                        closeSQLConnection();

                        if (server.isUserNameBusy(userName)) {
                            sendCommand(Command.errorCommand(ERROR,
                                    "Ошибка аутентификации",
                                    "Окно этого пользователя уже существует"
                            ));
                        } else {
                            finishAuthenticate();
                            return;
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            Server.logger.log(Level.ERROR, "Не удалось определить класс команды");
        }

        return command;
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    private void openSQLConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:LP.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeSQLConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void finishAuthenticate() throws IOException {
        sendCommand(Command.authOkCommand(userName));
        server.subscribe(this);
        Server.logger.log(Level.INFO,
                "Подключение через порт {} прошло аутентификацию как пользователь {}",
                clientSocket.getPort(), userName);
    }

    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command != null) {
                if (command.getType() == PRIVATE_MESSAGE) {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    server.sendPrivateMessage(this, data.getRecipient(), data.getMessage());
                } else if (command.getType() == PUBLIC_MESSAGE) {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    this.server.sendPublicMessage(data.getMessage(), this);
                }
            }
        }
    }

    public String getUserName() {
        return userName;
    }
}