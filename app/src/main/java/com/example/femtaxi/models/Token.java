package com.example.femtaxi.models;

public class Token {
    String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    //no es client
    @Override
    public String toString() {
        return "Token{" +
                "tokens='" + token + '\'' +
                '}';
    }
}
