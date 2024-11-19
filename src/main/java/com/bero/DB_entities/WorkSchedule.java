package com.bero.DB_entities;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.bero.DB_Controllers.DB_Handler;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class WorkSchedule {
    private int id;
    private int waiterId;
    private String waiterName;
    private String states[];
    private static LocalDate lastUpdatedRecordDate;
    private boolean isFirstFilled = false;

    @Setter(AccessLevel.NONE)
    private Span waiterNameSpan;

    public WorkSchedule (int id, String waiterName, String states[]){
        this.id = id;
        this.waiterName = waiterName;
        this.states = states;
        this.waiterNameSpan = new Span();
        waiterNameSpan.getStyle().set("text-wrap", "nowrap");
    }

    public WorkSchedule(){
        this.waiterName = "";
        this.states = new String[7];
        this.waiterNameSpan = new Span();
    }

    public static void setLastUpdatedRecordDate(LocalDate date){
        lastUpdatedRecordDate = date;
    }

    private void setStatesDefault(){
        for (int i = 0; i < states.length; i++) {
            states[i] = "Вихідний";
        }
    }

    public Div createAndSaveNewRow(){
        setStatesDefault();
        this.isFirstFilled = true;

        this.waiterNameSpan.setText("Оберіть офіціанта");
        Div nameCell = new Div(waiterNameSpan);
        nameCell.addClassName("cell");
        nameCell.getStyle().set("cursor", "pointer");

        ContextMenu cMenu = new ContextMenu(nameCell);
        cMenu.setOpenOnClick(true);
        nameCell.addClickListener( e->{
            try {
                fillMenuWithUnscheduledWaiters(cMenu);
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        Div row = new Div(nameCell);
        row.addClassName("shedule-row");

        
        for (String  state : states) {
            Div stateCell = new Div(state);
            stateCell.addClassName("cell");
            stateCell.getStyle().set("cursor", "pointer");
            ContextMenu stateMenu = new ContextMenu(stateCell);
            stateMenu.setOpenOnClick(true);
            switch (state) {
                case "Робочий": stateCell.addClassName("worked"); break;
                case "Вихідний": stateCell.addClassName("weekend"); break;
            }
            row.add(stateCell);

            stateCell.addClickListener(e->{
                fillStateMenu(stateMenu, states, stateCell);
            });

        }

        return row;
    }

    private int getClickedStateCellIndex(Div stateCell){
        Div parent = (Div) stateCell.getParent().get();
        List<Component> children = parent.getChildren().toList();
        return children.indexOf(stateCell) - 1; //minus waiterNameCell
    }

    private void fillStateMenu(ContextMenu stateMenu, String states[], Div stateCell){
        int index = getClickedStateCellIndex(stateCell);
        String oppositeState = states[index].equals("Робочий") ? "Вихідний" : "Робочий";
        stateMenu.removeAll();
        stateMenu.addItem(oppositeState, event ->{
            try {
                if(this.isFirstFilled){
                    updateStatesAlternatelyFromIndex(index, stateCell);
                    isFirstFilled = false;
                }
                else{
                    states[index] = oppositeState;
                    stateCell.setText(states[index]);
                    if(oppositeState.equals("Робочий")){
                        stateCell.removeClassName("weekend");
                        stateCell.addClassName("worked");
                    }
                    else if(oppositeState.equals("Вихідний")){
                        stateCell.removeClassName("worked");
                        stateCell.addClassName("weekend");
                    }
                }
                saveStates();
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        stateMenu.addItem("Видалити офіціанта з розкладу", e->{
            removeWorkScheduleRecordFromUI(stateCell);
            try {
                DB_Handler.connect();
                DB_Handler.removeWorkScheduleRecord(this.id);
                DB_Handler.disconnect();
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
    }

    private void fillMenuWithUnscheduledWaiters(ContextMenu cMenu) throws SQLException, ClassNotFoundException{
        DB_Handler.connect();
        List<Waiter> waiters = DB_Handler.getAllWaiterNotInWorkSchedule();
        DB_Handler.disconnect();

        cMenu.removeAll();
        
        for (Waiter waiter : waiters) {
                cMenu.addItem(waiter.getName(), event -> {
                try {
                    if(this.waiterName.equals("")){
                        addNewRecordToDB(waiter);
                    }
                    else {
                        updateRecordInDB(waiter);
                    }
                } catch (ClassNotFoundException | SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            });
        }

        if(waiters.size() == 0){ cMenu.addItem("Всі офіціанти вже є у графіку");}
    }

    private void updateRecordInDB(Waiter waiter) throws ClassNotFoundException, SQLException{
        waiterId = waiter.getId();
        waiterName = waiter.getName();
        waiterNameSpan.setText(waiterName);
        DB_Handler.connect();
        DB_Handler.updateScheduleRecord(this);
        DB_Handler.disconnect();
    }
    private void addNewRecordToDB(Waiter waiter) throws ClassNotFoundException, SQLException{
        waiterId = waiter.getId();
        waiterName = waiter.getName();
        waiterNameSpan.setText(waiterName);
        waiterNameSpan.getStyle().set("text-wrap", "nowrap");
        DB_Handler.connect();
        DB_Handler.addNewScheduleRecord(this);
        DB_Handler.disconnect();
    }

    private void updateStatesAlternatelyFromIndex(int index, Div stateCell) {
        List<Component> stateCells = stateCell.getParent().get().getChildren().skip(1).toList(); // Пропускаємо ячейку з іменем офіціанта
    
        if (index >= 0 && index < states.length) {
            for (int i = 0; i < states.length; i++) {
                // Визначаємо стан "Робочий" або "Вихідний" з урахуванням index
                String currentState = ((i + index) % 2 == 0) ? "Робочий" : "Вихідний";
                states[i] = currentState;
    
                // Оновлюємо візуальне представлення кожної ячейки
                Div cell = (Div) stateCells.get(i);
                cell.setText(currentState);
                if (currentState.equals("Робочий")) {
                    cell.removeClassName("weekend");
                    cell.addClassName("worked");
                } else {
                    cell.removeClassName("worked");
                    cell.addClassName("weekend");
                }
            }
        }
    }
    
   public Div fillScheduleRow() throws ClassNotFoundException, SQLException{
    processStatesForCurrentDate();

    this.waiterNameSpan.setText(waiterName);;
    Div nameCell = new Div(waiterNameSpan);
    nameCell.addClassName("cell");
    nameCell.getStyle().set("cursor", "pointer");
    

    Div row = new Div(nameCell);
    row.addClassName("shedule-row");

    for (String  state : states) {
            Div stateCell = new Div(state);
            stateCell.addClassName("cell");
            stateCell.getStyle().set("cursor", "pointer");
            ContextMenu stateMenu = new ContextMenu(stateCell);
            stateMenu.setOpenOnClick(true);
            switch (state) {
                case "Робочий": stateCell.addClassName("worked"); break;
                case "Вихідний": stateCell.addClassName("weekend"); break;
            }
            row.add(stateCell);

            stateCell.addClickListener(e->{
                fillStateMenu(stateMenu, states, stateCell);
            });

        }

    ContextMenu cMenu = new ContextMenu(nameCell);
    cMenu.setOpenOnClick(true);
    nameCell.addClickListener( e->{
        try {
            fillMenuWithUnscheduledWaiters(cMenu);
        } catch (ClassNotFoundException | SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    });
    return row;
   }

  private void processStatesForCurrentDate() throws ClassNotFoundException, SQLException{
    LocalDate currentDate = LocalDate.now();
    long daysBetween = ChronoUnit.DAYS.between(lastUpdatedRecordDate, currentDate);
    switch ((int)daysBetween) {
        case 1: shiftStates(1);
            break;
        case 2: shiftStates(2);
            break;
        case 3: shiftStates(3);
            break;
        case 4: shiftStates(4);
            break;
        case 5: shiftStates(5);
            break;
        case 6: shiftStates(6);
            break;

        default:
            if(daysBetween > 0){
                refillStates();
            }
            break;
    }
  }

  private void refillStates() throws ClassNotFoundException, SQLException{
    String workedState = "Робочий";
    String wekendState = "Вихідний";

    //6 - last index of states
    states[0] = states[6].equals(workedState) ? wekendState : workedState;
    for (int i = 1; i < states.length; i++) {
        states[i] = states[i-1].equals(wekendState) ? workedState : wekendState;
    }

    saveStates();
  }

  private void shiftStates(int step) throws ClassNotFoundException, SQLException{
    String workedState = "Робочий";
    String wekendState = "Вихідний";

    for (int i = 0; i < states.length; i++) {
        if( i+step < states.length){
            states[i] = states[i + step];
        }
        else{
            states[i] = states[i].equals(workedState) ? wekendState : workedState;
        } 
    }
    saveStates();
  }

 private void removeWorkScheduleRecordFromUI(Div stateCell){
    stateCell.getParent().get().removeFromParent();
  }

  private void saveStates() throws ClassNotFoundException, SQLException{
    DB_Handler.connect();
    DB_Handler.saveStates(states, this.id);
    DB_Handler.disconnect();
  }
}
