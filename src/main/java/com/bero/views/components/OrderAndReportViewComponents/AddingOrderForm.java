package com.bero.views.components.OrderAndReportViewComponents;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Dish;
import com.bero.DB_entities.Order;
import com.bero.DB_entities.Table;
import com.bero.DB_entities.User;
import com.bero.DB_entities.Waiter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.VaadinSession;

public class AddingOrderForm {
    private static Div addOrderForm;
    private static Div inputsContainer;
    private static Div dishTable;
    private static List<Dish> dishes;

    private static Order order;
    private static Button submitButton;
    private static List<Dish> orderedDishes = new ArrayList<>();

    private static Select<String> tableSelect;

    public static Div createAndGetAddingOrderForm() throws ClassNotFoundException, SQLException{
      addOrderForm = new Div();
      addOrderForm.addClassName("addOrderform");

      H1 title = new H1("Додати замовлення");
      Span closeButton = new Span("            X");
      closeButton.getElement().getStyle().set("font-size", "1em");
      closeButton.getElement().getStyle().set("cursor", "pointer");
      closeButton.getElement().getStyle().set("color", "#ff9800");
      closeButton.getElement().getStyle().set("position", "relative");
      closeButton.getElement().getStyle().set("left", "25px");
      closeButton.getElement().addEventListener("click", e->{
        addOrderForm.getElement().executeJs("document.querySelector(\".addOrderform\").classList.remove(\"show\")");
        clearFilledDataInForm();
      });
      title.add(closeButton);

      inputsContainer = new Div();
      inputsContainer.addClassName("inputsContainer");
      Span label = new Span("Оберіть номер столу");
      label.addClassName("label1");

      //this.tableSelect
      tableSelect = createSelect();
      fillTableSelect(tableSelect);

      Span label2 = new Span("Оберіть страву");
      label2.addClassName("label2");

      Select<String> dishSelect = createSelect();
      fillDishSelect(dishSelect);

      Input quantityInput = new Input();
      quantityInput.setValue("1");
      quantityInput.setPlaceholder("Кількість");
      quantityInput.getElement().addEventListener("change", e ->{
        cheackOrUpdateNumber(quantityInput);
      });


      Button addDishButton = new Button("Додати страву");
      addDishButton.getElement().addEventListener("click", e ->{
        addFoolDishInfoToTable(dishSelect.getValue(), quantityInput.getValue());
        updateTotalSumLabel(addOrderForm);
      });


      fillInputsContainer(inputsContainer, label, tableSelect, label2, dishSelect, quantityInput, addDishButton);
      

      H2 secondTitle = new H2("Список страв");

      dishTable = new Div();
      dishTable.addClassName("styled-table");
      dishTable.getElement().getStyle().set("width", "100%");
      Div dishTableHeader = createHederRow("Назва страви", "Категорія", "Опис", "Кількість", "Ціна за штуку", "Загальна ціна");
      dishTable.add(dishTableHeader);

      H3 generalSumAll = new H3("Загальна сума: ");

      submitButton = new Button("Підтвердити");
      submitButton.addClassName("submitButton");
      submitButton.getElement().addEventListener("click", e->{
        try {
          if(orderedDishes.size() == 0){
            Notification.show("Не обрано жодної страви");
          }
          else{

           boolean success = createAndSaveOrder();
           if(success){
            Div orderTable = order.createTable();
            getOrdersContainer().addComponentAtIndex(0, orderTable);

            // order = null;
            clearFilledDataInForm(); 
            addOrderForm.getElement().executeJs("document.querySelector(\".addOrderform\").classList.remove(\"show\")");
           }
          }
        } catch (ClassNotFoundException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (SQLException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      });

      addOrderForm.add(title, inputsContainer, secondTitle, dishTable, generalSumAll, submitButton);
      return addOrderForm;
    }


    private static void fillInputsContainer(Div inputsContainer, Component... components) throws ClassNotFoundException, SQLException{
      User user = (User) VaadinSession.getCurrent().getAttribute("user");

      if( user instanceof Waiter ){
        for (Component component : components) {
          inputsContainer.add(component);
        }
      }

      else{
        
        Span label = new Span("Оберіть офіціанта");
        label.addClassName("label2");

        Select<String> waiterSelect = createSelect();
        fillWaiterSelect(waiterSelect);

        inputsContainer.add(components[0], components[1]);//adding lable "choose table" and select component of table choosing 
        inputsContainer.add(label, waiterSelect);// adding new select field-component if now is logged in an Owner or an Admin

        for (int i = 2; i < components.length; i++) {
          inputsContainer.add(components[i]);          
        }

      }

    }

    private static Div getOrdersContainer(){
     Main main = (Main)addOrderForm.getParent().get();
     Div ordersContainer = (Div)main.getChildren().skip(1).findFirst().get();
    return ordersContainer;
    }

   private static boolean createAndSaveOrder() throws ClassNotFoundException, SQLException{
    Table table = createTable();
    if(table == null){
      Notification.show("Не обрано столу");
      return false;
    }
    else{

      order = new Order( table, createWaiter(), new ArrayList<>(orderedDishes), LocalDateTime.now());
      DB_Handler.connect();
      int orderId = DB_Handler.addNewOrder(order);
      DB_Handler.disconnect();
      order.setId(orderId);
      return true;
    }

   }

   private static Waiter createWaiter() throws ClassNotFoundException, SQLException{
    User user = (User) VaadinSession.getCurrent().getAttribute("user");

    if(user instanceof Waiter){
      return ((Waiter)user);
    }

    Select<String> waiterSelect =  (Select<String>) inputsContainer.getChildren().skip(3).findFirst().get();
    String waiterName = waiterSelect.getValue();
    DB_Handler.connect();
    user = DB_Handler.getWaiterByName(waiterName);
    DB_Handler.disconnect();
    return ((Waiter)user);
   }

   private static Table createTable() throws ClassNotFoundException, SQLException{

    String tableSelectValue = ((Select<String>)inputsContainer.getChildren().skip(1).findFirst().get()).getValue();

    if (!tableSelectValue.equals("Немає вільних столиків")) {
      // Розбиваємо текст на слова за пробілами
      String[] words = tableSelectValue.split(" ");
      
      if (words.length > 1) { // Перевіряємо, чи є хоча б 2 слова
          String number = words[1]; // Номер столу (друге слово)
  
          DB_Handler.connect();
          Table table = DB_Handler.getTableByNumber(number);
          DB_Handler.disconnect();
  
          return table;
      }
  }
    return null;

   }

    private static void addFoolDishInfoToTable(String dishName, String quantity){
     dishes.stream().filter( dish -> dish.getName().equals(dishName)).findFirst().ifPresent(dish -> {
      dish.setQuantity(Integer.parseInt(quantity));
      Dish alreadyExistingDish = addOrderedDishToList(dish);

      if(alreadyExistingDish == null){
        orderedDishes.add(dish.createClone());
        dishTable.add(createDishRow(dish));
      }
      else{
        updateExistedDishRowQuantityAndGeneralPrice(alreadyExistingDish.getName(), alreadyExistingDish.getQuantity(), alreadyExistingDish.getPrice());
      }
     });
    }

    private static Dish addOrderedDishToList(Dish orderedDish){
      boolean alreadyHasDishWithSuchName[] = {false};
      orderedDishes.stream()
      .filter(dish ->  dish.getName().equals(orderedDish.getName()))
      .findFirst().ifPresent(dish -> {

        alreadyHasDishWithSuchName[0] = true;
        int generalQuantity = dish.getQuantity() + orderedDish.getQuantity();
        if(generalQuantity > 100){
          orderedDish.setQuantity(100);
          dish.setQuantity(100);
        }
        else{
          orderedDish.setQuantity(generalQuantity);
          dish.setQuantity(generalQuantity);
        }

      });


      if(alreadyHasDishWithSuchName[0]){
        return orderedDish;
      }

      return null;
      
    }

 
  private static void updateExistedDishRowQuantityAndGeneralPrice(String dishName, int quantity, double price){
    dishTable.getChildren().filter(dishRow -> {
    Div dishNameCell = (Div)dishRow.getChildren().findFirst().get();
    return dishNameCell.getText().equals(dishName);
    }).findFirst().ifPresent(dishRow -> {

      int quantityColumnPosition = 4; // table header has 6 header-cell and quantity-cell is under number 4
      Div quantityCell = ((Div)dishRow.getChildren().skip(quantityColumnPosition -1).findFirst().get());
      quantityCell.setText("" + quantity);

      int generalPriceColumnPosition = 6;
      Div generalPriceCell = ((Div)dishRow.getChildren().skip(generalPriceColumnPosition -1).findFirst().get());
      generalPriceCell.setText("" + quantity * price);
      
    });
  }

 private static void updateTotalSumLabel( Div addOrderForm){
    int totalSumLabelPosition = 5;
    H3 totalSumLabel = (H3)addOrderForm.getChildren().skip(totalSumLabelPosition - 1).findFirst().get();

    double totalSum = orderedDishes.stream().map(dish -> dish.getPrice() * dish.getQuantity()).reduce(0.0, Double::sum);
    totalSumLabel.setText("Загальна сума: " + totalSum);
 }

    private static Div createDishRow(Dish dish){
      Div dishRow = new Div();
      dishRow.addClassName("table-row");
  
      Div nameCell = new Div(dish.getName());
      nameCell.addClassName("cell");
      
      Div categoryCell = new Div(dish.getCategory());
      categoryCell .addClassName("cell");
  
      Div descriptionCell = new Div(dish.getDescription());
      descriptionCell .addClassName("order-description-cell");
  
      Div quantityCell = new Div(""+dish.getQuantity());
      quantityCell.addClassName("cell");
  
      Div priceCell = new Div(""+dish.getPrice());
      priceCell.addClassName("cell");
  
      Div groupPriceCell = new Div((dish.getQuantity() * dish.getPrice()) + "");
      groupPriceCell.addClassName("cell");
  
      dishRow.add(nameCell, categoryCell, descriptionCell, quantityCell, priceCell, groupPriceCell);
  
      return dishRow;
     }


    public static void fillTableSelect(Select<String> tableSelect) throws ClassNotFoundException, SQLException{
      tableSelect.removeAll();
      DB_Handler.connect();
      List<Table> tables = DB_Handler.getAllFreeTables();
      DB_Handler.disconnect();

      String tableInfo[] = new String[tables.size()];
      for (int i = 0; i < tables.size(); i++) {
        tableInfo[i] = "Стіл " + tables.get(i).getNumber() + " Місткість: " + tables.get(i).getCapacity();
      }

      if (tableInfo.length != 0){
        tableSelect.setItems(tableInfo);
         tableSelect.setValue(tableInfo[0]); 
      }
      else{
        tableSelect.setItems("Немає вільних столиків");
        tableSelect.setValue("Немає вільних столиків"); 
      }

    }

    private static void fillWaiterSelect(Select<String> waiterSelect) throws ClassNotFoundException, SQLException{
      DB_Handler.connect();
      List<Waiter> waiters = DB_Handler.getAllWaiters();
      DB_Handler.disconnect();

      String waiterInfo[] = new String[waiters.size()];
      for (int i = 0; i < waiterInfo.length; i++) {
        waiterInfo[i] = waiters.get(i).getName();
      }

      waiterSelect.setItems(waiterInfo);
      if(waiterInfo.length != 0){
        waiterSelect.setValue(waiterInfo[0]);
      }
    }

    private static void fillDishSelect(Select<String> dishSelect) throws ClassNotFoundException, SQLException{
      DB_Handler.connect();
      dishes = DB_Handler.getAllDishes();
      DB_Handler.disconnect();

      String dishInfo[] = new String[dishes.size()];
      for (int i = 0; i < dishInfo.length; i++) {
        dishInfo[i] = dishes.get(i).getName();
      }


      dishSelect.setItems(dishInfo);
      if(dishInfo.length != 0){
        dishSelect.setValue(dishInfo[0]);
      }
    }

    private static  boolean cheackOrUpdateNumber(Input input){
      try{
         int quantity =  Integer.parseInt(input.getValue());
         if(quantity < 0){
          quantity =  1;
          input.setValue( String.valueOf(quantity));
         }
         else if(quantity > 100){
          quantity =  100;
          input.setValue( String.valueOf(quantity));
         }
          return true;
      }
      catch(NumberFormatException e){
          e.printStackTrace();
          input.setValue("1");
         return false; 
      } 
  }

    private static Div createHederRow(String... heders) {
      Div hederRow = new Div();
      hederRow.addClassName("order-header-row");
      
      for (String heder : heders) {
       Div cell = new Div(heder);
       cell.addClassName("order-heder-cell");
       hederRow.add(cell);
      }

      return hederRow;
   }

    protected static Select<String> createSelect() {
        Select<String> select = new Select<>();
        select.getElement().getStyle().set("color", "black");
        select.addClassNames("input-list", "add-order-select");
        return select;
    }

    public static Div getAddDishForm(){
      return addOrderForm;
    }

    private static void clearFilledDataInForm(){
      orderedDishes.clear();
      H3 totalSum = (H3) addOrderForm.getChildren().skip(4).findFirst().get();
      totalSum.setText("Загальна сума: ");
      dishTable.removeAll();
      Div dishTableHeader = createHederRow("Назва страви", "Категорія", "Опис", "Кількість", "Ціна за штуку", "Загальна ціна");
      dishTable.add(dishTableHeader);
    }


    public static void fillDishTableByRows(Div tableRows){
      dishTable.removeAll();
      dishTable.add(tableRows);
    }

    public static void setInputsValues(Table table, Waiter waiter){
     Select<String> tableSelect = (Select<String>)inputsContainer.getChildren().skip(1).findFirst().get();
     Select<String> waiterSelect = (Select<String>)inputsContainer.getChildren().skip(3).findFirst().get();

     tableSelect.setValue( "Стіл " + table.getNumber() + " Місткість: " + table.getCapacity());
     waiterSelect.setValue(waiter.getName());
    }

    public static Div getAddOrderForm(){
      return addOrderForm;
    }

    public static Div getInputsContainer(){
      return inputsContainer;
    }

    public static Div getDishTable(){
      return dishTable;
    }

    public static List<Dish> getDishes(){
      return dishes;
    }

    public static Select<String> getTableSelect(){
      return tableSelect;
    }

}

