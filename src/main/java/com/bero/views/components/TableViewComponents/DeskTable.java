package com.bero.views.components.TableViewComponents;

import java.sql.SQLException;
import java.util.List;
import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Table;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;

public class DeskTable {
    Div DesksTable = new Div();
    static Div tablesRows;

    public Div createTableForDesks(String... headers) throws ClassNotFoundException, SQLException {
    
    Div tableHeader = new Div();
    tableHeader.addClassName("table-header");
    for (String header : headers) {
        tableHeader.add(createHeaderCell(header));
    }

    // Заповнюємо таблицю даними
    Div TablesDataContainer = createTablesDataContainer();
    DesksTable.add(tableHeader, TablesDataContainer, createPlusRow());
    DesksTable.addClassName("styled-table");
    DesksTable.getElement().getStyle().set("width", "45%");

    return DesksTable;
}

private   Div createHeaderCell(String text) {
    Div headerCell = new Div();
    headerCell.addClassName("header-cell");
    headerCell.setText(text);
    return headerCell;
}

private Div createPlusRow(){
    Div tableRow = new Div();
    tableRow.addClassNames("table-row", "plus-row");

    Image plusButton =  new Image("icons/plus-icon.png", "");
    plusButton.addClassName("plus-button");
    plusButton.getElement().addEventListener("click", e->{
       this.tablesRows.add( createEmptyRow());
    });

    tableRow.add(plusButton);

    return tableRow; 
}

private Div createEmptyRow(){
    Div tableRow = new Div();
        tableRow.addClassName("table-row");

        Image submitCreationIcon = new Image("icons/submit-icon.png", "");
        submitCreationIcon.addClassNames("submit-icon", "show");

        
        String deskNumber = "1";//default for empty row
        int capacity = 1; 
        Table table = new Table(deskNumber, capacity);
        
        Span numberSpan = createEditableSpan(table.getNumber());
        Div numberCell = new Div(numberSpan);
        numberCell.addClassName("cell");

        Span capacitySpan = createEditableSpan(table.getCapacity() + "");
        Div capacityCell = new Div(capacitySpan);
        capacityCell.addClassName("cell");
        
        Image removeIcon = new Image("icons/remove-icon.png", "");
        removeIcon.addClassName("stuff-remove-icon");
        removeIcon.getElement().addEventListener("click", e->{
            tableRow.removeFromParent();
        });

        submitCreationIcon.getElement().addEventListener("click", e->{
            try {
                validateAndSaveTable(tableRow, numberSpan, capacitySpan, table, false);

            } catch (SQLException | ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        tableRow.add(numberCell, capacityCell, removeIcon, submitCreationIcon);
        return tableRow;
}


public static boolean validateAndSaveTable(Div tableRow, Span numberSpan, Span capacitySpan, Table table, boolean isUpdate) 
        throws ClassNotFoundException, SQLException {
    DB_Handler.connect();
    
    boolean isValidData = validateEnteredData(numberSpan.getText(), capacitySpan.getText(), table, isUpdate);
    if (isValidData) {
        if (isUpdate) {
            DB_Handler.updateTable(table);
            Notification.show("Столик оновлено успішно");
        } else {
            DB_Handler.addTable(table);
            Notification.show("Столик додано успішно");
            
            Div parent = (Div) tableRow.getParent().get();
            tableRow.removeFromParent();
            parent.add(table.createTableRow());
        }
    }

    DB_Handler.disconnect();
    return isValidData;
}

private static boolean validateEnteredData(String number, String capacity, Table table, boolean isUpdateMode) throws SQLException {

    if(number.isBlank() || capacity.isBlank()){
        Notification.show("Поля не можуть бути порожніми");
        return false;
    }
    if ( ( ! DB_Handler.checkUnickTableNumber(number)) && isUpdateMode == false) {
        Notification.show("Столик із таким номером вже є");
        return false;
    }


    if(isRepetitionMoreThanOne(number) && isUpdateMode){
        Notification.show("Столик із таким номером вже є");
        return false;
    }


    if ( ! updateTableValues(table, number, capacity)) {
        Notification.show("Значення місткості повинно бути додатнім числом");
        return false;
    }
    
     
    return true;
}

private static boolean updateTableValues(Table table, String number, String capacity) {
    if (isPositiveNumber(capacity)) {
        table.setNumber(number);
        table.setCapacity(Integer.parseInt(capacity));
        return true;
    }
    return false;
}

private static boolean isPositiveNumber(String capacityCellValue) {
    try {
        int capacity = Integer.parseInt(capacityCellValue);
        return capacity > 0;
    } catch (NumberFormatException e) {
        e.printStackTrace();
        return false;
    }
}

private Div createTablesDataContainer() throws ClassNotFoundException, SQLException {

    DB_Handler.connect();
    List<Table> tables = DB_Handler.getAllTables();
    DB_Handler.disconnect();

    this.tablesRows = new Div(); 
    tablesRows.addClassName("DB_tables-rows");
    for (Table table : tables) {
        Div tableRow = table.createTableRow();
        tablesRows.add(tableRow);
    }
    return tablesRows;
    
}

    public static Span createEditableSpan(String initialText) {
        Span span = new Span(initialText);
        span.addClassName("editable");
        span.getElement().setAttribute("contenteditable", "true");
        span.getElement().setAttribute("tabindex", "0");
        


        
        span.getElement().addEventListener("input", e->{

            span.getElement().executeJs("return this.innerText;").then(text -> {
            span.setText(text.asString());

           // String textAfterChanging = span.getText();
            
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

     private static boolean isRepetitionMoreThanOne(String number){
        int repetitionCounter[] = {0};
        tablesRows.getChildren().forEach(tableRow ->{
           String localNumber = ((Span) tableRow.getChildren().findFirst().get().getChildren().findFirst().get()).getText();
           if(number.equals(localNumber)){
            repetitionCounter[0]++;
           }
           
        });

        if(repetitionCounter[0] > 1){
            return true;
        }
        return false;
      }
    
}
