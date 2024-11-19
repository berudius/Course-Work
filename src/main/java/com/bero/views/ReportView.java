package com.bero.views;

import java.sql.SQLException;
import com.bero.DB_entities.User;
import com.bero.views.components.OrderAndReportViewComponents.OrderTable;
import com.bero.views.components.OrderAndReportViewComponents.ReportQueryController;
import com.bero.views.components.generalComponents.HeaderAndFooter;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;


@Route("reports")
@PageTitle("Reports")
@CssImport("./themes/my-app/order-view.css")
public class ReportView  extends VerticalLayout implements BeforeEnterObserver  {


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        
        if (user == null) {
            event.forwardTo(SignInView.class);
        }
    }

    public ReportView() throws ClassNotFoundException, SQLException{
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
       OrderTable orderTableConstructor = new OrderTable();
       Div reportTable = orderTableConstructor.createReportContainer();
       ReportQueryController queryController = new ReportQueryController(reportTable);
        return new Main(queryController.createRadioButtonContainer(), reportTable);
    }

}



