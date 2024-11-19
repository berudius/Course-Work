package com.bero.views.components.StuffViewComponents;

import java.sql.SQLException;
import java.util.List;


import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Admin;
import com.bero.DB_entities.Key;
import com.bero.DB_entities.Owner;
import com.bero.DB_entities.User;
import com.bero.DB_entities.Waiter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;

public class StuffTable {

   private enum UserTableType{
    WAITER,
    ADMIN,
    OWNER 
   }

   private   Div stuffTable = new Div();
   private   Div usersRows = new Div();
   private   Div userAddingForm;
   private   Image plusButton;
   private   String userTypes[] = {"Офіціанти", "Адміни", "Власники"};
   private   UserTableType currentRowType = UserTableType.WAITER;

   public StuffTable(){
    stuffTable.addClassName("styled-table");
   }

   private Div createTableForUsers(List<? extends User> users, String... headers) throws ClassNotFoundException, SQLException {

    // Заголовки таблиці
    Div tableHeader = new Div();
    tableHeader.addClassName("table-header");
    for (String header : headers) {
        tableHeader.add(createHeaderCell(header));
    }

    // Заповнюємо таблицю даними
    Div UsersDataContainer = createUsersDataContainer(users);
    this.userAddingForm = createAddUserForm(currentRowType);
    stuffTable.add(tableHeader, createUserTypesRow(), UsersDataContainer, this.userAddingForm, createPlusRow());

    return stuffTable;
}

private   Div createUserTypesRow(){
    Div tableRow = new Div();
    tableRow.addClassNames( "user-types-row");
    
        Span waiter = new Span(userTypes[0]);
        waiter.addClassName("user-type");
        waiter.getElement().addEventListener("click", 
        e ->{
            resetStyles(waiter);
            stuffTable.removeAll();
            userAddingForm.removeAll();
            currentRowType = UserTableType.WAITER;
            this.userAddingForm = createAddUserForm(currentRowType);
            try {
                createTableForWaiters();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        tableRow.add(waiter);

        Span admin = new Span(userTypes[1]);
        admin.addClassName("user-type");
        admin.getElement().addEventListener("click", 
        e ->{
             resetStyles(admin);
            stuffTable.removeAll();
            userAddingForm.removeAll();
            currentRowType = UserTableType.ADMIN;
            this.userAddingForm = createAddUserForm(currentRowType);
            try {
                createTableForAdmins();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        tableRow.add(admin);

        Span owner = new Span(userTypes[2]);
        owner.addClassName("user-type");
        owner.getElement().addEventListener("click", 
        e ->{
            resetStyles(owner);
            stuffTable.removeAll();
            userAddingForm.removeAll();
            currentRowType = UserTableType.OWNER;
            this.userAddingForm = createAddUserForm(currentRowType);

            
            try {
                createTableForOwners();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        tableRow.add(owner);
    

    tableRow.getChildren().findFirst().get().addClassName("user-type-selected");
    return tableRow;
}

private   Div createHeaderCell(String text) {
    Div headerCell = new Div();
    headerCell.addClassName("header-cell");
    headerCell.setText(text);
    return headerCell;
}

private   Div createUsersDataContainer(List<? extends User> users) {
    this.usersRows = new Div(); 
    this.usersRows.addClassName("DB_user-rows");
    for (User user : users) {
        Div tableRow = user.createTableRow();
        usersRows.add(tableRow);
    }
    return usersRows;
    
}


public   Div createTableForWaiters() throws ClassNotFoundException, SQLException {
    DB_Handler.connect();
    List<Waiter> waiters = DB_Handler.getAllWaiters();
    DB_Handler.disconnect();
    return createTableForUsers(waiters, "Ім'я", "Кваліфікаційний ступінь", "Логін", "Права доступу");
}

public   Div createTableForAdmins() throws ClassNotFoundException, SQLException {
    DB_Handler.connect();
    List<Admin> admins = DB_Handler.getAllAdmins();
    DB_Handler.disconnect();
    return createTableForUsers(admins, "Ім'я", "Логін", "Права доступу");
}

public   Div createTableForOwners() throws ClassNotFoundException, SQLException {
    DB_Handler.connect();
    List<Owner> owners = DB_Handler.getAllOwners();
    DB_Handler.disconnect();
    return createTableForUsers(owners, "Ім'я", "Логін", "Права доступу");
}

private Div createPlusRow(){
    Div tableRow = new Div();
    tableRow.addClassNames("table-row", "plus-row");
    plusButton =  new Image("icons/plus-icon.png", "");
    plusButton.addClassName("plus-button");
    plusButton.getElement().addEventListener("click", e->{
       
        this.userAddingForm.addClassName("show-flex");
    });

    tableRow.add(plusButton);

    return tableRow; 
}

public void addComponentAtIndex(int index, Component component){
    if(index > 0 && index <= stuffTable.getComponentCount()){
    stuffTable.addComponentAtIndex(index, component);
    }
}

private void resetStyles(Span selectedSpan){
    List<Component> spanUserTypes = stuffTable.getChildren().skip(1).findFirst().get().getChildren().toList();
    for(Component spanType: spanUserTypes){
        spanType.removeClassName("user-type-selected");
    }

   selectedSpan.addClassName("user-type-selected");
} 


private Div createAddUserForm(UserTableType type){

    Div localAddUserForm = new Div();
        localAddUserForm.addClassName("user-add-form");

        String userType = "";
        switch (type) {
            case UserTableType.WAITER:
            userType = "Офіціанта";
            break;
            case UserTableType.ADMIN:
            userType = "Адміна";
            break;
            case UserTableType.OWNER:
            userType = "Власника";
            break;
        }

        H2 closeButton = new H2("X");
        closeButton.addClassName("close-add-user-form-button");
        closeButton.getElement().addEventListener("click", e->{
            localAddUserForm.removeClassName("show-flex");
        });
        Div closeButtonContainer = new Div(closeButton);
        closeButtonContainer.addClassName("close-button-container");
        localAddUserForm.add(closeButtonContainer);

        H2 title = new H2("Форма заповнення даних нового \n" + userType);
        title.addClassName("h2");
        localAddUserForm.add(title);

        // Ім'я
        Input nameInput = new Input();
        nameInput.setPlaceholder("Ім'я");
        
        nameInput.setClassName("input-field");
        localAddUserForm.add(nameInput);

        // Логін
        Input loginInput = new Input();
        loginInput.setPlaceholder("Логін");
      
        loginInput.setClassName("input-field");
        localAddUserForm.add(loginInput);

        // Пароль
        Input passwordInput = new Input();
        passwordInput.setPlaceholder("Пароль");
        passwordInput.setType("password");
        passwordInput.setClassName("input-field");

        Input passwordConfirmInput = new Input();
        passwordConfirmInput.setPlaceholder("Підтвердити пароль");
        passwordConfirmInput.setType("password");
       
        passwordConfirmInput.setClassName("input-field");
        localAddUserForm.add(passwordInput, passwordConfirmInput);

        if(type == UserTableType.WAITER){
        // Компетенція
        Select<String> competencySelect = new Select<>();
        competencySelect.setPlaceholder("Оберіть компетентність");
        competencySelect.setItems("Початківець",
                 "Середній Спеціаліст",
                 "Професіонал",
                 "Експерт", 
                 "Майстер");
        competencySelect.addClassNames("add-user-input-list", "input-list");
        localAddUserForm.add(competencySelect);
        }

        // Повідомлення валідації
        Span validationMessage = new Span();
        validationMessage.setClassName("validation-message");
        localAddUserForm.add(validationMessage);

        // Кнопка відправлення
        Button submitButton = new Button("Підтвердити", event -> {
            try {
                fillAndValidateUserData(this.currentRowType);
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        localAddUserForm.add(submitButton);
        return localAddUserForm;
}

private boolean isLoginUnique(String login) throws SQLException, ClassNotFoundException{
    DB_Handler.connect();
    boolean answear = DB_Handler.isLoginUnique(login);
    DB_Handler.disconnect();
    return answear;
}

private void fillAndValidateUserData(UserTableType type) throws ClassNotFoundException, SQLException{

   List<Component> dataFields = this.userAddingForm.getChildren().skip(2).toList();
   String name = ((Input)dataFields.get(0)).getValue();
   String login = ((Input)dataFields.get(1)).getValue();
   String password = ((Input)dataFields.get(2)).getValue();
   String confirmPass = ((Input)dataFields.get(3)).getValue();
   String competencyRank = "defaultly is not blank";
   Span errorMessage;

   if(type == UserTableType.WAITER){
    competencyRank = ((Select<String>)dataFields.get(4)).getValue();
    errorMessage = ((Span)dataFields.get(5));
   }
   else{
    errorMessage = ((Span)dataFields.get(4));
   }


   if(name.isBlank() || login.isBlank() || password.isBlank() || confirmPass.isBlank()){
    errorMessage.setText("Всі поля повинні бути заповнені!");
   }

   else if(competencyRank==null){
    errorMessage.setText("Оберіть компетентність офіціанта!");
   }

   else if( ! isLoginUnique(login)){
    errorMessage.setText("Логін вже зайнятий іншим користувачем.");
   }

   else if( ! password.equals(confirmPass)){
    errorMessage.setText("Пароль та підвердити пароль не співпадають.");
   }

   else{
    User user = null;
    DB_Handler.connect();
    switch (type) {
        case UserTableType.WAITER:
        String accessRight = "Офіціант";
       user = DB_Handler.addNewUser(new Waiter(name, competencyRank, new Key(login, password, accessRight)));
        break;

        case UserTableType.ADMIN:
        String accessRightAdm = "Адміністратор";
       user = DB_Handler.addNewUser(new Admin(name,  new Key(login, password, accessRightAdm)));
        break;

        case UserTableType.OWNER:
        String accessRightOw = "Власник";
       user = DB_Handler.addNewUser(new Owner(name, new Key(login, password, accessRightOw)));
        break;
    }

    DB_Handler.disconnect();
    errorMessage.setText("");
    this.userAddingForm.removeClassName("show-flex");
    addUserOnUI(user);
    
   }
}

private void addUserOnUI (User user){

    if(user != null){
        Div newUserRow = user.createTableRow();
        this.usersRows.add(newUserRow);
    }
}

public Div getUserRows(){
    return this.usersRows;
}

}