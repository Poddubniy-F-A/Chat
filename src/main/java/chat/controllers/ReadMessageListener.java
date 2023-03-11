package chat.controllers;

import command.Command;

public interface ReadMessageListener {
    void processReceivedCommand(Command command);
}