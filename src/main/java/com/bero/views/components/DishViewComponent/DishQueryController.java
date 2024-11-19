package com.bero.views.components.DishViewComponent;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.bero.DB_Controllers.DB_Handler;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;


public class DishQueryController {
    private static DishQueryController queryController;
    private Div enteringDiv;
    private Div radioButtonContainer;
    
    private Div dishesContainer;

    private DishQueryController(Div dishesContainer){ 
        this.dishesContainer = dishesContainer;
    }

    public static DishQueryController getQueryController(Div dishesContainer){
        if(queryController == null){
            queryController = new DishQueryController(dishesContainer);
        }

        return queryController; 
    }

    public static void setNull(){
        queryController = null;
    }

    public Div createRadioButtonContainer(){
        Div radioContainer1 = createRadioButton("1", "Запит1", "Вивести інформацію про всі гарячі страви, вартість яких не перевищує заданої\r\n" + "користувачем.");
        Div radioContainer3 = createRadioButton("3", "Запит3", "Вивести назви страв, які замовляли кожного дня більше 2-х разів.");
        Div radioContainer4 = createRadioButton("4", "Запит4", "Вивести перелік страв, які замовляли більше двох разів за кожним, столиком.(до уваги не беруться порожні столики)");
        Div radioContainer10 = createRadioButton("10", "Запит10", "Підрахувати скільки страв пропонує ресторан у кожній категорії; скласти рейтинг найбільш популярних страв закладу.");
        this.enteringDiv = new Div();
        enteringDiv.addClassName("entering");
        // Створюємо контейнер
        this.radioButtonContainer = new Div(radioContainer1, radioContainer3, radioContainer4, radioContainer10, enteringDiv);
        radioButtonContainer.addClassName("centered-div");

        return radioButtonContainer;
    }

    private Div createRadioButton(String value, String label, String hiddenMessage) {
        // Створюємо контейнер для кожного radio button
        Div radioDiv = new Div();
        radioDiv.getStyle().set("display", "flex").set("margin", "0 10px");
        
        // Створюємо елемент input типу radio
        Input radioInput = new Input();
        radioInput.getElement().setAttribute("type", "radio");
        radioInput.getElement().setAttribute("name", "query");
        radioInput.getElement().setAttribute("value", value);
        radioInput.addClassName("radio-item");
        
        // Створюємо заголовок для radio button
        Span hiddenSpan = new Span(hiddenMessage);
        Span radioLabel = new Span(label);
        radioLabel.add(hiddenSpan);

        radioLabel.addClassName("radio-label");
        hiddenSpan.addClassName("query-hidden-message");

        // Додаємо radio button і label до контейнера
        radioDiv.add(radioInput, radioLabel);

        radioInput.getElement().addEventListener("change", e->{
           resetDishesContainer();
           doAppropriateQuery(value);
        });
        
        return radioDiv;
    }

   private void doAppropriateQuery(String value){
         switch (value) {
            case "1":  doFirstQuery(); 
                break;
            case "3":  doThirdQuery();
                break;
            case "4":  doFourthQuery();
                break;
            case "10":  doTenthQuery();
                break;
         }
   }

   private void doFirstQuery(){
    enteringDiv.addClassName("show-flex");
    this.radioButtonContainer.getStyle().set("padding-bottom", "120px");
    fillEnteringDivForFirstQuery();
   }
   private void doThirdQuery(){
    enteringDiv.removeClassName("show-flex");
    this.radioButtonContainer.getStyle().set("padding-bottom", "0px");
    processThirdQuery();
   }
   private void doFourthQuery(){
    enteringDiv.removeClassName("show-flex");
    this.radioButtonContainer.getStyle().set("padding-bottom", "0px");
    processFourthQuery();
   }
   private void doTenthQuery(){
    enteringDiv.removeAll();
    enteringDiv.addClassName("show-flex");
    this.radioButtonContainer.getStyle().set("padding-bottom", "335px");
    processTenthQuery();
   }

