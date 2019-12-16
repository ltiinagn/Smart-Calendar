package com.java.weekview.sample;

public class UserProfile {
    public String username;
    public String email;
    public String auth;

    public UserProfile() {
    }

    public UserProfile(String username, String email, String auth) {
        this.username = username;
        this.email = email;
        this.auth = auth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuth() {
        return auth;
    }

    public void setAge(String age) {
        this.auth = auth;
    }
}
