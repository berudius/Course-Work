package com.bero.DB_entities;


import java.util.Arrays;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

import lombok.Data;





@Data
public class Waiter extends User{

    @SuppressWarnings("unused")
    private String competencyRank = "";

    public Waiter(){}

    public Waiter(int id, String name, String competencyRank, Key key){
    this.id = id;
    this.name = name;
    this.competencyRank = competencyRank;
    this.key = key;
   }

   public Waiter(int id, String name, String competencyRank){
    this.id = id;
    this.name = name;
    this.competencyRank = competencyRank;
   }

   public Waiter(String name, String competencyRank, Key key){
    this.name = name;
    this.competencyRank = competencyRank;
    this.key = key;
   }

   public Waiter(int id, String name){
    this.id = id;
    this.name = name;
   }

   @Override
   public String getCompetencyRank(){
    return this.competencyRank;
   }

   @Override
   public void setCompetencyRank(String competencyRank){
    this.competencyRank = competencyRank;
   }


   

    @Override
    protected Div createCompetencyCellSelect(Image submitIcon){
        Div competencyCell = new Div(createSelect(competencyRank, Arrays.asList(
            "Початківець",
                 "Середній Спеціаліст",
                 "Професіонал",
                 "Експерт", 
                 "Майстер"), submitIcon));
        competencyCell.addClassName("cell");
        return  competencyCell;
    }

    
   
    
    

   
   
}
