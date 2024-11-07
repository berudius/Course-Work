package com.bero.DB_entities;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.checkerframework.checker.units.qual.t;

import com.bero.DB_Controllers.DB_Handler;
import com.bero.views.components.EventViewComponents.EventTable;
import com.bero.views.components.OrderViewComponents.AddingOrderForm;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.select.Select;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;


@Data
@NoArgsConstructor
public class Event {
   int id;
   LocalDateTime dateTime;
   String eventType;
   List<Order> orders;




   public Event(int id, LocalDateTime dateTime, String eventType){
      this.id = id;
      this.dateTime = dateTime;
      this.eventType = eventType;
   }

   public Event(int id, LocalDateTime dateTime, String eventType, List<Order> orders){
      this.id = id;
      this.dateTime = dateTime;
      this.eventType = eventType;
      this.orders = orders;
   }

   
   public void setDefault(){
         this.dateTime = LocalDateTime.now();
         this.eventType = "Банкет";
   }

   public Div createEventRow(){

      if(dateTime != null && eventType != null){
         DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
         String date = dateTime.format(dateFormatter);
         String time = dateTime.format(timeFormatter);

         Input dateInput = new Input();
         dateInput.getElement().setAttribute("type", "date");
         dateInput.getElement().setAttribute("max", "31.12.2222");
         dateInput.setValue(date);

         Input timeInput = new Input();
         timeInput.getElement().setAttribute("type", "time");
         timeInput.setValue(time);

         Select<String> eventTypeSelect = createSelect(
            eventType, 
            "День народження",
            "Ювілей",
            "Корпоратив",
            "Фуршет",
            "Банкет",
            "Гала-вечір",
            "Весілля",
            "Повне бронювання ресторану"
        );

         Div dateCell = new Div(dateInput);
         Div timeCell = new Div(timeInput);
         Div eventTypeCell = new Div(eventTypeSelect);

         Span detailsSpan = new Span("Переглянути");
         detailsSpan.addClassName("details-span");
         Div orderDetailsCell = new Div(detailsSpan);

         dateCell.addClassName("cell");
         timeCell.addClassName("cell");
         eventTypeCell.addClassName("cell");
         orderDetailsCell.addClassName("cell");

         Div tableRow = new Div(dateCell, timeCell, eventTypeCell, orderDetailsCell);
         tableRow.add(createContextMenu(tableRow));
         tableRow.addClassName("table-row");

         detailsSpan.getElement().addEventListener("click", e->{
            try {
               fillOrderDetails();
            } catch (ClassNotFoundException | SQLException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
         });

         dateInput.getElement().addEventListener("change", e->{
            try {
               updateDateInDB(dateInput);
            } catch (ClassNotFoundException | SQLException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            
         });

         timeInput.getElement().addEventListener("change", e->{
            try {
               updateTimeInDB(timeInput);
            } catch (ClassNotFoundException | SQLException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
         });

         eventTypeSelect.getElement().addEventListener("change", e->{
            updateEventTypeInDB(eventTypeSelect);
         });

         

         return tableRow;
      }
      
      return new Div();
   }

  private void updateEventTypeInDB(Select<String> eventTypeSelect){

   }

   private void updateTimeInDB(Input input)throws ClassNotFoundException, SQLException{
      String time = input.getValue();
   
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
      LocalTime localTime = LocalTime.parse(time, timeFormatter );
      
     this.dateTime = this.dateTime.with(localTime);
      
        DB_Handler.connect();
        DB_Handler.updateEventDateTime(this.dateTime, this.id);
        DB_Handler.disconnect();
   }

   


   private void updateDateInDB(Input input) throws ClassNotFoundException, SQLException{
    String date = input.getValue();
    
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate localDate = LocalDate.parse(date, dateFormatter);

    this.dateTime = this.dateTime.with(localDate);

    DB_Handler.connect();
    DB_Handler.updateEventDateTime(this.dateTime, this.id);
    DB_Handler.disconnect();
   }
   // private void updateDateInDB(Input input) throws ClassNotFoundException, SQLException{
   //  String date = input.getValue();
    
   //  DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
   //  LocalDate localDate = LocalDate.parse(date, dateFormatter);
   //  LocalTime localTime = this.dateTime.toLocalTime();

   //  this.dateTime = LocalDateTime.of(localDate, localTime);

   //    DB_Handler.connect();
   //    DB_Handler.updateEventDateTime(this.dateTime, this.id);
   //    DB_Handler.disconnect();
   // }

   protected Select<String> createSelect(String initialValue, String... items) {
        Select<String> select = new Select<>();
        select.setItems(items);
        select.setValue(initialValue);
        select.addClassName("input-list");
        select.getElement().addEventListener("change", e->{
            // submitIcon.addClassName("show");
        });
        return select;
    }
   

   private Div createContextMenu(Div eventRow){

        ListItem executeLi = new ListItem("Виконати"); 
        ListItem removeLi = new ListItem("Видалити"); 
        UnorderedList ul = new UnorderedList(executeLi, removeLi);
        Div contextMenu = new Div(ul);
        contextMenu.addClassName("context-menu");

        Span menuCaller = new Span("...");
        menuCaller.addClassName("context-menu-caller");

        Div menuContainer = new Div(menuCaller, contextMenu);
        menuContainer.addClassName("menu");


        
        menuCaller.getElement().addEventListener("click", e ->{
            if(contextMenu.hasClassName("show")){
                contextMenu.removeClassName("show");
            }
            else{
                contextMenu.addClassName("show");
            }
        });

        removeLi.getElement().addEventListener("click", e ->{
         try{
            DB_Handler.connect();
            DB_Handler.removeEventById(this.id);
            DB_Handler.disconnect();
            eventRow.removeFromParent();
         } catch (ClassNotFoundException | SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
        });

        return menuContainer;
    }

   private void fillOrderDetails() throws ClassNotFoundException, SQLException{
      
      DB_Handler.connect();
      this.orders = DB_Handler.getEventOrders(this.id);
      DB_Handler.disconnect();

      EventTable eventTable = EventTable.getEventTable();
      Div orderDetailsContainer = eventTable.getOrderDetailsContainer();
      orderDetailsContainer.removeAll();
      eventTable.showOrderdetails();

      H4 title = new H4("Замовлення в межах події");
      title.getElement().getStyle().set("text-align", "center");
      title.getElement().getStyle().set("color", "#3a3434");
      
      
      Span questionMark = new Span("?");
      questionMark.addClassName("question-mark");
      Span hiddenMessage = new Span("Якщо в контекстному меню немає потрібного вам столика, "+
       "переконайтеся, що цей столик відсутній в інших подіях"); 
      hiddenMessage.addClassName("hidden-message");
      questionMark.add(hiddenMessage);
      
      orderDetailsContainer.add(title, createPlusButton(), questionMark);

      for (Order order : orders) {
         orderDetailsContainer.add(order.createMiniTable());
      }
   }

   public Div createPlusButton() throws ClassNotFoundException, SQLException {
        Div plusButton = new Div("Додати замовлення за номером столу");
        plusButton.addClassName("add-button2");

        ContextMenu contextMenu = new ContextMenu(plusButton);
        contextMenu.getStyle().set("z-index", "1001");
        contextMenu.setOpenOnClick(true);
        plusButton.getElement().addEventListener("click", e->{
         try {
            fillTableContextMenu(contextMenu);
         } catch (ClassNotFoundException | SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
        });
       
    
    
        return plusButton;
    }


    private void fillTableContextMenu(ContextMenu cmenu) throws SQLException, ClassNotFoundException {
      DB_Handler.connect();
      List<Table> tables = DB_Handler.getMissedEventTables(this.id);
      DB_Handler.disconnect();
  
      cmenu.removeAll();
      if(tables.size() != 0){
          for (Table table : tables) {
            cmenu.addItem(table.getNumber(), e ->{
                 try {

                  addOrderToEvent(table);
                  // removeItem(e, cmenu);
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

  

  private void addOrderToEvent(Table table) throws ClassNotFoundException, SQLException{
   DB_Handler.connect();
   Order order = DB_Handler.getOrderByTableId(table.getId());   
   DB_Handler.addEventOrder(order.getId(), this.id);
   
   EventTable eventTable = EventTable.getEventTable();
   eventTable.getOrderDetailsContainer().addComponentAtIndex(3, order.createMiniTable());
   
   DB_Handler.disconnect();
  }

   
}
