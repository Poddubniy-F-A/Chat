package chat.background;

import chat.controllers.ReadMessageListener;
import command.Command;
import org.apache.logging.log4j.Level;
import server.model.Server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Network {
    private static Network INSTANCE;
    private final List<ReadMessageListener> listeners = new CopyOnWriteArrayList<>();

    public static final String SERVER_HOST = "127.0.0.1";
    public static final int SERVER_PORT = 8189;
    private final String host;
    private final int port;
    private Socket socket;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Thread readMessageProcess;

    private boolean connected;

    private Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private Network() {
        this(SERVER_HOST, SERVER_PORT);
    }

    public static Network getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Network();
        }

        return INSTANCE;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            readMessageProcess = startReadMessageProcess();
            connected = true;
            return true;
        } catch (IOException e) {
            Server.logger.log(Level.ERROR, "Ошибка при создании потоков ввода/вывода");
            return false;
        }
    }

    public Thread startReadMessageProcess() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    Command command = readCommand();
                    for (ReadMessageListener listener : listeners) {
                        listener.processReceivedCommand(command);
                    }
                } catch (IOException e) {
                    Server.logger.log(Level.ERROR, "Не удалось получить сообщение от сервера");
                    close();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private Command readCommand() throws IOException {
        Command command = null;

        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            Server.logger.log(Level.ERROR, "Не удалось определить класс полученной команды");
        }

        return command;
    }

    public void sendAuthMessage(String login, String password, String nick, Boolean isReg) throws IOException {
        sendCommand(Command.authCommand(login, password, nick, isReg));
    }

    public void sendPrivateMessage(String receiver, String message) throws IOException {
        sendCommand(Command.privateMessageCommand(receiver, message));
    }

    public void sendPublicMessage(String message) throws IOException {
        sendCommand(Command.publicMessageCommand(message));
    }

    public void sendCommand(Command command) {
        try {
            outputStream.writeObject(command);
        } catch (IOException e) {
            Server.logger.log(Level.ERROR, "Не удалось отправить сообщение на сервер");
        }
    }

    public ReadMessageListener addReadMessageListener(ReadMessageListener listener) {
        this.listeners.add(listener);
        return listener;
    }

    public void removeReadMessageListener(ReadMessageListener listener) {
        this.listeners.remove(listener);
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        try {
            connected = false;
            socket.close();
            readMessageProcess.interrupt();
        } catch (IOException e) {
            Server.logger.log(Level.ERROR, "Не удалось закрыть сетевое соединение");
        }
    }
}