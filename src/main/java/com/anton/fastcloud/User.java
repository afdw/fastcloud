package com.anton.fastcloud;

public class User extends DataObject {
    public String username;
    public String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
