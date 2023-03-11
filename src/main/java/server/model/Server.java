package server.model;

import command.Command;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    protected static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final List<ClientHandler> clients = new ArrayList<>();

    public final static Logger logger = LogManager.getLogger(Server.class);

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            logger.log(Level.INFO, "Сервер начал работу");
            while (true) {
                logger.log(Level.INFO, "Ожидаются новые подключения");
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(this, clientSocket).handle();
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Не удалось подключиться к порту {}", port);
        }
    }

    public synchronized boolean isUserNameBusy(String userName) {
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        notifyUserListUpdated();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        notifyUserListUpdated();
    }

    private void notifyUserListUpdated() throws IOException {
        int clientsNumber = clients.size();
        for (int i = 0; i < clientsNumber; i++) {
            List<String> recipients = new ArrayList<>();

            if (clientsNumber > 2) {
                recipients.add("Чат");
            }
            for (int j = 0; j < i; j++) {
                recipients.add(clients.get(j).getUserName());
            }
            for (int j = i + 1; j < clientsNumber; j++) {
                recipients.add(clients.get(j).getUserName());
            }

            clients.get(i).sendCommand(Command.updateUserListCommand(recipients));
        }
    }

    public synchronized void sendPublicMessage
            (String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendCommand(Command.incomingMessageCommand(sender.getUserName(), message));
            }
        }
    }

    public synchronized void sendPrivateMessage
            (ClientHandler sender, String recipient, String privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender && client.getUserName().equals(recipient)) {
                client.sendCommand(Command.incomingMessageCommand(sender.getUserName(), privateMessage));
            }
        }
    }
}