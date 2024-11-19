package com.bero.DB_entities;

import java.sql.SQLException;
import com.bero.DB_Controllers.DB_Handler;
import com.bero.views.components.TableViewComponents.DeskTable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Table {
    private int id = -1;
    private String number;
    private int capacity;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String textBeforeChanging;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String textAfterChanging;

    public Table(){

    }

   public Table(int id , String number, int capacity){
    this.id = id;
    this.number = number;
    this.capacity = capacity;
   }

   public Table(String number, int capacity){
    this.number = number;
    this.capacity = capacity;
   }

   public Table(int id, String number){
    this.id = id;
    this.number = number;
   }

public Div createTableRow(){

        Div tableRow = new Div();
        tableRow.addClassName("table-row");

        Image submitIcon = new Image("icons/submit-icon.png", "");
        submitIcon.addClassName("submit-icon");
        
        Span numberSpan = createEditableSpan(this.number, submitIcon);
        Div numberCell = new Div(numberSpan);
        numberCell.addClassName("cell");

        Span capacitySpan = createEditableSpan(this.capacity + "", submitIcon);
        Div capacityCell = new Div(capacitySpan);
        capacityCell.addClassName("cell");
        
        Image removeIcon = new Image("icons/remove-icon.png", "");
        removeIcon.addClassName("stuff-remove-icon");
        removeIcon.getElement().addEventListener("click", e->{

            try {
                DB_Handler.connect();

            if(DB_Handler.hasTableOrders(this.id)){
                Notification notification = new Notification();
                notification.addClassName("custom-notification");
                notification.setDuration(3000); // Тривалість показу
                notification.show("Неможливо видалити столик допоки столик має незавершені замовлення.");
            }
            else{
                tableRow.removeFromParent();
                DB_Handler.removeTableById(this.id);
            }

            DB_Handler.disconnect();
            
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        submitIcon.getElement().addEventListener("click", e -> {
            try {
              boolean isValidData = DeskTable.validateAndSaveTable(tableRow, numberSpan, capacitySpan, this, true);
                if(isValidData){submitIcon.removeClassName("show");}
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        tableRow.add(numberCell, capacityCell, removeIcon, submitIcon);
        return tableRow;
    }

    public Span createEditableSpan(String initialText, Image submitIcon) {
        Span span = new Span(initialText);
        span.addClassName("editable");
        span.getElement().setAttribute("contenteditable", "true");
        span.getElement().setAttribute("tabindex", "0");
        
        span.getElement().addEventListener("focus", e->{
            this.textBeforeChanging = span.getText();
        });

        span.getElement().addEventListener("input", e->{

            span.getElement().executeJs("return this.innerText;").then(text -> {
            span.setText(text.asString());
            String textAfterChanging = span.getText();
            
            span.getElement().executeJs(
                "const range = document.createRange();" +
                "const sel = window.getSelection();" +
                "range.selectNodeContents(this);" +
                "range.collapse(false);" + // Поставити курсор в кінець
                "sel.removeAllRanges();" +
                "sel.addRange(range);"
            );
           if( ! this.textBeforeChanging.equals(textAfterChanging)){
                submitIcon.addClassName("show");  
            }
        });
 
        });
        return span;
    }
}
