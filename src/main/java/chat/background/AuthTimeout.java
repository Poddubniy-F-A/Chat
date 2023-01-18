package chat.background;

import chat.ChatLauncher;
import javafx.application.Platform;
import org.apache.logging.log4j.Level;
import server.model.Server;

import java.util.TimerTask;

import static javafx.scene.control.Alert.AlertType.WARNING;

public class AuthTimeout extends TimerTask {
    @Override
    public void run() {
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            Server.logger.log(Level.ERROR, "Ошибка прерывания");
        }

        Platform.runLater(() -> {
            if (!Network.getInstance().isConnected()) {
                ChatLauncher.getInstance().makeErrorDialog(WARNING,
                        "Время на авторизацию истекло",
                        "Время на авторизацию в чате истекло. Повторите вход"
                );
                ChatLauncher.getInstance().getAuthStage().close();
                ChatLauncher.getInstance().getChatStage().close();
            }
        });
    }
}