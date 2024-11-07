package com.bero.DB_entities;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.bero.DB_Controllers.DB_Handler;
import com.bero.views.components.OrderViewComponents.OrderTable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
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
public class Order {

    private int id = -1;

    private Table table;

    private Waiter waiter;
    private List<Dish> dishes;

    private LocalDateTime dateTime;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private H2 totalSum;
    //container of all dishes displayed for user
    private Div dishRows;

    public Order(Table table, Waiter waiter, List<Dish> dishes, LocalDateTime dateTime){
        this.table = table;
        this.waiter = waiter;
        this.dishes = dishes;
        this.dateTime = dateTime;
    }

    public Order (int id, Table table, Waiter waiter, List<Dish> dishes, LocalDateTime dateTime){
        this.id = id;
        this.table = table;
        this.waiter = waiter;
        this.dishes = dishes;
        this.dateTime = dateTime;
    }


    public Div createTable() throws ClassNotFoundException, SQLException{

        Div table = new Div();
        table.addClassName("order-styled-table");
        table.setId(this.id + "");

        Div containerForTableAndWaiter = createAndFillContainerForTableAndWaiter(false);
        Div HeaderRow = createHederRow("Назва страви", "Категорія", "Опис", "Кількість", "Ціна за штуку", "Загальна ціна");
         
        

        Span dots = new Span("...");
        dots.addClassName("context-menu-caller");

        ListItem executeLi = new ListItem("Виконати");
        ListItem removeLi = new ListItem("Видалити");
        UnorderedList ul = new UnorderedList(executeLi, removeLi);

        Div contextMenu = new Div(ul);
        contextMenu.addClassName("context-menu");
        Div menu = new Div(dots, contextMenu);
        menu.addClassName("menu");
        menu.getElement().getStyle().set("top", "60px");

        //container of all dishes displayed for user
        dishRows = createDishRows();
        Button addDishbButton = new Button("Додати страву");
        addDishbButton.getElement().getStyle().set("margin", "10px 30% 0 30%");

        ContextMenu cmenu = new ContextMenu(addDishbButton);
        cmenu.setOpenOnClick(true);
        fillDishesContextMenu(cmenu);

        this.totalSum = new H2( "Загальна сума: " + getTotalSum());
        totalSum.addClassName("totalSum");
       
        table.add(menu, containerForTableAndWaiter, HeaderRow, dishRows, addDishbButton, totalSum);
   
        

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

        executeLi.getElement().addEventListener("click", e->{
            OrderTable.openReceipt(this);
            try {
                DB_Handler.connect();
                DB_Handler.saveExecutedOrder(this.id);
                table.removeFromParent();
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
        });

        removeLi.getElement().addEventListener("click", e->{
            table.removeFromParent();
            try {
                DB_Handler.connect();
                DB_Handler.removeOrderById(this.id);
                DB_Handler.disconnect();
                contextMenu.removeClassName("show");
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        

        return table;
    }
 
    public Div createMiniTable() throws ClassNotFoundException, SQLException{
        Div table = new Div();
        table.addClassName("event-order-styled-table");

        Div containerForTableAndWaiter = createAndFillContainerForTableAndWaiter(true);
        Div HeaderRow = createMiniHederRow("Назва страви", "Категорія", "Опис", "Кількість", "Ціна за штуку", "Загальна ціна");

        //container of all dishes displayed for user
        dishRows = createMiniDishRows();


        this.totalSum = new H2( "Загальна сума: " + getTotalSum());
        totalSum.addClassName("totalSum");

        Span redirectSpan = new Span("Перейти до замовлення");
        redirectSpan.addClassName("event-order-span");
        redirectSpan.getElement().getStyle()
        .set("margin-left", "5%");

        Span removeSpan = new Span("Видалити");
        removeSpan.addClassName("event-order-span");
        removeSpan.getStyle()
        .set("margin-left", "3%");

        Div infoContainer = new Div(totalSum, redirectSpan, removeSpan);
        infoContainer.getElement().getStyle()
        .set("display", "flex")
        .set("align-items", "center");

        removeSpan.getElement().addEventListener("click", e->{
            try {
                removeMiniTable(table);
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        redirectSpan.getElement().addEventListener("click", e->{
            if(this.id != -1){
                UI.getCurrent().navigate("orders/" + this.id);
            }
        });


        table.add(containerForTableAndWaiter, HeaderRow, dishRows, infoContainer);
   
        return table;
    }

    private void removeMiniTable(Div miniTable) throws ClassNotFoundException, SQLException{
        DB_Handler.connect();
        DB_Handler.removeOrderFromEvent(this.id);
        DB_Handler.disconnect();
        miniTable.removeFromParent();
    }


    public double getTotalSum(){
        double totalSum = 0.0;
        for (Dish dish : this.dishes) {
            totalSum += dish.getPrice() * dish.getQuantity();
        }

        return totalSum;
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

    if( ! miniMode){

        tableCell.getElement().getStyle().set("cursor", "pointer");
        waiterCell.getElement().getStyle().set("cursor", "pointer");

        ContextMenu cWaiterMenu = new ContextMenu(waiterCell);
        cWaiterMenu.setOpenOnClick(true);
        fillWaiterContextMenu(cWaiterMenu, tableAndWaiterContainer);

        ContextMenu cTableMenu = new ContextMenu(tableCell);
        cTableMenu.setOpenOnClick(true);
        
        tableCell.getElement().addEventListener("click", e->{
            try {
                cTableMenu.removeAll();
                fillTableContextMenu(cTableMenu, tableAndWaiterContainer);
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
    }
    
    return tableAndWaiterContainer;
   }




private void fillWaiterContextMenu(ContextMenu cmenu, Div tableAndWaiterContainer) throws SQLException, ClassNotFoundException {
    DB_Handler.connect();
    List<Waiter> waiters = DB_Handler.getAllWaiters();
    DB_Handler.disconnect();
    for (Waiter waiter : waiters) {
        cmenu.addItem(waiter.getName(), e ->{

           Div waiterDiv = (Div) tableAndWaiterContainer.getChildren().skip(1).findFirst().get();
           waiterDiv.setText("Офіціант: " + waiter.getName());

           try {
            DB_Handler.connect();
            DB_Handler.updateWaiterInOrder(waiter.getId(), this.id);
            DB_Handler.disconnect();
            this.waiter = waiter;
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
           
        });
    }
    
}
private void fillTableContextMenu(ContextMenu cmenu, Div tableAndWaiterContainer) throws SQLException, ClassNotFoundException {
    DB_Handler.connect();
    List<Table> tables = DB_Handler.getAllFreeTables();
    DB_Handler.disconnect();

    if(tables.size() != 0){
        for (Table table : tables) {
            cmenu.addItem(table.getNumber(), e ->{
               Div tableDiv = (Div) tableAndWaiterContainer.getChildren().findFirst().get();
               tableDiv.setText("Стіл: " + table.getNumber());
    
               try {
                DB_Handler.connect();
                DB_Handler.updateTableInOrder(table.getId(), this.id);
                DB_Handler.disconnect();
                this.table = table;
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            });
    }
    
    
    }else{
        cmenu.addItem("Немає вільних столиків");
    }


    
    
}

private void fillDishesContextMenu(ContextMenu cmenu) throws ClassNotFoundException, SQLException {

    DB_Handler.connect();
  
    List<Dish> dishes = DB_Handler.getAllDishesWithNoImage();
    
    DB_Handler.disconnect();

    for (Dish dish : dishes) {
        cmenu.addItem(dish.getName(), e->{

            try {

                if( ! checkDishExistence(dish)){
                    this.dishes.add(dish);
                    addDishInDishRows(dish);
                    updateTotalPrice();
                    DB_Handler.connect();
                    DB_Handler.addDishToOrder(this.id, dish.getId(), 1);
                    DB_Handler.disconnect();
                }

                else{
                   int dishIndex = updateDishQuantityInOrderDishesList(dish);
                   updateDishRowQuantityAndGeralGroupPriceCells(dishIndex);
                   updateTotalPrice();
                   Dish dishToAddQuantity = this.dishes.get(dishIndex);
                   updateQuantityInDB(dishToAddQuantity);

                }
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }  
        });
    }
    }

    private void updateQuantityInDB(Dish dishToAddQuantity) throws SQLException, ClassNotFoundException{
        DB_Handler.connect();
        DB_Handler.updateOrderDishQuantity(this.id, dishToAddQuantity.getId(), dishToAddQuantity.getQuantity());
        DB_Handler.disconnect();
    }

   private int updateDishQuantityInOrderDishesList(Dish dish){
    int dishIndex = 0;
    for (Dish dishInOrderDishesList : this.dishes) {
        if(dishInOrderDishesList.getId() == dish.getId()){
          if(dishInOrderDishesList.getQuantity() != 100){
            dishInOrderDishesList.setQuantity(dishInOrderDishesList.getQuantity() + 1);  
            return dishIndex;
          } 
          return dishIndex; 
        }
        
        dishIndex++;
    }

    return -1;
   }

   private void updateDishRowQuantityAndGeralGroupPriceCells(int dishIndex){

    if(dishIndex != -1){

       Dish dish = this.dishes.get(dishIndex);

       Div appropriateDishRow = (Div) dishRows.getChildren().skip(dishIndex).findFirst().get();
       int quantityCellPosition = 4;
       int generalDishesPriceCellPosition = 6;
       Div quantityCell = (Div) appropriateDishRow.getChildren().skip(quantityCellPosition-1).findFirst().get();
       quantityCell.setText( "" + dish.getQuantity());

       Div generalDishesPriceCell = (Div) appropriateDishRow.getChildren().skip(generalDishesPriceCellPosition-1).findFirst().get();
       generalDishesPriceCell.setText( "" + dish.getQuantity() * dish.getPrice());

    }
   }

    private boolean checkDishExistence(Dish dish){
        for (Dish dishInOrderDishesList : this.dishes) {
            if(dishInOrderDishesList.getId() == dish.getId()){
                return true;
            }
        }

        return false;
    }
                    
                
    private void addDishInDishRows(Dish dish) {
        Div createdDishRow = createDishRow(dish);
        dishRows.add(createdDishRow);
    }


    private Div createMiniDishRows(){
        Div tableDishRows = new Div();
        tableDishRows.addClassName("rowsContainer");
        for (Dish dish : this.dishes) {
        tableDishRows.add(createMiniDishRow(dish));
        }

        return tableDishRows;
   }

   private Div createMiniDishRow(Dish dish){
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

    ContextMenu contextMenu = new ContextMenu(dishRow);
    contextMenu.setOpenOnClick(true);

    
    contextMenu.addItem("Видалити страву", event -> {
        try {
            dishes.remove(dish);
            updateTotalPrice();
            dishRow.removeFromParent();
            DB_Handler.connect();
            DB_Handler.removeDishFromOrder(this.id, dish.getId());
            DB_Handler.disconnect();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    });

    contextMenu.addItem("Збільшити на 1", event -> {
        increaseDishQuantityAndUpdatePrice(dish, quantityCell, groupPriceCell);
        try {
            updateQuantityInDB(dish);
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    });
    contextMenu.addItem("Зменшити на 1", event -> {
        decreaseDishQuantityAndUpdatePrice(dish, quantityCell, groupPriceCell);
        try {
            updateQuantityInDB(dish);
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    });


    return dishRow;
   }

  private void increaseDishQuantityAndUpdatePrice(Dish dish, Div quantityCell, Div groupPriceCell){
        if(dish.getQuantity() != 100){
            dish.setQuantity(dish.getQuantity() + 1);
            quantityCell.setText(""+dish.getQuantity());
            groupPriceCell.setText((dish.getQuantity() * dish.getPrice()) + "");
            updateTotalPrice();
        }
   }

   private void decreaseDishQuantityAndUpdatePrice(Dish dish, Div quantityCell, Div groupPriceCell){
        if(dish.getQuantity() != 1){
            dish.setQuantity(dish.getQuantity() - 1);
            quantityCell.setText(""+dish.getQuantity());
            groupPriceCell.setText((dish.getQuantity() * dish.getPrice()) + "");
            updateTotalPrice();
        }
   }


   private void updateTotalPrice(){
    this.totalSum.setText("Загальна сума: " + this.getTotalSum());
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

    private Div createMiniHederRow(String... heders) {
        Div hederRow = new Div();
        hederRow.addClassName("table-header");
        
        for (String heder : heders) {
            Div cell = new Div(heder);
            cell.addClassName("header-cell");
            hederRow.add(cell);
        }

        return hederRow;
        }
    

}


    
    // private Div cloneRows(Div dishRows){

    //     Div cloned = new Div();

    //     List<Component> rows = dishRows.getChildren().toList();
    //     for (Component component : rows) {
    //         cloned.add(cloneRow((Div)component));
    //     }

    //     H2 title = new H2("Ново-додані страви:");
    //     title.getElement().getStyle().set("background", "#292929");
    //     title.getElement().getStyle().set("padding", "10px 0");
    //     title.getElement().getStyle().set("margin", "0");

    //     cloned.add(title);
    //     return cloned;
    // }


    // private Div cloneRow(Div originalRow) {
    //     Div clonedRow = new Div();
    //     clonedRow.addClassName("table-row");

    //     int i = 0;
    //     int descriptionCellPosition = 3;
    //     for (Component child : originalRow.getChildren().collect(Collectors.toList())) {
    //         if (child instanceof Div) {
    //             Div cellClone = new Div();
    //             if(i == descriptionCellPosition -1){
    //                 cellClone.addClassName("order-description-cell");
    //             }
    //             else{
    //                 cellClone.addClassName("cell");
    //             }
    //             cellClone.setText(((Div) child).getText());
    //             clonedRow.add(cellClone);


    //             i++;
    //         }

    //     }

    //     return clonedRow;
    // }


// private void updateTotalPrice(Div dishRow){
//     Div totalSumParent = (Div) dishRow.getParent().get().getParent().get();
//     H2 totalSum = (H2)totalSumParent.getChildren().reduce((firs, second) -> second).orElse(null);
//     String text = totalSum.getText();
//     totalSum.setText("Загальна сума: " + this.getTotalSum());
//    }

//     private Div createHederRow(String... heders) {
//         Div hederRow = new Div();
//         hederRow.addClassName("table-header");
        
//         for (String heder : heders) {
//          Div cell = new Div(heder);
//          cell.addClassName("header-cell");
//          hederRow.add(cell);
//         }

//         return hederRow;
//      }
    
//      public void addDishToDishes(Dish dish){
        
//      }
