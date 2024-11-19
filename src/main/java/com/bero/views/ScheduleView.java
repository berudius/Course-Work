package com.bero.views;

import java.sql.SQLException;

import com.bero.DB_entities.User;
import com.bero.views.components.ScheduleViewComponents.ScheduleTableContainer;
import com.bero.views.components.generalComponents.HeaderAndFooter;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("schedule")
@PageTitle("Schedule")
@CssImport("./themes/my-app/work-schedule-view.css")
public class ScheduleView extends VerticalLayout implements BeforeEnterObserver{
        public void beforeEnter(BeforeEnterEvent event) {
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        
        if (user == null) {
            event.forwardTo(SignInView.class);
        }
    }

    public ScheduleView() throws ClassNotFoundException, SQLException{
        FillScheduleViewByContent();
    }

        private void FillScheduleViewByContent() throws ClassNotFoundException, SQLException {
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

        ScheduleTableContainer container = new ScheduleTableContainer();
        container.fillScheduleContainer();

        H2 title = new H2("*Примітка: Для імітації зміни графіку спробуйте перевести системний годиник на наступний день та оновити сторінку");
        title.getStyle()
        .set("margin-bottom", "0")
        .set("color", "red")
        .set("font-size", "20px")
        .set("padding", "50px 50px 0px 50px");
        
        return new Main(title, container.getContainer());
    }
}
