package command.commands;

import java.io.Serializable;

public class PrivateMessageCommandData implements Serializable {
    private final String recipient;
    private final String message;

    public PrivateMessageCommandData(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }
}