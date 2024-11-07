package com.bero.views.components.generalComponents;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;

public class HeaderAndFooter {
    

       public static Header createHeader (){
        H1 title = new H1("Ambrosia Luxe");

        Div nav = createNavigationBar();
        nav.addClassName("nav"); 
        
        Header header = new Header(nav, title);
        header.setId("default-anchor");
        return header;
    }

        private static Div createNavigationBar() {
        Div nav = new Div();
        UnorderedList ul = new UnorderedList();
        ul.add(new ListItem(new Anchor("", "Головна")));
        ul.add(new ListItem(new Anchor("orders/:default-anchor", "Замовлення")));
        ul.add(new ListItem(new Anchor("stuff", "Персонал")));
        ul.add(new ListItem(new Anchor("dishes", "Страви")));
        ul.add(new ListItem(new Anchor("tables", "Столики")));
        ul.add(new ListItem(new Anchor("events", "Події")));
        ul.add(new ListItem(new Anchor("work-schedule", "Робочий графік")));
        ul.add(new ListItem(new Anchor("reports", "Звіти")));

        nav.add(ul);
        return nav;
    }

        public static Footer createFooter (){
        return new Footer(new Paragraph("© 2024 Ambrosia Luxe. Усі права захищені."));
    }
}
