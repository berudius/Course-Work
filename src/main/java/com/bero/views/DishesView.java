package com.bero.views;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.bero.DB_Controllers.DB_Handler;
import com.bero.DB_entities.Dish;
import com.bero.DB_entities.User;
import com.bero.views.components.DishViewComponent.DishQueryController;
import com.bero.views.components.generalComponents.HeaderAndFooter;



@Route("dishes")
@CssImport("./themes/my-app/dishes-view.css")
@PageTitle("Dishes")
public class DishesView extends VerticalLayout implements BeforeEnterObserver {

    private MemoryBuffer buffer = new MemoryBuffer();
    private Button addDishDataToDBButton;//кнопка підтвердження додавання нової стрви у формі "form-container" 
    private Button editDishDataToDBButton;//кнопка підтвердження редагування страви у формі "form-container" 
    private Main main;
    
    public DishesView() throws ClassNotFoundException, SQLException  {

        FillDishesViewByContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        
        if (user == null) {
            event.forwardTo(SignInView.class);
        }
    }

    private void FillDishesViewByContent() throws ClassNotFoundException, SQLException{
        setSizeFull();
        Header header = HeaderAndFooter.createHeader();
        
        main = createMain();
        Footer footer = HeaderAndFooter.createFooter();

        Div realBody = new Div();
        realBody.add(header, main, footer);
        realBody.addClassName("real-body");
        add(realBody);
    }



    private Main createMain() throws SQLException, ClassNotFoundException{

        Aside categories = createAndfillCategoryContainer();

        Div dishesContainer = new Div();
        dishesContainer.addClassName("dishes");

        DishQueryController.setNull();
        DishQueryController queryController = DishQueryController.getQueryController(dishesContainer);


        fillDishConteinerByDishesFromDB(dishesContainer);

        return new Main(categories, queryController.createRadioButtonContainer(), dishesContainer, (createAddDishButton()), createDishAddingForm());
    }

    private Aside createAndfillCategoryContainer() throws SQLException, ClassNotFoundException {
        Aside categories = new Aside();
        categories.addClassName("categories");

        DB_Handler.connect();
        List <String> categoriesNames = DB_Handler.getCategories();
        DB_Handler.disconnect();

        for (String categoryName : categoriesNames) {
            Div category = new Div();
            category.addClassName("category");
            category.getElement().addEventListener("click", e -> {
                setRightStyleOnCategoriesUnits(categories, category);
                
            });

            category.add(categoryName);

            categories.add(category);
        } 

        return categories;
    }

    private void setRightStyleOnCategoriesUnits(Aside categories, Div categoryUnit){
        Div previousClickedCategory = (Div)categories.getChildren()
        .filter(categoryComponent -> categoryComponent
        .hasClassName("category-clicked"))
        .findFirst()
        .orElse(null);
         
        if(previousClickedCategory != null){
            previousClickedCategory.removeClassName("category-clicked");
        }     

        categoryUnit.addClassName("category-clicked");
    }

    public void fillDishConteinerByDishesFromDB(Div dishContainer) throws SQLException, ClassNotFoundException{
        DB_Handler.connect();
        List<Dish> dishes = DB_Handler.getAllDishes();
        DB_Handler.disconnect();

        for (Dish dish : dishes) {
            dishContainer.add(createDishCardFromDbDish(dish));
        }  
    }

    public Div createAddDishButton() {
        Div plusButton = new Div("+");
        plusButton.addClassName("add-button");
    
        plusButton.getElement().addEventListener("click", e -> {
            this.getElement().executeJs("document.querySelector(\".form-container\").style.display = \"block\";");
        });
    
        return plusButton;
    }

