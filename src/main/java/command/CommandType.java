package command;

public enum CommandType {
    ERROR,

    REG,
    ENTER,
    AUTH_OK,

    UPDATE_USERS_LIST,
    PUBLIC_MESSAGE,
    PRIVATE_MESSAGE,
    INCOMING_MESSAGE
}