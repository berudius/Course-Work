package com.bero.views.components.OrderAndReportViewComponents;

import java.security.PrivateKey;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.t;

import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Waiter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.select.Select;

public class ReportQueryController {

    private Div enteringDiv;
    private Div radioButtonContainer;
    private Div ordersContainer;
    
    private Span message1 = new Span();
    private Span message2 = new Span();

    public ReportQueryController(Div ordersContainer){
        this.ordersContainer = ordersContainer;
    }


    public Div createRadioButtonContainer(){
        Div radioContainer2 = createRadioButton("2", "Запит2", "Вивести інформацію про столики та страви, які обраний офіціант подав у визначений день.");
        Div radioContainer6 = createRadioButton("6", "Запит6", "Визначити столики, за яким не замовляли жодного десерта у визначений день.");
        Div radioContainer7 = createRadioButton("7", "Запит7", "Обчислити загальну суму виконаних замовлень на задану дату; максимальну суму замовлення для деякого столика.");
        
        this.enteringDiv = new Div();
        enteringDiv.addClassName("entering");

        this.radioButtonContainer = new Div(radioContainer2, radioContainer6, radioContainer7, enteringDiv);
        radioButtonContainer.addClassName("centered-div");
        radioButtonContainer.getStyle().set("margin-top", "20px");

        return radioButtonContainer;
    }

    private Div createRadioButton(String value, String label, String hiddenMessage) {
        // Створюємо контейнер для кожного radio button
        Div radioDiv = new Div();
        radioDiv.getStyle().set("display", "flex").set("margin", "0 10px");
        
        // Створюємо елемент input типу radio
        Input radioInput = new Input();
        radioInput.getElement().setAttribute("type", "radio");
        radioInput.getElement().setAttribute("name", "query");
        radioInput.getElement().setAttribute("value", value);
        radioInput.addClassName("radio-item");
        
        // Створюємо заголовок для radio button
        Span hiddenSpan = new Span(hiddenMessage);
        Span radioLabel = new Span(label);
        radioLabel.add(hiddenSpan);

        radioLabel.addClassName("radio-label");
        hiddenSpan.addClassName("query-hidden-message");

         
        
        // Додаємо radio button і label до контейнера
        radioDiv.add(radioInput, radioLabel);

        radioInput.getElement().addEventListener("change", e->{
            resetVisibility();
            message1.setText("");
            message2.setText("");
           doAppropriateQuery(value);
        });
        
        return radioDiv;
    }

   private void doAppropriateQuery(String value){
         switch (value) {
            case "2":  doSecondQuery(); 
                break;
            case "6":  doSixthQuery();
                break;
            case "7":  doSeventhQuery();
                break;
         }
   }

   private void doSecondQuery(){
    enteringDiv.removeAll();
    fillEnteringDivForSecondQuery();
   }
   private void doSixthQuery(){
    enteringDiv.removeAll();
    fillEnteringDivForSixthQuery();
   }
   private void doSeventhQuery(){
    enteringDiv.removeAll();
    fillEnteringDivForSeventhQuery();
   }


