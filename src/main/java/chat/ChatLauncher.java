package chat;

import chat.background.AuthTimeout;
import chat.controllers.AuthController;
import chat.controllers.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Timer;

public class ChatLauncher extends Application {
    private static ChatLauncher INSTANCE;
    private Stage chatStage;
    private Stage authStage;
    private FXMLLoader chatWindowLoader;
    private FXMLLoader authLoader;

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.chatStage = primaryStage;

        chatWindowLoader = new FXMLLoader();
        chatWindowLoader.setLocation(ChatLauncher.class.getResource("chatDialog.fxml"));
        Parent root = chatWindowLoader.load();
        chatStage.setScene(new Scene(root));
        getChatController().launchMessageHandler();

        authLoader = new FXMLLoader();
        authLoader.setLocation(ChatLauncher.class.getResource("authDialog.fxml"));
        AnchorPane authDialogPanel = authLoader.load();
        authStage = new Stage();
        authStage.initOwner(chatStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
        getAuthStage().setTitle("Аутентификация");
        getAuthStage().show();
        Timer mTimer = new Timer(true);
        mTimer.schedule(new AuthTimeout(), 0);
        getAuthController().initializeMessageHandler();
    }

    public static void main(String[] args) {
        launch();
    }

    public void switchToChatWindow(String userName, String login) {
        getAuthController().close();
        getAuthStage().close();

        getChatController().getUsernameLabel().setText(userName);
        getChatController().initializeHistory(login);

        getChatStage().setTitle("Чат (" + userName + ")");
        getChatStage().show();
    }

    public void makeErrorDialog(Alert.AlertType dialogType, String title, String message) {
        Alert alert = new Alert(dialogType);
        alert.initOwner(ChatLauncher.getInstance().getChatStage());
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public Stage getChatStage() {
        return chatStage;
    }

    public ChatController getChatController() {
        return chatWindowLoader.getController();
    }

    public AuthController getAuthController() {
        return authLoader.getController();
    }

    public static ChatLauncher getInstance() {
        return INSTANCE;
    }
}