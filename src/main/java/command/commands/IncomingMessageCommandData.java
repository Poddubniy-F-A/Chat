package command.commands;

import java.io.Serializable;

public class IncomingMessageCommandData implements Serializable {
    private final String sender;
    private final String message;

    public IncomingMessageCommandData(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}