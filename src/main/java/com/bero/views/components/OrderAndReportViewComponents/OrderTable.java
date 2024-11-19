package com.bero.views.components.OrderAndReportViewComponents;

import java.sql.SQLException;
import java.util.List;
import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Dish;
import com.bero.DB_entities.Order;
import com.bero.DB_entities.Report;
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
   private void createOrderTables () throws SQLException, ClassNotFoundException{


    DB_Handler.connect();
    List<Order> orders =  DB_Handler.getAllOrders();
   for(Order order : orders){
    this.tablesContainer.add(order.createTable());
   }

   DB_Handler.disconnect();
}

   public Div createReportContainer(){
    Div historyOrderTablesContainer = new Div();
    try{
        DB_Handler.connect();
        List<Report> reports =  DB_Handler.getAllReports();
        DB_Handler.disconnect();
        for(Report report : reports){
            historyOrderTablesContainer.add(report.createTable());
        }
    }
    catch(SQLException | ClassNotFoundException e){
        e.printStackTrace();
    }
    
    return historyOrderTablesContainer;
   }


   public static void openReceipt(Order order, Div orderTable) {
        
        Dialog receiptDialog = new Dialog();
        receiptDialog.setWidth("300px");
        
        
        Span title = new Span("Чек");
        title.getStyle().set("font-size", "20px").set("font-weight", "bold");

        Div receiptContent = new Div(title);

        List<Dish> dishes =  order.getDishes();
       dishes.forEach(dish -> {

            Div item = createItem( dish.getName() + "(" + dish.getQuantity() + ")", "" + dish.getPrice() * dish.getQuantity());
            receiptContent.add(item);
        });

        
        Div total = new Div("Загальна сума: " + order.getTotalSum() + "грн");
        total.getStyle().set("font-weight", "bold").set("margin-top", "10px");
        Div totalDiscount = new Div("Загальна сума із знижкою: " + ((double)Math.round(order.getTotalSum() * (1.0 - order.getDiscountRate()))) + "грн");
        totalDiscount.getStyle().set("font-weight", "bold").set("margin-top", "10px");
        totalDiscount.addClassName("hidden");
        if(order.isHasDiscount()){
            totalDiscount.removeClassName("hidden");
        }
       
        Span cross = new Span("X");
        cross.getElement().getStyle()
            .set("position", "relative")
            .set("left", "6.5px");

        Div closeButton = new Div( cross );
        
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

        
        receiptContent.add(total, totalDiscount);
        
        receiptDialog.add(closeButton, receiptContent);

        receiptDialog.open();

        closeButton.addClickListener(e->{ orderTable.removeFromParent(); receiptDialog.close(); });

        receiptDialog.addDialogCloseActionListener(e->{orderTable.removeFromParent(); receiptDialog.close();});
    }

    private static Div createItem(String description, String price) {
        Div item = new Div();
        item.add(new Text(description + " - " + price));
        item.getStyle().set("margin", "5px 0").set("font-size", "14px");
        return item;
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
