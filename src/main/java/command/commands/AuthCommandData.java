package command.commands;

import java.io.Serializable;

public class AuthCommandData implements Serializable {
    private final String login;
    private final String password;
    private final String nick;

    public AuthCommandData(String login, String password, String nick) {
        this.login = login;
        this.password = password;
        this.nick = nick;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNick() {
        return nick;
    }
}