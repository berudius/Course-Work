package com.bero.views;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("main")
public class MainView extends VerticalLayout {

    public MainView(){
        Div myDiv = new Div();
        myDiv.setText("Правий клік тут!");
        myDiv.setWidth("200px");
        myDiv.setHeight("100px");
        myDiv.getStyle().set("border", "1px solid black");

        // Створюємо контекстне меню
        ContextMenu contextMenu = new ContextMenu(myDiv);
        contextMenu.setOpenOnClick(true); // Відкриваємо меню по кліку
        

        // Додаємо пункти меню
        contextMenu.addItem("Дія 1", event -> Notification.show("Вибрана дія 1"));
        contextMenu.addItem("Дія 2", event -> Notification.show("Вибрана дія 2"));
        contextMenu.addItem("Дія 3", event -> Notification.show("Вибрана дія 3"));

        add(myDiv);
    }

    
}
