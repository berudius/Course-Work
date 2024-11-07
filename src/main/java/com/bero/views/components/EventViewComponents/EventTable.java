package com.bero.views.components.EventViewComponents;

import java.sql.SQLException;
import java.util.List;

import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Event;
import com.vaadin.flow.component.html.Div;

public class EventTable {

   private static EventTable eventTable;
   private Div eventRows;
   Div overlay;
   Div orderDetailsContainer;
    
    private EventTable(){
        this.overlay = new Div();
        overlay.addClassName("overlay");

        this.orderDetailsContainer = new Div();
        orderDetailsContainer.addClassName("order-details");
        

        overlay.getElement().addEventListener("click", e->{
            hideOrderdetails();
        });
    }

   public static void resetState(){
    eventTable = null;
    }

   public void hideOrderdetails(){
    if(overlay.hasClassName("show")){
        this.overlay.removeClassName("show");
        this.orderDetailsContainer.removeClassName("show");
    }


   }
   public void showOrderdetails(){
        this.overlay.addClassName("show");
        this.orderDetailsContainer.addClassName("show");
   }



    public static EventTable getEventTable() {
        if(eventTable == null){
            eventTable = new EventTable();
        }

        return eventTable;
    }





    public Div createEventTable() throws ClassNotFoundException, SQLException{

        Div eventTable = new Div(
            createHederRow("Дата", "Час", "Подія", "Деталі"),
            fillEventTable(),
            createPlusButton(this.eventRows)
            );
        eventTable.addClassName("order-styled-table");


        return eventTable;
    }
    

    private Div fillEventTable() throws ClassNotFoundException, SQLException{
       this.eventRows = new Div();
       this.eventRows.addClassName("event-rows-container");

       DB_Handler.connect();
       List<Event> events = DB_Handler.getAllEvents();
       DB_Handler.disconnect();
       for (Event event : events) {
        this.eventRows.add(event.createEventRow());
       }

       return this.eventRows;
    }

    private Div createHederRow(String... heders) {
        Div hederRow = new Div();
        hederRow.addClassName("table-header");
        
        for (String heder : heders) {
            Div cell = new Div(heder);
            cell.addClassName("header-cell");
            hederRow.add(cell);
        }

        return hederRow;
        }


        public Div getOrderDetailsContainer(){
            return this.orderDetailsContainer;
        }
        public Div getOverlay(){
            return this.overlay;
        }

        public Div getEventRowsContainer(){
            return this.eventRows;
        }



    public Div createPlusButton(Div eventRowsContainer) {
        Div plusButton = new Div("+");
        plusButton.addClassName("add-button");
    
        plusButton.getElement().addEventListener("click", e -> {
            Event event = new Event();
            event.setDefault();
            try {
                DB_Handler.connect();
                DB_Handler.addEvent(event);
                DB_Handler.disconnect();
                eventRowsContainer.addComponentAtIndex(0, event.createEventRow());
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
    
        return plusButton;
    }

        


    




}
