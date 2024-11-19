package com.bero.views;

import java.sql.SQLException;

import com.bero.DB_entities.User;
import com.bero.views.components.EventViewComponents.EventTable;
import com.bero.views.components.generalComponents.HeaderAndFooter;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("events")
@CssImport("./themes/my-app/event-view.css")
public class EventView extends VerticalLayout implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        
        if (user == null) {
            event.forwardTo(SignInView.class);
        }
    }

    public EventView() throws ClassNotFoundException, SQLException{
        FillOrderViewByContent();
    }

        private void FillOrderViewByContent() throws ClassNotFoundException, SQLException {
        setSizeFull();
        Header header = HeaderAndFooter.createHeader();
        Main main = createMain();
        Footer footer = HeaderAndFooter.createFooter();

        Div realBody = new Div();
        realBody.add(header, main, footer);
        realBody.addClassName("real-body");
        add(realBody);
    }
    private Main createMain() throws ClassNotFoundException, SQLException{
        EventTable.resetState();
        EventTable eventTable = EventTable.getEventTable();
        
        
        return new Main(
            eventTable.createEventTable(),
            eventTable.getOverlay(),
            eventTable.getOrderDetailsContainer()
        );
    }
}
