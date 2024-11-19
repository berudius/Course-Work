package com.bero.DB_entities;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.bero.DB_Controllers.DB_Handler;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public abstract class User {
    protected int id;
    protected String name;
    protected Key key; 

    @Getter(AccessLevel.NONE) // Не створюємо геттер
    @Setter(AccessLevel.NONE) // Не створюємо сеттер
    protected String textBeforeChanging = "";

    public Div createTableRow(){

        Div tableRow = new Div();
        tableRow.addClassName("table-row");

        Image submitIcon = new Image("icons/submit-icon.png", "");
        submitIcon.addClassName("submit-icon");
        
        Div nameCell = new Div(createEditableSpan(name, submitIcon));
        nameCell.addClassName("cell");

        Div competencyCell = createCompetencyCellSelect(submitIcon);

        Div loginCell = new Div(createEditableSpan(key.getLogin(), submitIcon));
        loginCell.addClassName("cell");
        Div accessCell = new Div(createSelect(key.getAccessRight(), Arrays.asList("Офіціант", "Адміністратор", "Власник"), submitIcon));
        accessCell.addClassName("cell");
        Image removeIcon = new Image("icons/remove-icon.png", "");
        removeIcon.addClassName("stuff-remove-icon");
        removeIcon.getElement().addEventListener("click", e->{

            try {
                DB_Handler.connect();

            if( this instanceof Waiter ){
                Notification notification = new Notification();
                notification.addClassName("custom-notification");
                notification.setDuration(3000); // Тривалість показу
                String message = 
                    DB_Handler.hasWaiterOrder(this.id)
                    ? "Неможливо видалити користувача допоки користувач має незавершені замовлення."
                     : DB_Handler.hasWaiterReport(this.id) 
                      ? "Неможливо видалити користувача допоки користувач є частиною одного із звітів." 
                       : "";                                      
               if( ! message.isBlank()){ notification.show(message); }
               else{
                tableRow.removeFromParent();
                removeFromDb();
               }
            }
            else{
                tableRow.removeFromParent();
                removeFromDb();
            }
            
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        submitIcon.getElement().addEventListener("click", e->{
            try {
                DB_Handler.connect();
                boolean isUpdatingSyccess = validateUpdating(nameCell, loginCell, competencyCell, accessCell);
                if(isUpdatingSyccess){ submitIcon.removeClassName("show"); }
                DB_Handler.disconnect();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        if(competencyCell == null){
            tableRow.add( nameCell, loginCell, accessCell, removeIcon, submitIcon);
        }
        else{
            tableRow.add(nameCell,competencyCell, loginCell, accessCell, removeIcon, submitIcon);
        }
        if(this instanceof Owner){removeIcon.addClassName("hidden");}
        
        return tableRow;
    }

    private boolean validateUpdating(Div nameCell, Div loginCell, Div competencyCell, Div accessCell) throws SQLException{

       String name = ((Span)nameCell.getChildren().findFirst().get()).getText();
       String login = ((Span)loginCell.getChildren().findFirst().get()).getText();
       String accessRight = ((Select<String>)accessCell.getChildren().findFirst().get()).getValue();
       String competencyRank = "Початківець";//set dafault

       Notification notification = new Notification();
       notification.addClassName("custom-notification");
        notification.setDuration(3000); // Тривалість показу
        
      
       if(this instanceof Waiter){
        competencyRank = ((Select<String>)competencyCell.getChildren().findFirst().get()).getValue();
       }

       if(name.isBlank() || login.isBlank()){
        notification.show("Поля не можуть бути порожніми!");
        return false;
       }

       else if( ! login.equals(this.key.getLogin()) && ! DB_Handler.isLoginUnique(login)){
        notification.show("Логін вже зайнятий іншим користувачем.");
        return false;
       }

       else if(accessRight.equals(this.getKey().getAccessRight()) ){
        this.name = name;
        this.getKey().setLogin(login);
        if( ! competencyRank.isBlank()){this.setCompetencyRank(competencyRank);}

         DB_Handler.updateUser(this);
         return true;
       }

       else{

        if(this instanceof Waiter && DB_Handler.hasWaiterOrder(this.id)){
            notification.show("Неможливо змінити роль користувача допоки користувач має незавершені замовлення.");
            return false;
        }
        else{    
        int i = 0;
        this.getKey().setAccessRight(accessRight);
        this.getKey().setLogin(login);
        DB_Handler.removeUserByKeyId(this.getKey().getId());;
         //the table row with the user
       Div theUIRowWithUserData = (Div)nameCell.getParent().get();
       theUIRowWithUserData.removeFromParent();//removing user from table on UI

        switch (accessRight) {
        
            case "Офіціант":
            DB_Handler.addNewUser(new Waiter(name, competencyRank, new Key(this.key.getLogin(), this.key.getPassword(), this.key.getAccessRight() )));
             break;

            case "Адміністратор":
           DB_Handler.addNewUser(new Admin(name, this.key));
             break;

            case "Власник":
             DB_Handler.addNewUser(new Owner(name, this.key));
             break;
        
            default:
                break;
        }

        this.setKey(null);
        return true;
       }
    }  
    }

    protected static Div createDetails(String name) {
        Div details = new Div();
        details.addClassName("details");
        Div additionalInfo = new Div();
        additionalInfo.addClassName("additional-info");
        additionalInfo.setText("Additional info about " + name);
        details.add(additionalInfo);
        return details;
    }
    
    protected Span createEditableSpan(String initialText, Image submitIcon) {
        Span span = new Span(initialText);
        span.addClassName("editable");
        span.getElement().setAttribute("contenteditable", "true");
        span.getElement().setAttribute("tabindex", "0");
        
        span.getElement().addEventListener("focus", e->{
            this.textBeforeChanging = span.getText();
            
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
           if( ! this.textBeforeChanging.equals(textAfterChanging)){
                submitIcon.addClassName("show");  
            }
        });

            
        });
        return span;
    }

    protected Select<String> createSelect(String initialValue, List<String> items, Image submitIcon) {
        Select<String> select = new Select<>();
        select.setItems(items);
        select.setValue(initialValue);
        select.addClassName("input-list");
        select.getElement().addEventListener("change", e->{
            submitIcon.addClassName("show");
        });
        return select;
    }
    
    private void removeFromDb() throws ClassNotFoundException, SQLException{
       DB_Handler.removeUserByKeyId(this.getKey().getId());
    }

   protected Div createCompetencyCellSelect(Image submitIcon){
    return null;
    }
   public void showAddUserForm(){}

    public void setCompetencyRank(String competencyRank){
    }

    public String getCompetencyRank(){
        return "";
    }  
}
