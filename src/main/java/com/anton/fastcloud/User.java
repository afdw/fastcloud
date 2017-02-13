package com.anton.fastcloud;

import java.util.Arrays;

public class User extends DataObject {
    public String username;
    public String password;
    public boolean test;
    public User[] fiends;

    public User() {
    }

    public User(String username, String password, boolean test, User[] fiends) {
        this.username = username;
        this.password = password;
        this.test = test;
        this.fiends = fiends;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", test=" + test +
                ", fiends=" + Arrays.toString(fiends) +
                '}';
    }
}
