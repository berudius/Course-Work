package com.bero.DB_entities;



public class Owner extends User {


    public Owner(){}
    public Owner(int id, String name, Key key){
        this.id = id;
        this.name = name;
        this.key = key;
   }
    public Owner( String name, Key key){
        this.name = name;
        this.key = key;
   }

 
   

   
   
}
