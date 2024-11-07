package com.bero.DB_entities;


import lombok.Data;


@Data
public class Key {


    private int id = -1;


    private String login;


    private String password;


    private String accessRight;

    // Constructors
    public Key() {
    }



    public Key(int id, String login, String password, String accessRight) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.accessRight = accessRight;
    }

    public Key(String login, String password, String accessRight){
        this.login = login;
        this.password = password;
        this.accessRight = accessRight;
    }

    public Key(int id, String login, String accessRight) {
        this.id = id;
        this.login = login;
        this.accessRight = accessRight;
    }

   
}
