package com.bero.DB_entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import lombok.Data;

@Data
public class Dish {

    private int id;
    private String category;
    private String name;
    private String description;
    private double price;
    private int  quantity;
    private Image image;
    private byte[] byteImage;

    public Dish createClone(){
        return new Dish(id, category, name, description, price, quantity);
    }

    public Dish(){}
    public Dish(int id,String category, String name, String description, double price, Image image){
        this.id = id;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    public Dish(int id,String category, String name, String description, double price, int quantity){
        this.id = id;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }


    public void setImageFromByteImage(){
        if(byteImage != null){
            String base64 = Base64.getEncoder().encodeToString(this.byteImage);
            String imageUrl = "data:image/jpeg;base64," + base64;

            this.image = new Image(imageUrl,"");
        }  
    }
    

    public void setByteImageByByteBuffer(MemoryBuffer buffer) {
        InputStream inputStream = buffer.getInputStream();
        try (ByteArrayOutputStream byteArr = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                byteArr.write(data, 0, bytesRead);
            }
            
            this.byteImage = byteArr.toByteArray(); 
        } catch (IOException e) {
        }
    }

}