    public Div createDishAddingForm(){

    Div formContainer = new Div();
        formContainer.setClassName("form-container");


        H2 closeButton = new H2("X");
        closeButton.addClassName("close-button");
        closeButton.getElement().addEventListener("click", e -> {
            this.getElement().executeJs("document.querySelector(\".form-container\").style.display = \"none\";");
            if (formContainer.getChildren().anyMatch(child -> child == editDishDataToDBButton)) {
                formContainer.remove(editDishDataToDBButton);
            }
            clearInputs(formContainer);
            formContainer.add(addDishDataToDBButton);
            ((H2)(formContainer.getChildren().skip(1).findFirst().get())).setText("Додати нову страву");
        });
        formContainer.add(closeButton);

        H2 title = new H2("Додати нову страву");
        formContainer.add(title);

        formContainer.add(createFormGroup("Категорія", "Введіть категорію", "category"));

        formContainer.add(createFormGroup("Назва", "Введіть назву", "name"));

        formContainer.add(createTextAreaGroup("Опис"));

        formContainer.add(createFormGroup("Ціна", "Введіть ціну", "price"));

        formContainer.add(createFileInputGroup("Зображення", "image"));


        Button submitButton = new Button("Додати продукт");
        submitButton.addClassName("submit-button");
        submitButton.getElement().addEventListener("click", e ->{
             
          Dish dish = validateAddDishFormContainer(formContainer, ValidationMode.ADDING_DISH_MODE);
          if(dish != null){
            try {
                DB_Handler.connect();
               int id = DB_Handler.AddNewDish(dish);
               DB_Handler.disconnect();
                formContainer.getElement().getStyle().set("display", "none");

               Main main = (Main) formContainer.getParent().get();
               Div dishesContainer = (Div) main.getChildren().skip(2).findFirst().get();
               
               dish.setImageFromByteImage();
               dish.setId(id);
               dishesContainer.add(createDishCardFromDbDish(dish));

            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
          }
            
        });
        
        addDishDataToDBButton = submitButton;
        
        formContainer.add(submitButton);

        add(formContainer);
        return formContainer;
    }

    public static int counyer = 0;

    public  Div createDishCardFromDbDish(Dish dbDish){

        Div dishCard = new Div();
        dishCard.addClassName("dish");
        dishCard.getElement().setAttribute("tabindex", "0");
        
        Image dishImage = dbDish.getImage();
        if(! dishImage.getSrc().equals("data:image/jpeg;base64,")){
            dishImage.getStyle().set("width", "250px");
        }
        
        H2 dishTitle = new H2(dbDish.getName());
        
        Paragraph categoryParagraph = new Paragraph("Категорія: " + dbDish.getCategory());
        Paragraph descriptionParagraph = new Paragraph(dbDish.getDescription());
        descriptionParagraph.addClassName("description");
        Paragraph priceParagraph = new Paragraph("Ціна: " + dbDish.getPrice() + " грн");

        //контексне меню(що викликається при натисненні на 3 крапки)
        ListItem edite = new ListItem("Редагувати");
        ListItem delete = new ListItem("Видалити");
        
       
        Div menu = new Div();
        delete.getElement().addEventListener("click", e ->{

         Div deletebunner = createDeleteBunner(dbDish, dishCard); 
         menu.add(deletebunner);     
        });

        edite.getElement().addEventListener("click", e -> {
            
            Div dishes = ((Div)dishCard.getParent().get());
            Main main = (Main)dishes.getParent().get();
            Div formContainer = (Div) main.getChildren().skip(4).findFirst().get();//skip 4 first brothers
            ((H2)(formContainer.getChildren().skip(1).findFirst().get())).setText("Редагувати страву");

            changeImageUpload(formContainer);
            fillFormInputsByDishData(formContainer, dishCard);
            changeButtonInAddDishFormOnEditButton(dishCard, formContainer);
            this.getElement().executeJs("document.querySelector(\".form-container\").style.display = \"block\";");
        });

        UnorderedList ul = new UnorderedList(edite, delete);
        Div contextMenu = new Div(ul);
        contextMenu.addClassName("context-menu");
        

        // Меню з трьома крапочками
        Span dots = new Span("…");
        dots.addClassName("dots");
        

        dishCard.getElement().addEventListener("focusout", e ->{
            contextMenu.removeClassName("show");
        });
        dots.getElement().addEventListener("click", e -> {

            if(contextMenu.hasClassName("show")){
                contextMenu.removeClassName("show");
            }
            else{
                contextMenu.addClassName("show");
            }
            
        });
        menu.addClassName("menu");
        menu.add(dots);
        menu.add(contextMenu);
        
        
        Span id = new Span(String.format("%d", dbDish.getId()));
        id.addClassName("id");
        
        dishCard.add(menu, dishImage, dishTitle, categoryParagraph, descriptionParagraph, priceParagraph, id);
        return dishCard;
    }

    private void fillFormInputsByDishData(Div formContainer, Div dishCard){

        
        String name = ((H2)dishCard.getChildren().skip(2).findFirst().get()).getText();
        String category = ((Paragraph)dishCard.getChildren().skip(3).findFirst().get()).getText().replace("Категорія: ", "");
        
        String description = ((Paragraph)dishCard.getChildren().skip(4).findFirst().get()).getText();
        String price = ((Paragraph)dishCard.getChildren().skip(5).findFirst().get()).getText().replace("Ціна: ", "").replace("грн", "");

        String strArr[] = {category, name, price};
        int counter[] = {0};

        formContainer.getChildren().forEach(child -> {
            if(child.getId().isPresent()){
                if(child.getId().get().equals("textAreaGroup")){
                    Span inputDescription = (Span)child.getChildren().skip(1).findFirst().get();
                    inputDescription.setText(description);
                }
            }
            else if(child.getClassName() != null && child.getClassName().equals("form-group")){

                Input input = (Input)child.getChildren().skip(1).findFirst().get();
                input.setValue(strArr[counter[0]]);
                counter[0]++;
            }
        });
    }

    private void changeButtonInAddDishFormOnEditButton(Div dishCard, Div formContainer){
        
        formContainer.remove(addDishDataToDBButton);

        Button editButton = new Button("Редагувати");
        editButton.addClassName("edit-button");
        editButton.getElement().addEventListener("click", e -> {

        
         Dish dishToUpdate = validateAddDishFormContainer(formContainer, ValidationMode.EDITING_DISH_MODE);
         if(dishToUpdate != null){
            try {

                int idCard = Integer.parseInt(((Span)dishCard.getChildren().skip(6).findFirst().get()).getText());
                dishToUpdate.setId(idCard);
                clearInputs(formContainer);
                
                DB_Handler.connect();
                DB_Handler.updateDish(dishToUpdate);
                DB_Handler.disconnect();
                boolean includeImage = dishToUpdate.getByteImage() != null ? true : false ;
                updateDishCardByDish(dishCard, dishToUpdate, includeImage);
                // updateInitialDishesContainerByDish(dishToUpdate, includeImage);
                
                this.getElement().executeJs("document.querySelector(\".form-container\").style.display = \"none\";");
                formContainer.remove(editDishDataToDBButton);
                formContainer.add(addDishDataToDBButton);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
         }
    
        });

        editDishDataToDBButton = editButton;

        formContainer.add(editButton);
    }

  private void  clearInputs(Div formContainer){
    formContainer.getChildren().forEach(child -> {
        if(child.getId().isPresent()){
            if(child.getId().get().equals("textAreaGroup")){
                Span inputDescription = (Span)child.getChildren().skip(1).findFirst().get();
                inputDescription.setText("");
            }
        }
        
        else if(child.getClassName() != null && child.getClassName().equals("form-group")){
            Input input = (Input)child.getChildren().skip(1).findFirst().get();
            input.setValue("");
        }

    });
  }


  private void changeImageUpload(Div formContainer){
    Div uploadParent = (Div)formContainer.getChildren().skip(6).findFirst().get();
    Upload currentUpload = (Upload)uploadParent.getChildren().skip(1).findFirst().get();
    
    uploadParent.remove(currentUpload);
    buffer = new MemoryBuffer(); 
    Upload newUpload = new Upload(buffer);
    uploadParent.addComponentAtIndex(1, newUpload);;

    Span validationMessage = (Span)uploadParent.getChildren().skip(2).findFirst().get();

    newUpload .addFileRemovedListener(event ->{
            validationMessage.setVisible(true);
        });
        
        newUpload.addSucceededListener(event -> {
            validationMessage.setVisible(false);
        });
    
  }

 private void updateDishCardByDish(Div dishCard, Dish dish, boolean includeImage){
    List<Component> components = dishCard.getChildren().toList();
    H2 name = (H2)components.get(2);
    Paragraph category = (Paragraph)components.get(3);
    Paragraph description = (Paragraph)components.get(4);
    Paragraph price = (Paragraph)components.get(5);

    name.setText(dish.getName());
    category.setText("Категорія: " + dish.getCategory());
    description.setText(dish.getDescription());
    price.setText("" + dish.getPrice());

    if(includeImage){
        Image image = (Image) components.get(1);
        dish.setImageFromByteImage();
        image.setSrc(dish.getImage().getSrc());
    }
 };

    private Div createDeleteBunner(Dish dbDish, Div dishСard) {

        Div deleteBanner = new Div();
        deleteBanner.addClassName("delete-banner");

        Paragraph message = new Paragraph("Ви точно хочете видалити страву " + dbDish.getName() + " із списку страв?");
        Div buttonContainer = new Div();
        buttonContainer.addClassName("button-container");
        Button buttonConfirmYes = new Button("Так");
        Button buttonConfirmNo = new Button("Ні");

        buttonConfirmYes.addClassName("delete-button");
        buttonConfirmNo.addClassName("cancel-button");

        
        buttonConfirmYes.getElement().addEventListener("click", e -> {
            try {
                DB_Handler.connect();
                if(DB_Handler.isDishInAnyOrder(dbDish.getId())){
                    Notification.show("Неможливо видалити страву допоки вона є в одному із замовлень");
                }
                else{
                    DB_Handler.deleteDishById(dbDish.getId());
                    dishСard.removeFromParent();
                }
                deleteBanner.removeFromParent();
                DB_Handler.disconnect();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
        });

        buttonConfirmNo.getElement().addEventListener("click", e -> {
            deleteBanner.removeFromParent();
        });

        buttonContainer.add(buttonConfirmYes, buttonConfirmNo);
        deleteBanner.add(message, buttonContainer);
        return deleteBanner;

    }



   private enum ValidationMode{
    ADDING_DISH_MODE,
    EDITING_DISH_MODE
   }

    //Валідація полів форми додавання нової страви
    private Dish validateAddDishFormContainer(Div formContainer, ValidationMode mode) {
        
        final int allInputsQuantity = 5;
        int[] couterInputValidationsIsOk = {0};
        Dish dish = new Dish();
    
        formContainer.getChildren().forEach(component -> {
            if (component.getClassName() != null) {
                processComponent(component, dish, couterInputValidationsIsOk, mode );
            }
        });
    
        if (couterInputValidationsIsOk[0] == allInputsQuantity) {
           return dish;
        }

        return null;
    }
    
    private void processComponent(Component component, Dish dish, int[] counter, ValidationMode mode) {
        if (component.getId().isPresent()) {
            String id = component.getId().get();
            
            if (id.equals("textAreaGroup")) {
                handleTextAreaGroup(component, dish, counter);
            } else if (id.equals("imageInputGroup")) {
                try {
                    handleImageInputGroup(component, dish, counter, mode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } 
        }

        else if(component.getClassName().equals("form-group")){
            handleFormGroup(component, dish, counter);
        }
    }
    
    private void handleTextAreaGroup(Component component, Dish dish, int[] counter) {
        List<Component> children = component.getChildren().toList();
        Span errorMessage = (Span) children.get(2); // індекс для errorMessage
        Span inputDescription = (Span) children.get(1); // input опису
    
        String innerText =  inputDescription.getText();
        if (innerText.isBlank()) {
            errorMessage.setText("Поле не може бути порожнім");
            errorMessage.setVisible(true);
        } else {
            errorMessage.setVisible(false);
            
            counter[0]++;
            dish.setDescription(innerText);
        }
    }

   
    
    private void handleImageInputGroup(Component component, Dish dish, int[] counter,  ValidationMode mode) throws IOException {
        List<Component> children = component.getChildren().toList();
        Span errorMessage = (Span) children.get(2); // індекс для errorMessage
    
        if (errorMessage.isVisible() && mode == ValidationMode.ADDING_DISH_MODE) {
            errorMessage.setText("Зображення не обрано");
        } else {
            counter[0]++;
            if (buffer.getInputStream().available() > 0){ dish.setByteImageByByteBuffer(buffer);} 
            errorMessage.setText("");
        }


    }


    
    private void handleFormGroup(Component component, Dish dish, int[] counter) {
        List<Component> children = component.getChildren().toList();
        Input input = (Input) children.get(1); // індекс для input
        Span errorMessage = (Span) children.get(2); // індекс для errorMessage
    
        if (input.getValue().isBlank()) {
            errorMessage.setText("Поле не може бути порожнім");
            errorMessage.setVisible(true);
        } else if (((Span) children.get(0)).getText().equals("Ціна") && !isNumberEntered(input)) {
            errorMessage.setText("Це поле приймає тільки числа");
            errorMessage.setVisible(true);
        } else {
            errorMessage.setVisible(false);
            
            counter[0]++;
            setDishProperty(input, dish, children);
        }
    }
    
    private void setDishProperty(Input input, Dish dish, List<Component> children) {
        String labelText = ((Span) children.get(0)).getText();
        switch (labelText) {
            case "Категорія":
                dish.setCategory(input.getValue());
                break;
            case "Назва":
                dish.setName(input.getValue());
                break;
            case "Ціна":
                dish.setPrice(Double.parseDouble(input.getValue()));
                break;
        }
    }
    
    private boolean isNumberEntered(Input input){
        try{
           double price =  Double.parseDouble(input.getValue());
           if(price < 0){
            price = price - price * 2;
            input.setValue( String.valueOf(price));
           }
            return true;
        }
        catch(NumberFormatException e){
            e.printStackTrace();
           return false; 
     
        }
        
    }


    private Div createFormGroup(String labelText, String placeholder, String inputId) {
        Div group = new Div();
        group.addClassName("form-group");

        Span label = new Span(labelText);

        Input input = new Input();
        input.setId(inputId);
        input.setPlaceholder(placeholder);

        Span validationMessage = new Span();
        validationMessage.setClassName("validation-message");
        validationMessage.setVisible(false); // Сховати за замовчуванням

        group.add(label, input, validationMessage);
        return group;
    }

    private Div createTextAreaGroup(String labelText) {
        Div group = new Div();
        group.setId("textAreaGroup");
        group.addClassName("form-group");
    
        Span label = new Span(labelText);
        group.add(label);
    
    
        Span textArea = new Span();
        textArea.getElement().setAttribute("contenteditable", true);
        textArea.getElement().setAttribute("placeholder", "Введіть текст тут");
        textArea.addClassName("custom-textarea");

        
        textArea.getElement().addEventListener("focusout", e -> {
          
           textArea.getElement().executeJs("return this.innerText;").then(text -> {
            textArea.setText(text.asString());
        });
        
        });
        group.add(textArea);
        
        Span validationMessage = new Span(); 
        validationMessage.setClassName("validation-message");
        validationMessage.setVisible(false); // Сховати за замовчуванням
        group.add(validationMessage);
        return group;
    }
    
    private Div createFileInputGroup(String labelText, String inputId) {
        Div group = new Div();
        group.setId("imageInputGroup");
        group.addClassName("form-group");

        Span label = new Span(labelText);
       

        Upload fileUpload = new Upload(buffer);

        fileUpload.setAcceptedFileTypes("image/jpeg", "image/png");
        
        

        Span validationMessage = new Span();
        validationMessage.setClassName("validation-message");
        validationMessage.setVisible(true);

        fileUpload.addFileRemovedListener(event ->{
            validationMessage.setVisible(true);
        });
        
        fileUpload.addSucceededListener(event -> {
            validationMessage.setVisible(false);
        });
       

        group.add(label, fileUpload, validationMessage);
        return group;
    }

}


