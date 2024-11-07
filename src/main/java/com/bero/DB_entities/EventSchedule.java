package com.bero.DB_entities;



import lombok.Data;


@Data
public class EventSchedule {

    private int id;


    private String eventType;

    private Order order;
    
    public EventSchedule (String eventType, Order order){
        this.eventType = eventType;
        this.order = order;
    }
}