   private boolean isDataValid(String inputValue, Span errorMessage) {
    if (inputValue == null || inputValue.isBlank()) {
        errorMessage.setText("Поле не може бути порожнім");
        return false;
    }
    
    try {
        Double.parseDouble(inputValue);  
    } catch (NumberFormatException e) {
        errorMessage.setText("Поле повинно містити тільки число");
        return false;
    }
    

    errorMessage.setText("");  
    return true;
}


   private void fillEnteringDivForFirstQuery(){
    this.enteringDiv.removeAll();
    Input priceInput = new Input();
    priceInput.setType("number");
    priceInput.setPlaceholder("Введіть ціну");
    Button button = new Button("Виконати запит");
    Span errorMessage = new Span("");
    errorMessage.getStyle().set("color", "red").set("width", "65%");
    this.enteringDiv.add(priceInput, errorMessage, button);
    
    button.getElement().addEventListener("click", e->{
        if(isDataValid(priceInput.getValue(), errorMessage)){
            displayAppropriateDishes(priceInput.getValue());
        }
    });

   }

   private void displayAppropriateDishes(String inputValue){
    // resetDishesContainer();
    double priceToFilter = Double.parseDouble(inputValue);
    String categoryToFilter = "Hot Dish";

    dishesContainer.getChildren().forEach(dishCard -> {
        int categoryPosition = 4;
        int pricePosition = 6;
        
        // Отримання абзацу з категорією
        Paragraph categoryPar = (Paragraph) dishCard.getChildren()
            .skip(categoryPosition - 1)
            .findFirst()
            .orElse(null);
        
        // Отримання абзацу з ціною
        Paragraph pricePar = (Paragraph) dishCard.getChildren()
            .skip(pricePosition - 1)
            .findFirst()
            .orElse(null);
        
        // Перевірка наявності елементів та отримання значень тексту
        if (categoryPar != null && pricePar != null) {
            String category = categoryPar.getText().replace("Категорія: ", "");
            double price = extractPrice(pricePar.getText());
    
            // Фільтрація на основі ціни та категорії
            if (price > priceToFilter || !category.equals(categoryToFilter)) {
                dishCard.addClassName("hidden");
            }
        }
    });
   }

   private void resetDishesContainer(){
    dishesContainer.getChildren().forEach(dishCard -> dishCard.removeClassName("hidden"));
   }

