package com.bero.views.components.StuffViewComponents;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Waiter;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;

public class StuffQueryController {
    private Div enteringDiv;
    private Div radioButtonContainer;
    private Div waitersContainer;

    public StuffQueryController(Div waitersContainer){
        this.waitersContainer = waitersContainer;
    }

    public Div createRadioButtonContainer(){
        Div radioContainer8 = createRadioButton("8", "Запит8", "Отримати інформацію про офіціантів, які обслуговують столик з вказаними номерами.");
        Div radioContainer9 = createRadioButton("9", "Запит9", "Отримати повну інформацію про офіціантів, які можуть обслуговувати весілля. (припускаємо, що весілля можуть проводити тільки офіціанти із рангом компетенції > Середній спеціаліст)");
        Div subQuery = createRadioButton("-1", "Підзапит 9-го", "Отримати інформацію про офіціантів, які працюють тільки по вихідним.");
        
        this.enteringDiv = new Div();
        enteringDiv.addClassName("entering");

        this.radioButtonContainer = new Div(radioContainer8, radioContainer9, subQuery, enteringDiv);
        radioButtonContainer.addClassName("centered-div");
        radioButtonContainer.getStyle()
        .set("margin-top", "20px");

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
            resetWaitersRows();
           doAppropriateQuery(value);
        });
        
        return radioDiv;
    }

   private void doAppropriateQuery(String value){
    this.enteringDiv.addClassName("show-flex");
         switch (value) {
            case "8":  doEighthQuery(); 
                break;
            case "9":  doNinethQuery();
                break;
            case "-1":  doSubQuery();
                break;
         }
   }

   private void doEighthQuery(){
    this.enteringDiv.removeAll(); 
    radioButtonContainer.getStyle().set("padding-bottom", "60px");
    fillEnteringDivForEighthQuery();
   }
   private void doNinethQuery(){
    this.waitersContainer.removeAll();
    try {
        DB_Handler.connect();
        List<Waiter> waiters = DB_Handler.getAllWaiters();
        DB_Handler.disconnect();

        boolean isAnyCanConductWedding = false; 
        CompetencyEnum minimalAllowedEnumForWedding = getCompetencyEnumFromString("Професіонал");
        for (Waiter waiter : waiters) {
            CompetencyEnum waiterCommpetencyEnum = getCompetencyEnumFromString(waiter.getCompetencyRank()); 
            if(waiterCommpetencyEnum.isGreaterOrEq(minimalAllowedEnumForWedding)){
                this.waitersContainer.add(waiter.createTableRow());
                isAnyCanConductWedding = true;
            }
        }
        String message = isAnyCanConductWedding ? "Нижче відображено офіціантів, що можуть обслуговувати весілля" : "Ніхто із офіціантів не може обслуговувати весілля";
        Notification.show(message);
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
   }
   private void doSubQuery(){
    this.waitersContainer.removeAll();

    try {
        DB_Handler.connect();
        List<Waiter> waiters =  DB_Handler.getWaitersWorkingWekends();
        DB_Handler.disconnect();
        for (Waiter waiter : waiters) {
            this.waitersContainer.add(waiter.createTableRow());
        }
        String message = waiters.size() > 0 ? "Працівники, що працюють на вихідних відображено в контейнері працівників" : "У вихідні дні ніхто не працює";
        Notification.show(message);
      
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
   }

   private void fillEnteringDivForEighthQuery(){
   
    List<String> tablesNumbers = new ArrayList<>();
    try {
        DB_Handler.connect();
        tablesNumbers = DB_Handler.getAllNotFreeTablesNumbers();
        DB_Handler.disconnect();
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    
    Select<String> tableSelect = createSelect(tablesNumbers.toArray(new String [0]));
    tableSelect.setPlaceholder("Оберіть стіл, щоб додати до пошуку");
    Div selectedTablesByUser = new Div();
    selectedTablesByUser.addClassName("parent-parent");
    
    this.enteringDiv.add(tableSelect, selectedTablesByUser);

     List<String> tablesNumbersToCheckWaiters = new ArrayList<>();

    tableSelect.getElement().addEventListener("change", e->{
        String selectedTable = tableSelect.getValue();
        
        Span parentRemover = new Span("X");
        parentRemover.addClassName("parentRemover");
        Span selectedTableSpan = new Span(selectedTable);
        Div spansContainer = new Div(selectedTableSpan, parentRemover);
        spansContainer.addClassName("parent");

        tablesNumbersToCheckWaiters.add(selectedTable);
        selectedTablesByUser.add(spansContainer);
        removeSelectedItem(tableSelect);
        
        if(tablesNumbersToCheckWaiters.size() > 0){ processEigthQuery(tablesNumbersToCheckWaiters); }

        parentRemover.addClickListener(event->{
            parentRemover.getParent().get().removeFromParent();
            tablesNumbersToCheckWaiters.remove(selectedTable);
            addNewItemSelect(tableSelect, selectedTable);

            if(tablesNumbersToCheckWaiters.size() > 0){ processEigthQuery(tablesNumbersToCheckWaiters); }
        });
    });

   }

    private void processEigthQuery(List<String> tableNumbers ){
        this.waitersContainer.removeAll();

        try {
            DB_Handler.connect();
            List<Waiter> waiters =  DB_Handler.getWaitersByTablesNumbers(tableNumbers);
            DB_Handler.disconnect();

            for (Waiter waiter : waiters) {
                this.waitersContainer.add(waiter.createTableRow());
            }
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

   private void resetWaitersRows(){
    this.waitersContainer.removeAll();
    try {
        DB_Handler.connect();
        List<Waiter> waiters = DB_Handler.getAllWaiters();
        for (Waiter waiter : waiters) {
            waitersContainer.add(waiter.createTableRow());
        }
        DB_Handler.disconnect();
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
   
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

    protected void addNewItemSelect(Select<String> select, String newItem) {

        List<String> items = new ArrayList<>(select.getListDataView().getItems().collect(Collectors.toList()));
    
 
        if (!items.contains(newItem)) {
            items.add(newItem);
            items.sort((item1, item2) ->{
                int num1 = extractLeadingNumber(item1);
                int num2 = extractLeadingNumber(item2);
            
                // Якщо обидва мають числові значення, порівнюємо їх
                if (num1 != -1 && num2 != -1) {
                    return Integer.compare(num1, num2);
                }
            
                // Якщо тільки одне з них має число, воно стає "меншим"
                if (num1 != -1) return -1;
                if (num2 != -1) return 1;
            
                // Інакше порівнюємо як строки
                return item1.compareToIgnoreCase(item2);
            });
            select.setItems(items);
        }
    }
    

    protected void removeSelectedItem(Select<String> select) {
    
    String selectedItem = select.getValue();

    if (selectedItem != null) {
        
        List<String> items = new ArrayList<>(select.getListDataView().getItems().collect(Collectors.toList()));

        items.remove(selectedItem);

        
        select.setItems(items);

       
        select.clear();
    }
}

private int extractLeadingNumber(String item) {
    // Використовуємо регулярний вираз для пошуку числової частини на початку рядка
    String numberPart = item.replaceAll("^\\D*(\\d+).*", "$1");

    try {
        return Integer.parseInt(numberPart);
    } catch (NumberFormatException e) {
        return -1; // Якщо немає числової частини
    }
}

public static CompetencyEnum getCompetencyEnumFromString(String status) {
    for (CompetencyEnum competencyEnum : CompetencyEnum.values()) {
        if (competencyEnum.getStatus().equalsIgnoreCase(status)) {
            return competencyEnum; 
        }
    }
    return null; 
}

}