   private void fillEnteringDivForSecondQuery(){
    try {
        DB_Handler.connect();
        List<String> waitersNames = DB_Handler.getWaitersNames();
        DB_Handler.disconnect();
        Select<String> waiterSelect = createSelect(waitersNames.toArray(new String[0]));
        waiterSelect.setPlaceholder("Оберіть офіціанта");
        
        Input inputDate = new Input();
        inputDate.setType("date");
        inputDate.setValue(LocalDate.now().toString());
        inputDate.getStyle().set("margin-top", "10px");


        Button button = new Button("Знайти");
        button.getStyle().set("width", "35%");
        enteringDiv.add(waiterSelect, inputDate, button);
        radioButtonContainer.getStyle().set("padding-bottom", "155px");
        enteringDiv.addClassName("show-flex");

        button.addClickListener(e->{
            resetVisibility();
            processSecondQuery(waiterSelect.getValue(), inputDate.getValue());
        });
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
   }

   private void fillEnteringDivForSixthQuery(){
    Input inputDate = new Input();
    inputDate.setType("date");
    inputDate.setValue(LocalDate.now().toString());
    inputDate.getStyle().set("margin-top", "10px");


    Button button = new Button("Знайти");
    button.getStyle().set("width", "35%");
    enteringDiv.add(inputDate, button);
    radioButtonContainer.getStyle().set("padding-bottom", "155px");
    enteringDiv.addClassName("show-flex");

    button.addClickListener(e->{
        resetVisibility();
        processSixthQuery(inputDate.getValue());
    });
   }

   private void fillEnteringDivForSeventhQuery(){
    Input inputDate = new Input();
    inputDate.setType("date");
    inputDate.setValue(LocalDate.now().toString());
    inputDate.getStyle().set("margin-top", "10px");

    List<String> tablesNumbers = fillTableSelectByTablesNumbers(LocalDate.now().toString());
    
    Select<String> tableSelect = createSelect(tablesNumbers.toArray(new String[0]));
    tableSelect.setPlaceholder("Оберіть столик");
    tableSelect.getStyle().set("margin-top", "10px");


    Button button = new Button("Знайти");
    button.getStyle().set("width", "35%");

    enteringDiv.add(inputDate, tableSelect, button, message1, message2);
    radioButtonContainer.getStyle().set("padding-bottom", "265px");
    enteringDiv.addClassName("show-flex");

    inputDate.getElement().addEventListener("change", e->{
        List<String> tablesNumbers2 = fillTableSelectByTablesNumbers(inputDate.getValue());
        tableSelect.removeAll();
        if(tablesNumbers2.size() == 0){
            tableSelect.setItems("На вказану дату не було зафіксовано закінчених замовлень на жоден із столиків."); 
        }
        else{
            tableSelect.setItems(tablesNumbers2.toArray(new String[0]));
        }
    });

    button.addClickListener(e->{
        resetVisibility();
        processSeventhQuery(inputDate.getValue(), tableSelect.getValue());
    });
   }


   private List<String> fillTableSelectByTablesNumbers(String enteredDate){
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = LocalDate.parse(enteredDate, formatter);
    List<String> tablesNumbers = new ArrayList<>();
    try {
        DB_Handler.connect();
        tablesNumbers = DB_Handler.getTablesNumbersFromReportsMatchingDate(date);
        DB_Handler.disconnect();
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    return tablesNumbers;
    
   }

  private void processSeventhQuery(String date, String tableNumber){
    boolean succes = validateTableNumber(tableNumber);
    if(succes){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate eneteredDate = LocalDate.parse(date, dateFormatter);
    
        try {
            DB_Handler.connect();
            Double revenueSum = DB_Handler.calculateRevenueForDate(eneteredDate);
            Double maxSumForEnteredTable = DB_Handler.getMaxSumForTableAtDate(eneteredDate, tableNumber);
            DB_Handler.disconnect();
    
            message1.setText("1. Загальна сума замовлень, виконаних о " + date + ", сягає: " + revenueSum);
            message2.setText("2. Максимальна вартість замовлень, виконаних о " + date + ", серед столиків із номером " + tableNumber + " = " + maxSumForEnteredTable);
            
            filterReportAccordingToEnteredDate(eneteredDate);
        } catch (ClassNotFoundException | SQLException e) {
            Notification.show("Щось пішло не так.");
            e.printStackTrace();
        }
    }
   }



   private void filterReportAccordingToEnteredDate(LocalDate enteredDate){
    this.ordersContainer.getChildren().forEach( report->{

        Div tableHeader = (Div) report.getChildren().skip(1).findFirst().get();
        String reportDateStr = ((Div)tableHeader.getChildren().skip(2).findFirst().get()).getText();
        reportDateStr = reportDateStr.substring(0,10);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate reportDate = LocalDate.parse(reportDateStr, dateFormatter);

        if( ! enteredDate.isEqual(reportDate)){ report.addClassName("hidden"); }
    });
   }

   private void processSixthQuery(String date){
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate eneteredDate = LocalDate.parse(date, dateFormatter); 

    this.ordersContainer.getChildren().forEach( report->{

        Div tableHeader = (Div) report.getChildren().skip(1).findFirst().get();
        String reportDateStr = ((Div)tableHeader.getChildren().skip(2).findFirst().get()).getText();
        reportDateStr = reportDateStr.substring(0,10);
        LocalDate reportDate = LocalDate.parse(reportDateStr, dateFormatter);


        boolean hasDessertCategory = false;
        List<Component> dishesData = report.getChildren().skip(3).findFirst().get().getChildren().toList();

        for (Component dishData : dishesData) {
            String category = ((Div)dishData.getChildren().skip(1).findFirst().get()).getText();

           if(category.equals("Dessert")){ hasDessertCategory = true; }
        }

        if(hasDessertCategory || ! eneteredDate.isEqual(reportDate)){
            report.addClassName("hidden");
        }

    });
   }

   private void processSecondQuery(String waiterName, String date){
    boolean success = validate(waiterName);
    if(success){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate eneteredDate = LocalDate.parse(date, dateFormatter); 
        
        this.ordersContainer.getChildren().forEach( report->{
            Div tableHeader = (Div) report.getChildren().skip(1).findFirst().get();
            String reportWaiterName = ((Div)tableHeader.getChildren().skip(1).findFirst().get()).getText().replace("Офіціант: ", "");
            String reportDateStr = ((Div)tableHeader.getChildren().skip(2).findFirst().get()).getText();
            reportDateStr = reportDateStr.substring(0,10);
            DateTimeFormatter reportDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate reportDate = LocalDate.parse(reportDateStr, dateFormatter);

            if( ! waiterName.equals(reportWaiterName) || (! eneteredDate.isEqual(reportDate)) ){
                report.addClassName("hidden");
            }
        });
    }
   }

   private void resetVisibility(){
    this.ordersContainer.getChildren().forEach( report ->{
        report.removeClassName("hidden");
    });
   }

   private boolean validate(String waiterName){
    if(waiterName.isBlank()){
        Notification.show("Оберіть офіціанта!");
        return false;
    }

    return true;
   }

   protected Select<String> createSelect( String... items) {
        Select<String> select = new Select<>();
        select.setItems(items);
        select.addClassName("input-list");
        select.getStyle().set("color","#1e1e1e !important");

        // select.getElement().addEventListener("change", e->{
        //     resetVisibility();
        // });
        return select;
    }

    public boolean validateTableNumber(String tableNumber){
        if(tableNumber == null){tableNumber = ""; }
        if(tableNumber.isBlank()){
            Notification.show("Оберіть столик для визначення найбільшої вартості замовлених страв серед усіх столиків!");
            return false;
        }

        return true;
    }
   
}