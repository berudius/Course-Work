package com.bero.DB_entities;

public class Admin extends User{

    public Admin(){}
   public Admin(int id, String name, Key key){
    this.id = id;
    this.name = name;
    this.key = key;
   }
   public Admin( String name, Key key){
    this.name = name;
    this.key = key;
   }
}

