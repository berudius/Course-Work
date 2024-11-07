package com.bero.views.components.OrderViewComponents;

import java.sql.SQLException;
import java.util.List;

import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Order;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;


public class OrderTable {

   Div tablesContainer = new Div();

   public OrderTable (){}

   public Div getTablesContainer() throws ClassNotFoundException, SQLException{
    createOrderTables();
    return this.tablesContainer;
   }


   public static void openReceipt(Order order) {
        
        Dialog receiptDialog = new Dialog();
        receiptDialog.setWidth("300px");

        
        Span title = new Span("Чек");
        title.getStyle().set("font-size", "20px").set("font-weight", "bold");

        Div receiptContent = new Div(title);

        order.getDishes().forEach(dish -> {

            Div item = createItem( dish.getName() + "(" + dish.getQuantity() + ")", "" + dish.getPrice() * dish.getQuantity());
            receiptContent.add(item);
        });

        
        Span total = new Span("Загальна сума: " + order.getTotalSum() + "грн");
        total.getStyle().set("font-weight", "bold").set("margin-top", "10px");

       
        Span cross = new Span("X");
        cross.getElement().getStyle()
            .set("position", "relative")
            .set("left", "6.5px");

        Div closeButton = new Div( cross );
        closeButton.getElement().addEventListener("click", event -> receiptDialog.close());
        closeButton.getStyle()
            .set("position", "absolute")
            .set("top", "10px")
            .set("right", "10px")
            .set("background", "#ff5a5a")
            .set("color", "#fff")
            .set("border", "none")
            .set("border-radius", "50%")
            .set("width", "24px")
            .set("height", "24px")
            .set("cursor", "pointer");

        
        receiptContent.add(total);
        
        receiptDialog.add(closeButton, receiptContent);

        receiptDialog.open();
    }

    private static Div createItem(String description, String price) {
        Div item = new Div();
        item.add(new Text(description + " - " + price));
        item.getStyle().set("margin", "5px 0").set("font-size", "14px");
        return item;
    }


    private void createOrderTables () throws SQLException, ClassNotFoundException{


        DB_Handler.connect();
        List<Order> orders =  DB_Handler.getAllOrders();
       for(Order order : orders){
        this.tablesContainer.add(order.createTable());
       }

       DB_Handler.disconnect();
    }












    protected Span createEditableSpan(String initialText) {
        Span span = new Span(initialText);
        span.addClassName("editable");
        span.getElement().setAttribute("contenteditable", "true");
        span.getElement().setAttribute("tabindex", "0");
        
        span.getElement().addEventListener("focus", e->{

            
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
        });

            
        });
        return span;
    }


        
}
