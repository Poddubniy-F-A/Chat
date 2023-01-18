package server;

import server.model.Server;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ServerLauncher {
    private static final int PORT = 8189;

    public static void main(String[] args) {
        try {
            OutputStream outputStream = new FileOutputStream("logs/logfile.log", true);
            outputStream.write(("<<<NEW SESSION>>>\n").getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (Exception e) {
            System.err.println("Файл логирования не существует");
            e.printStackTrace();
        }

        new Server().start(PORT);
    }
}