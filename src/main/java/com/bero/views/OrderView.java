package com.bero.views;

import java.sql.SQLException;
import com.bero.DB_entities.User;
import com.bero.views.components.OrderAndReportViewComponents.AddingOrderForm;
import com.bero.views.components.OrderAndReportViewComponents.OrderTable;
import com.bero.views.components.generalComponents.HeaderAndFooter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;

@Route("orders/:order-id")
@PageTitle("Orders")
@CssImport("./themes/my-app/order-view.css")
public class OrderView  extends VerticalLayout implements BeforeEnterObserver  {

    String anchor;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        
        if (user == null) {
            event.forwardTo(SignInView.class);
        }

        RouteParameters params = event.getRouteParameters();
        this.anchor = params.get("order-id").orElse(null);

        if (anchor != null) {
            UI.getCurrent().getPage().executeJs(
                "var el = document.getElementById($0); " +
                "if (el) { " +
                "   el.scrollIntoView({behavior: 'smooth'}); " +
                "} else { " +
                "document.getElementById($1).scrollIntoView({behavior: 'smooth'})" +
                "}", 
                anchor, "default-anchor"
            );

        }
        else{
            UI.getCurrent().getPage().executeJs(
                "document.getElementById($0).scrollIntoView({behavior: 'smooth'})", "default-anchor"
            );
        }
    }

    public OrderView() throws ClassNotFoundException, SQLException{
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
       Div orderTable = orderTableConstructor.getTablesContainer();
       Div addingOrderForm = AddingOrderForm.createAndGetAddingOrderForm();
        return new Main(addingOrderForm, orderTable, createAddDishButton());
    }

    public Div createAddDishButton() {
        Div plusButton = new Div("+");
        plusButton.addClassName("add-button");
    
        plusButton.getElement().addEventListener("click", e -> {
            try {
                Select<String> tableSelect = AddingOrderForm.getTableSelect();
                AddingOrderForm.fillTableSelect(tableSelect);
            } catch (ClassNotFoundException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            this.getElement().executeJs("document.querySelector(\".addOrderform\").classList.toggle(\"show\")");
        });
    
        return plusButton;
    }

}
