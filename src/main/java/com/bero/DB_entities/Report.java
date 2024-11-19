package com.bero.DB_entities;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.bero.DB_Controllers.DB_Handler;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Report {
    private int id = -1;
    private Table table;
    private Waiter waiter;
    private List<Dish> dishes;
    private LocalDateTime dateTime;
    private Div dishRows;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private H2 totalSum;
    @Setter (AccessLevel.NONE)
    @Getter (AccessLevel.NONE)
    private H2 totalDiscountSumH2;
    @Setter (AccessLevel.NONE)
    @Getter (AccessLevel.NONE)
    private boolean hasDiscount = false;
    private float discountRate = (float)0.15;
    
        public Report (int id, Table table, Waiter waiter, List<Dish> dishes, LocalDateTime dateTime){
            this.id = id;
            this.table = table;
            this.waiter = waiter;
            this.dishes = dishes;
            this.dateTime = dateTime;
        }
    
        public Div createTable() throws ClassNotFoundException, SQLException{
    
            Div orderTable = new Div();
            orderTable.addClassName("order-styled-table");
            orderTable.setId(this.id + "");
    
            Div containerForTableAndWaiter = createAndFillContainerForTableAndWaiter(false);
            Div HeaderRow = createHederRow("Назва страви", "Категорія", "Опис", "Кількість", "Ціна за штуку", "Загальна ціна");
             
            Span dots = new Span("...");
            dots.addClassName("context-menu-caller");
    
            // ListItem executeLi = new ListItem("Виконати");
            ListItem removeLi = new ListItem("Видалити");
            // UnorderedList ul = new UnorderedList(executeLi, removeLi);
            UnorderedList ul = new UnorderedList( removeLi);
    
            Div contextMenu = new Div(ul);
            contextMenu.addClassName("context-menu");
            Div menu = new Div(dots, contextMenu);
            menu.addClassName("menu");
            menu.getElement().getStyle().set("top", "60px");
    
            //container of all dishes displayed for user
            dishRows = createDishRows();
            
            this.totalSum = new H2( "Загальна сума: " + getTotalSum());
            totalSum.addClassName("totalSum");
            this.totalDiscountSumH2 = new H2("Загальна сума із знижкою " + Math.round(discountRate * 100) + "%: " + ((double)Math.round(getTotalSum() * (1.0-discountRate))));//totalsum * (100%-15%)
            totalDiscountSumH2.addClassNames("hidden", "totalSum");
            if(hasDiscount){
                this.totalDiscountSumH2.removeClassName("hidden");
            }
            orderTable.add(menu, containerForTableAndWaiter, HeaderRow, dishRows, totalSum, totalDiscountSumH2);
       
            boolean clicked[] = {false};
            dots.getElement().addEventListener("click", e ->{
                if(clicked[0]){
                    contextMenu.removeClassName("show");
                    clicked[0] = false;
                }
                else{
                    contextMenu.addClassName("show");
                    clicked[0] = true;
                }
            });
    
            removeLi.getElement().addEventListener("click", e->{
                try {
                    deleteReportFromDBAndUI(orderTable, contextMenu);
                } catch (SQLException | ClassNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            });
            
            return orderTable;
        }
    
        private void deleteReportFromDBAndUI (Div orderTable, Div contextMenu) throws ClassNotFoundException, SQLException{
            DB_Handler.connect();
            DB_Handler.removeReportById(this.id);
            DB_Handler.disconnect();
            orderTable.removeFromParent();
            contextMenu.removeClassName("show");
        }
    
        private Div createAndFillContainerForTableAndWaiter(boolean miniMode) throws ClassNotFoundException, SQLException {
        Div tableAndWaiterContainer = new Div();
        tableAndWaiterContainer.addClassName("table-header");
    
        Div tableCell = new Div("Стіл: " + this.table.getNumber());
        tableCell.addClassName("cell");
       
    
        Div waiterCell = new Div("Офіціант: " + this.waiter.getName());
        waiterCell.addClassName("cell");
    
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Div dateTime = new Div(this.dateTime.format(formatter));
        dateTime.addClassName("cell");
    
        tableAndWaiterContainer.add(tableCell, waiterCell, dateTime);
        
        return tableAndWaiterContainer;
       }
    
       public double getTotalSum() {
        double totalSum = 0.0;
    
        for (Dish dish : this.dishes) {
            totalSum += dish.getPrice() * dish.getQuantity();
        }
        if(totalSum >= 500){
          this.hasDiscount = true;
    }

    return totalSum;
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

    private Div createDishRows(){
        Div tableDishRows = new Div();
        for (Dish dish : this.dishes) {
        tableDishRows.add(createDishRow(dish));
        }

        return tableDishRows;
   }

   private Div createDishRow(Dish dish){
    Div dishRow = new Div();
    dishRow.addClassName("table-row");

    Div nameCell = new Div(dish.getName());
    nameCell.addClassName("cell");
    
    Div categoryCell = new Div(dish.getCategory());
    categoryCell .addClassName("cell");

    Div descriptionCell = new Div(dish.getDescription());
    descriptionCell .addClassName("cell");

    Div quantityCell = new Div(""+dish.getQuantity());
    quantityCell.addClassName("cell");

    Div priceCell = new Div(""+dish.getPrice());
    priceCell.addClassName("cell");

    Div groupPriceCell = new Div((dish.getQuantity() * dish.getPrice()) + "");
    groupPriceCell.addClassName("cell");

    dishRow.add(nameCell, categoryCell, descriptionCell, quantityCell, priceCell, groupPriceCell);
    return dishRow;
   }
}