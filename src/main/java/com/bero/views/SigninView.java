package com.bero.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;

import java.sql.SQLException;


import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Key;
import com.bero.DB_entities.Owner;
import com.bero.DB_entities.Waiter;
import com.bero.DB_entities.Admin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;


@Route("sign-in")
@PageTitle("Ambrosia Luxe")
@CssImport("./themes/my-app/signin-form.css")
public class SigninView extends VerticalLayout {


    public SigninView() throws ClassNotFoundException, SQLException {

        setSizeFull();
        Div signInFormContainer = new Div();
        signInFormContainer.addClassName("sign-in-form-container");
        
      createSigninForm(signInFormContainer);
      DB_Handler.connect();
    }


    private void createSigninForm(Div container){
        // Div form = new Div();
        // form.addClassName("form");

        H2 label = new H2("Вхід до облікового запису");

        Span label1 = new Span("Логін");
        label1.addClassName("label");
        Input loginField = new Input();
        
        Span loginErrorShower = new Span();
        loginErrorShower.addClassName("label-error-shower");

        Span label2 = new Span("Пароль");
        label2.addClassName("label");
        Input passwordField = new Input();
        passwordField.getElement().setAttribute("type", "password");
        Span passwordErrorShower = new Span();
        passwordErrorShower.addClassName("label-error-shower");
        

        Button button = new Button("Увійти");
        button.getElement().addEventListener("click", event -> {

            Key key = validateLoginAndPassword(loginField, loginErrorShower, passwordField, passwordErrorShower );
            if(key != null){
                try{
                   String keyWord = key.getAccessRight();
                    switch (keyWord) {
                        case "Офіціант":
                        Waiter waiter = DB_Handler.getWaiterByKey(key);
                        VaadinSession.getCurrent().setAttribute("user", waiter);
                        break;

                        case "Адміністратор":
                        Admin admin = DB_Handler.getAdminByKey(key);
                        VaadinSession.getCurrent().setAttribute("user", admin);
                        break;

                        case "Власник":
                        Owner owner = DB_Handler.getOwnerByKey(key);
                        VaadinSession.getCurrent().setAttribute("user", owner);
                        break;

                        default:
                        break;
                    }

                    UI.getCurrent().navigate(DishesView.class);
                }
                catch(SQLException e){

                }
                
            }



        });

        Span forgotPass = new Span("Забули пароль?");
        forgotPass.addClassName("forgot-pass");
        forgotPass.getElement().addEventListener("click", e ->{
            
            clearContent(container);
            createForgetPassForm(container);
            
        });
        


        // Додаємо компоненти до форми
        // form.add(label, label1, loginField, label2, passwordField, button, forgotPass);
        container.add(label, label1, loginField, loginErrorShower, label2, passwordField, passwordErrorShower, button, forgotPass);
        add(container);
    }

    private Key validateLoginAndPassword(Input loginField, Span loginErrorShower
     ,Input passwordField, Span passwordErrorShower)  {

       loginErrorShower.getElement().getStyle().set("color", "");
       passwordErrorShower.getElement().getStyle().set("color", "");

       String login = loginField.getValue();
       String password = passwordField.getValue();
       loginErrorShower.removeAll();
       passwordErrorShower.removeAll();

       if(login.isBlank()){
        loginErrorShower.getElement().getStyle().set("color", "red");
        loginErrorShower.add("Це поле обов'язкове");
        return null;
       }


       else if(password.isBlank()){
        passwordErrorShower.getElement().getStyle().set("color", "red");
        passwordErrorShower.add("Це поле обов'язкове");
        return null;
       }

       else{
        Key key = DB_Handler.getKeyByLogin(login);
        
        if(key == null){
            loginErrorShower.getElement().getStyle().set("color", "red");
            loginErrorShower.add("Користувача із логіном " + login + " не існує");
            return null;
        }

    else if( ! (key.getPassword()).equals(password) ){
        passwordErrorShower.getElement().getStyle().set("color", "red");
        passwordErrorShower.add("Не правильний пароль");
        return null;
    }
    else{
        return key;
    }
       
       }

    //    else if(login.contains(" ")){
    //     loginErrorShower.getElement().getStyle().set("color", "red");
    //     loginErrorShower.add("Логін не може містити пробілів");
    //     return false;
    //    }

    //    else if(login.length() < 5){
    //     loginErrorShower.getElement().getStyle().set("color", "red");
    //     loginErrorShower.add("Мінімальна кількість символів для логіну - 5");
    //     return false;
    //    }

    //    else if(login.length() > 16){
    //     loginErrorShower.getElement().getStyle().set("color", "red");
    //     loginErrorShower.add("Максимальна кількість символів для логіну - 16");
    //     return false;
    //    }


    }


    private void createForgetPassForm(Div container){
        Image backButton = new Image("icons/backIcon.png", "");
        backButton.addClassName("back-button");
        backButton.getElement().addEventListener("click", e ->{

            clearContent(container);
            createSigninForm(container);
        });
        
        H2 label = new H2("Відновлення паролю");

        Span loginLabel = new Span("Введіть логін");
        loginLabel.addClassName("label");

        Input loginField = new Input();
        Span passwordLabel = new Span();
        passwordLabel.addClassName("label-error-shower");

        Button button = new Button("Знайти пароль");
        button.getElement().addEventListener("click", event -> {

            if(loginField.getValue().isEmpty()){}

            displayPasswordUsingLogin(loginField.getValue(), passwordLabel);

        });

        container.add(backButton, label, loginLabel, loginField, passwordLabel, button);
    }


    private void clearContent(Div container){
        container.removeAll();
    }



    private void displayPasswordUsingLogin(String login, Span passwordLable){
        passwordLable.removeAll();
        if(login.isBlank()){
            passwordLable.add("Будь ласка введіть логін");
            return;
        }


            String password = DB_Handler.getPassword(login);

            if(password != null){
                passwordLable.add("Ваш пароль: " + password);
            }
            else{
                passwordLable.add("Користувача із логіном " + login + " не знайдено");
            }



            
    }

    
}
