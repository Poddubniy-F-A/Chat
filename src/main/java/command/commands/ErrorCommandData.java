package command.commands;

import javafx.scene.control.Alert;

import java.io.Serializable;

public class ErrorCommandData implements Serializable {
    Alert.AlertType dialogType;
    String title;
    private final String message;

    public ErrorCommandData(Alert.AlertType dialogType, String title, String message) {
        this.dialogType = dialogType;
        this.title = title;
        this.message = message;
    }

    public Alert.AlertType getDialogType() {
        return dialogType;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}