   public static double extractPrice(String text) {
    // Знаходимо індекс першої цифри
    int startIndex = -1;
    for (int i = 0; i < text.length(); i++) {
        if (Character.isDigit(text.charAt(i))) {
            startIndex = i;
            break;
        }
    }

    // Якщо цифри не знайдено
    if (startIndex == -1) {
        throw new IllegalArgumentException("Число не знайдено у тексті.");
    }

    // Знаходимо індекс останньої цифри або крапки
    int endIndex = startIndex;
    while (endIndex < text.length() && (Character.isDigit(text.charAt(endIndex)) || text.charAt(endIndex) == '.')) {
        endIndex++;
    }

    // Витягуємо підрядок із числом і перетворюємо його на double
    String numberStr = text.substring(startIndex, endIndex);
    return Double.parseDouble(numberStr);
}


public void processThirdQuery(){
    try{
        DB_Handler.connect();
        List<String> dishesNames = DB_Handler.getPopularDishesDaily();
        DB_Handler.disconnect();

        if(dishesNames.isEmpty()){
            Notification.show("За весь час роботи ресторану не зафіксовано жодної страви, яку замовляли більше ніж 2 рази щодня.", 10000, Position.BOTTOM_END);
        }
        else{
            String message = "Страви, що замовлялися, в дні роботи ресторану, за увесь час більше 2 разів: [";
            for (String string : dishesNames) {
                message += string + ", ";
            }
            message+="]";
            Notification.show(message, 10000, Position.BOTTOM_END);
        }
    }
    catch(SQLException | ClassNotFoundException e){
        e.printStackTrace();
    }
    
}

private void processFourthQuery(){
    try {
        DB_Handler.connect();
        List<Integer> dishesIds = DB_Handler.getDishesOrderedMoreThanTwiceAtEachTable();
        DB_Handler.disconnect();

        dishesContainer.getChildren().forEach(dishCard ->{
            int dishCardIdPosition = 7;
            String dishCardId = ((Span)dishCard.getChildren().skip(dishCardIdPosition -1).findFirst().get()).getText();
            int intCardId = Integer.parseInt(dishCardId);

            boolean idMatches = false;
            for (Integer integer : dishesIds) {
                if(integer == intCardId){ idMatches = true;}
            }
            if( ! idMatches){dishCard.addClassName("hidden");}

        });
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}

private void processTenthQuery(){
   boolean succes = displayDishesPerCategoryCountMessage();
   if(succes){ displayTopFiveDishes();}
}


private boolean displayDishesPerCategoryCountMessage(){
    try {
        DB_Handler.connect();
        Map <String, Integer> dishesPerCategory = DB_Handler.countDishesPerCategory();
        DB_Handler.disconnect();

        if(dishesPerCategory.size() == 0){
            enteringDiv.add(new Span("Страви відсутні"));
            return false;
        }
        else{
            dishesPerCategory.forEach((category, dishQuantity) ->{
                enteringDiv.add(new Span("Кількість страв категорії " + category + " сягає " + dishQuantity));
            });

            enteringDiv.add(new Span("Топ 5 страв ресторану: "));
            return true;
        }
        
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    return false;
}

private void displayTopFiveDishes(){
    try {
        DB_Handler.connect();
        Map<Integer, Integer> top5DishesAndTheirRateDegree =  DB_Handler.getTopFiveDishes();
        DB_Handler.disconnect();

        int dishCardIdPosition = 7;
        
        // First, convert the Stream to a List
        List<Component> dishCardsList = dishesContainer.getChildren()
        .collect(Collectors.toList());

        dishCardsList.sort((dishCard1, dishCard2) -> {
            int dishId1 = Integer.parseInt(((Span) dishCard1.getChildren().skip(dishCardIdPosition - 1).findFirst().get()).getText());
            int dishId2 = Integer.parseInt(((Span) dishCard2.getChildren().skip(dishCardIdPosition - 1).findFirst().get()).getText());

            Integer degreeRate1 = top5DishesAndTheirRateDegree.get(dishId1);
            Integer degreeRate2 = top5DishesAndTheirRateDegree.get(dishId2);
            if (degreeRate1 == null) { degreeRate1 = 0; }
            if (degreeRate2 == null) { degreeRate2 = 0; }

            if (degreeRate1 > degreeRate2) { return -1; }
            else if (degreeRate1 < degreeRate2) { return 1; }
            else { return 0; }
        });

        // If you want to update the Stream back into the container:
        dishesContainer.removeAll();
        dishesContainer.add(dishCardsList);


        dishesContainer.getChildren().forEach(dishCard ->{
            int dishCardNamePosition = 3;
            String dishCardId = ((Span)dishCard.getChildren().skip(dishCardIdPosition -1).findFirst().get()).getText();
            String dishCardName = ((H2)dishCard.getChildren().skip(dishCardNamePosition -1).findFirst().get()).getText();
            int intCardId = Integer.parseInt(dishCardId);
            boolean idMatches = false;
            
            for (int dishId : top5DishesAndTheirRateDegree.keySet()) {
                if(dishId == intCardId){
                    idMatches = true;
                }
            }

            if( ! idMatches){
                dishCard.addClassName("hidden");
            }
            else{
                enteringDiv.add(new Span("Рейтинговий ступінь страви " + dishCardName + " = " + top5DishesAndTheirRateDegree.get(intCardId)));
            }
            
        });
    } catch (ClassNotFoundException | SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}
}
