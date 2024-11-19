package com.bero.views.components.ScheduleViewComponents;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.WorkSchedule;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class ScheduleTableContainer {

    private Div container = new Div();
    private Div scheduleRows = new Div();

    public ScheduleTableContainer(){}

    public void fillScheduleContainer() throws ClassNotFoundException, SQLException{
        this.container.addClassName("schedule-container");
        this.container.add(createScheduleTable(), createPlusButton(scheduleRows));
    }

    private Div createScheduleTable() throws ClassNotFoundException, SQLException{

        DB_Handler.connect();
        List<WorkSchedule> records = DB_Handler.getWorkScheduleRecords();
        DB_Handler.disconnect();

        Div headerRow = createHeaderRow();
        scheduleRows = new Div();
        scheduleRows.addClassName("schedule-rows-container");

        
        for (WorkSchedule record : records) {
            scheduleRows.add(record.fillScheduleRow());
        }

        Div scheduleTable = new Div(headerRow, scheduleRows);
        scheduleTable.addClassName("schedule-styled-table");

        return scheduleTable;
    }

    private Div createHeaderRow() {
       
        Div hederWaiterNameCell = new Div("Ім'я офіціанта");
        hederWaiterNameCell.addClassName("cell");

        Div hederRow = new Div(hederWaiterNameCell);
        hederRow.add(getDaysOfWeekCells());
        hederRow.addClassName("shedule-header-row");

        return hederRow;
        }

        private List<Component> getDaysOfWeekCells(){

        Map<DayOfWeek, String> map  = getWeekDaysAroundCurrent();
        List<Component> cells = new ArrayList<>();
        map.forEach((dayOfWeek, stringDate) ->{
            String dayName = "";
            switch (dayOfWeek) {
                case MONDAY: dayName = "Понеділок ";
                    break;
                case TUESDAY: dayName = "Вівторок ";
                    break;
                case WEDNESDAY: dayName = "Середа ";
                    break;
                case THURSDAY: dayName = "Четвер ";
                    break;
                case FRIDAY: dayName = "П'ятниця ";
                    break;
                case SATURDAY: dayName = "Субота ";
                    break;
                case SUNDAY: dayName = "Неділя ";
                    break;
            }
            cells.add(createDayOfWeekCell(dayName, stringDate));
        });

        return cells;
    }

    private Div createDayOfWeekCell(String dayName, String stringDate){
        Div dayCell = new Div(new Span(dayName), new Span(stringDate));
        dayCell.addClassName("cell");
        return dayCell;
    }

    private Map<DayOfWeek, String> getWeekDaysAroundCurrent(){
        Map <DayOfWeek, String> WeekDaysAroundCurrentDay = new HashMap<>();

        LocalDate currentDate = LocalDate.now();

        int centerIndex = 3; 
        
        
        for (int i = 1; i <= centerIndex; i++) {
            LocalDate localDate = currentDate.plusDays(i);
            addDayToMap(WeekDaysAroundCurrentDay, localDate);
        }

        addDayToMap(WeekDaysAroundCurrentDay, currentDate);

        for (int i = 1; i <= centerIndex; i++) {
            LocalDate localDate = currentDate.minusDays(i);
            addDayToMap(WeekDaysAroundCurrentDay, localDate);
        }
        
        return WeekDaysAroundCurrentDay
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (e1, e2) -> e1,LinkedHashMap::new
            )
        );
    }

    private void addDayToMap(Map<DayOfWeek, String> map, LocalDate date){
        DayOfWeek day = date.getDayOfWeek();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM");
        String stringDate = date.format(dateFormatter);

        map.put(day, stringDate);   
    }

    public Div getContainer() {
        return this.container;
    }

    private Div createPlusButton(Div schedulRows) {
        Div plusButton = new Div("+");
        plusButton.addClassName("add-button");
    
        plusButton.getElement().addEventListener("click", e -> {
            WorkSchedule record = new WorkSchedule();
            this.scheduleRows.add(record.createAndSaveNewRow());
        });
    
        return plusButton;
    }

}