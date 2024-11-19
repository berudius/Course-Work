package com.bero.DB_Controllers;

import com.bero.DB_entities.Admin;
import com.bero.DB_entities.Dish;
import com.bero.DB_entities.Event;
import com.bero.DB_entities.Key;
import com.bero.DB_entities.Order;
import com.bero.DB_entities.Owner;
import com.bero.DB_entities.Report;
import com.bero.DB_entities.Table;
import com.bero.DB_entities.User;
import com.bero.DB_entities.Waiter;
import com.bero.DB_entities.WorkSchedule;
import java.util.Base64;
import java.util.LinkedHashMap;
import com.vaadin.flow.component.html.Image;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DB_Handler {
    static final String URL = "jdbc:mysql://localhost:3306/restourant_db";
    static final String USER_NAME = "root";
    static final String PASSWORD = "admin";
    static Connection connection;

    public static void connect() throws ClassNotFoundException, SQLException {
        if (connection == null || connection.isClosed()){
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        }
    }

    public static void disconnect() throws SQLException {
        if (connection != null && ! connection.isClosed()) {
            connection.close();
        }
    }
    
    public static Waiter getWaiterByKey(Key key) throws SQLException{
        String sql = "SELECT * FROM restourant_db.waiters where keyId = " + key.getId();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        result.next();
        int waiterId = result.getInt("id");
        String name = result.getString("name");
        String competencyRank = result.getString("competencyRank");

        return new Waiter(waiterId, name, competencyRank, key);
    }

    public static List<String> getWaitersNames() throws SQLException{
        String sql = "select name from restourant_db.waiters";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        List<String> waitersNames = new ArrayList<>();
        while (result.next()) {
            String name = result.getString("name");
            waitersNames.add(name);
        }

        return waitersNames;
    }
   public static Waiter getWaiterByName(String name) throws SQLException{
        String sql = "SELECT * FROM restourant_db.waiters where name = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, name);
        ResultSet result = statement.executeQuery();

        result.next();
        int waiterId = result.getInt("id");
        result.getString("name");
        String competencyRank = result.getString("competencyRank");

        return new Waiter(waiterId, name, competencyRank);
   }

   public static List<Waiter> getWaitersByTablesNumbers(List<String> tableNumbers) throws SQLException{
    String sql = "SELECT w.id, w.name, w.competencyRank, w.keyId, k.login, k.accessRight\n " + //
                "FROM restourant_db.waiters w\n " + //
                "JOIN restourant_db.keys k ON k.id = w.keyId\n " + //
                "JOIN restourant_db.orders o ON o.idWaiter = w.id\n " + //
                "JOIN restourant_db.tables t ON t.id = o.idTable\n " + //
                "WHERE t.number IN ( ";
    for (int i = 0; i < tableNumbers.size(); i++) {
        sql +="?";
        if (i < tableNumbers.size() - 1) {
            sql+=", ";
        }
    }
    sql +=")\n " + //
                "GROUP BY w.id, w.name, w.competencyRank, w.keyId, k.login, k.accessRight\n " + //
                "HAVING COUNT(DISTINCT t.number) = ?";
    
    PreparedStatement statement = connection.prepareStatement(sql);
    for (int i = 0; i < tableNumbers.size(); i++) {
        statement.setString(i+1, tableNumbers.get(i));
    }
    statement.setInt(tableNumbers.size() +1, tableNumbers.size());
    
    ResultSet result = statement.executeQuery();
    List<Waiter> waiters = new ArrayList<>();
    while (result.next()) {

        int keyId = result.getInt("keyId");
        String login = result.getString("login");
        String accessRight = result.getString("accessRight");

        int idWaiter = result.getInt("id");
        String name = result.getString("name");
        String competencyRank = result.getString("competencyRank");

        waiters.add(new Waiter(idWaiter, name, competencyRank, new Key(keyId, login,  accessRight)));
    }
    
    return waiters;
   }

    public static Admin getAdminByKey(Key key) throws SQLException{
        String sql = "SELECT * FROM restourant_db.administrators where keyId = " + key.getId();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        result.next();
        int adminId = result.getInt("id");
        String name = result.getString("name");

        return new Admin(adminId, name, key);
    }
    public static Owner getOwnerByKey(Key key) throws SQLException{
        String sql = "SELECT * FROM restourant_db.owners where keyId = " + key.getId();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        result.next();
        int ownerId = result.getInt("id");
        String name = result.getString("name");

        return new Owner(ownerId, name, key);
    }
    public static Key getKeyByLogin(String login){
        try{
            String sql = "Select * from restourant_db.keys where login = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, login);
            ResultSet result = statement.executeQuery();

            result.next();
            int id = result.getInt("id");
            String loginFromDB = result.getString("login");
            String password = result.getString("password");
            String accessRight = result.getString("accessRight");

            return new Key(id,loginFromDB, password, accessRight);
        }
        catch(SQLException e){
            return null;
        }  
    }

    public static boolean isLoginUnique(String login) throws SQLException{
        String sql = "Select login from restourant_db.keys where login =  \""+ login + "\"";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        if(result.next()){
            return false;
        }

        return true;
    }

    public static String getPassword(String login){
        try{
            String sql = "Select password from restourant_db.keys where login = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, login);
            ResultSet result = statement.executeQuery();

            result.next();
            String password = result.getString("password");

            return password;
        }
        catch(SQLException e){
            return null;
        }   
    }

    public static List<Dish> getAllDishes() throws SQLException{

        String sql = "Select * from restourant_db.dishes";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        List<Dish> dishes = new ArrayList<>();
        while (result.next()) {

            int id = result.getInt("id");
            String category = result.getString("category");
            String name = result.getString("name");;
            String description = result.getString("description");;
            double price = result.getDouble("price");

            byte[] image = result.getBytes("image");
            // перетворення байтів зображення в base64
            String base64 = Base64.getEncoder().encodeToString(image);
            String imageUrl = "data:image/jpeg;base64," + base64; // формат зображення




            dishes.add(new Dish(id,category, name, description, price, new Image(imageUrl, "")));
        }

        return dishes;
    }
    public static List<Dish> getAllDishesWithNoImage() throws SQLException{

        String sql = "Select id,category, name, description, price from restourant_db.dishes";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        List<Dish> dishes = new ArrayList<>();
        while (result.next()) {

            int id = result.getInt("id");
            String category = result.getString("category");
            String name = result.getString("name");;
            String description = result.getString("description");;
            double price = result.getDouble("price");

            dishes.add(new Dish(id,category, name, description, price, 1));
        }

        return dishes;
    }

    public static void deleteDishById(int id) {
        String sql = "DELETE FROM restourant_db.dishes WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static boolean isDishInAnyOrder(int dishId) throws SQLException{
        String sql = "SELECT * FROM restourant_db.dish_list where idDish = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, dishId);
        ResultSet result = statement.executeQuery();
        if(result.next()){
            return true;
        }

        return false;
    }

   public static int AddNewDish(Dish dish){
    String sql = "Insert into restourant_db.dishes (category, name, description, price, image) value(?,?,?,?,?)";
    try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        statement.setString(1, dish.getCategory());
        statement.setString(2, dish.getName());
        statement.setString(3, dish.getDescription());
        statement.setDouble(4, dish.getPrice());
        statement.setBytes(5, dish.getByteImage());
        statement.executeUpdate();

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1); // Повертаємо ID
            } else {
                throw new SQLException("Creating dish failed, no ID obtained.");
            }
        }
        }
        
    
    catch(SQLException e){
        e.printStackTrace();
        return -1;
    }
   }

   public static void updateTable(Table table){
    try{
        String sql = "update restourant_db.tables set number = ?, capacity = ? where id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, table.getNumber());
        statement.setInt(2, table.getCapacity());
        statement.setInt(3, table.getId());
        statement.executeUpdate();
    }
    catch(SQLException e){

    }
    
   }

   public static void updateDish(Dish dish){

    try{
        if(dish.getByteImage() != null){
            String sql = "UPDATE restourant_db.dishes \n" + //
                                "SET category = ?, \n" + //
                                "    name = ?, \n" + //
                                "    description = ?, \n" + //
                                "    price = ?, \n" + //
                                "    image = ?" + //
                                "WHERE id = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, dish.getCategory());
            statement.setString(2, dish.getName());
            statement.setString(3, dish.getDescription());
            statement.setDouble(4, dish.getPrice());
            statement.setBytes(5, dish.getByteImage());
            statement.setInt(6, dish.getId());
            statement.executeUpdate();
        }

        else{
            String sql = "UPDATE restourant_db.dishes \n" + //
                         "SET category = ?, \n" + //
                         "    name = ?, \n" + //
                         "    description = ?, \n" + //
                         "    price = ? \n" + //
                         "WHERE id = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, dish.getCategory());
            statement.setString(2, dish.getName());
            statement.setString(3, dish.getDescription());
            statement.setDouble(4, dish.getPrice());
            statement.setInt(5, dish.getId());
            statement.executeUpdate();
        }

        
    }catch(SQLException e){
        e.printStackTrace();
    }
   }

   public static List<String> getCategories() throws SQLException{
    String sql = "SELECT distinct category FROM restourant_db.dishes;";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<String> categories = new ArrayList<>();
    while (result.next()) {
        categories.add(result.getString("category"));
    }

    categories.add(0, "Всі страви");

    return categories; 
   }

  public static List<Waiter> getAllWaiters() throws SQLException{
    String sql = "select w.id, name, competencyRank, keyId, login, password, accessRight from restourant_db.waiters w\n" + //
                "join restourant_db.keys k on k.id = w.keyId";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Waiter> waiters = new ArrayList<>();
    while (result.next()) {

        int keyId = result.getInt("keyId");
        String login = result.getString("login");
        String password = result.getString("password");
        String accessRight = result.getString("accessRight");

        int idWaiter = result.getInt("id");
        String name = result.getString("name");
        String competencyRank = result.getString("competencyRank");

        waiters.add(new Waiter(idWaiter, name, competencyRank, new Key(keyId, login, password, accessRight)));
    }
    
    return waiters;
  }

  public static List<Admin> getAllAdmins() throws SQLException{
    String sql = "select a.id, name, keyId, login, password, accessRight from restourant_db.administrators a\n" + //
                "join restourant_db.keys k on k.id = a.keyId";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Admin> admins = new ArrayList<>();
    while (result.next()) {

        int keyId = result.getInt("keyId");
        String login = result.getString("login");
        String passsword = result.getString("password");
        String accessRight = result.getString("accessRight");

        int idAdmin = result.getInt("id");
        String name = result.getString("name");

        admins.add(new Admin(idAdmin, name, new Key(keyId, login, passsword, accessRight)));
    }
    
    return admins;
  }

  public static List<Owner> getAllOwners() throws SQLException{
    String sql = "select o.id, name, keyId, login, password, accessRight from restourant_db.owners o\n" + //
                "join restourant_db.keys k on k.id = o.keyId";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Owner> owners = new ArrayList<>();
    while (result.next()) {

        int keyId = result.getInt("keyId");
        String login = result.getString("login");
        String passwrord = result.getString("password");
        String accessRight = result.getString("accessRight");

        int idOwner = result.getInt("id");
        String name = result.getString("name");

        owners.add(new Owner(idOwner, name, new Key(keyId, login,passwrord, accessRight)));
    }
    
    return owners;
  }

  public static User addNewUser(User user) throws SQLException{
    String className = user.getClass().getName();
    switch (className) {
        case "com.bero.DB_entities.Waiter":
           return createWaiter((Waiter)user);

        case "com.bero.DB_entities.Admin": 
           return createAdmin((Admin)user);

        case "com.bero.DB_entities.Owner": 
           return createOwner((Owner)user);
    }

    return null;
  }

  private static int createKey(Key key) throws SQLException{
    String sql = "Insert into restourant_db.keys (login, password, accessRight) value (?, ?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    statement.setString(1, key.getLogin());
    statement.setString(2, key.getPassword());
    statement.setString(3, key.getAccessRight());
     statement.executeUpdate();

     ResultSet generatedKeys = statement.getGeneratedKeys();
     generatedKeys.next();
     int keyId = generatedKeys.getInt(1);

    return keyId;
  }

  private static User createWaiter (Waiter waiter) throws SQLException{


        int  keyId = createKey(waiter.getKey());

    

    String sql = "Insert into restourant_db.waiters (name, competencyRank, keyId) value (?, ?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
     statement.setString(1, waiter.getName());
     statement.setString(2, waiter.getCompetencyRank());
     statement.setInt(3, keyId);
     statement.executeUpdate();

    ResultSet generatedKeys = statement.getGeneratedKeys();
    generatedKeys.next();
    int waiterId = generatedKeys.getInt(1);
    waiter.getKey().setId(keyId);
    waiter.setId(waiterId);

     return waiter;
  }

  private static User createAdmin (Admin admin) throws SQLException{


       int keyId = createKey(admin.getKey());



    String sql = "Insert into restourant_db.administrators (name, keyId) value (?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
     statement.setString(1, admin.getName());
     statement.setInt(2, keyId);
     statement.executeUpdate();

    ResultSet generatedKeys = statement.getGeneratedKeys();
     generatedKeys.next();
     int adminId = generatedKeys.getInt(1);
     admin.getKey().setId(keyId);
     admin.setId(adminId);

     return admin;
  }

  private static User createOwner (Owner owner) throws SQLException{

    int keyId = createKey(owner.getKey());

    String sql = "Insert into restourant_db.owners (name, keyId) value (?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
     statement.setString(1, owner.getName());
     statement.setInt(2, keyId);
     statement.executeUpdate();

    ResultSet generatedKeys = statement.getGeneratedKeys();
     generatedKeys.next();
     int ownerId = generatedKeys.getInt(1);
     owner.getKey().setId(keyId);
     owner.setId(ownerId);

     return owner;
  }

  public static void removeUser(User user) throws SQLException{
    String className = user.getClass().getName();
    switch (className) {
        case "com.bero.DB_entities.Waiter":
        removeWaiter(user);

        case "com.bero.DB_entities.Admin": 
         removeAdmin(user);

        case "com.bero.DB_entities.Owner": 
        removeOwner(user);
    }
  }

  public static void removeWaiter(User waiter) throws SQLException{
    String sql = "delete from restourant_db.waiters where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, waiter.getId());
    statement.executeUpdate();
  }

  public static void removeAdmin(User admin) throws SQLException{
    String sql = "delete from restourant_db.administrators where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, admin.getId());
    statement.executeUpdate();
  }

  public static void removeOwner(User owner) throws SQLException{
    String sql = "delete from restourant_db.owners where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, owner.getId());
    statement.executeUpdate();
  }
  
   public static void removeUserByKeyId(int keyId) throws SQLException{
    String sql = "delete from restourant_db.keys where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, keyId);
    statement.executeUpdate();
   }

   public static void updateUser(User user) throws SQLException{
    String className = user.getClass().getName();
    switch (className) {
        case "com.bero.DB_entities.Waiter":
        updateWaiter(user);

        case "com.bero.DB_entities.Admin": 
         updateAdmin(user);

        case "com.bero.DB_entities.Owner": 
        updateOwner(user);
    }

  }

  public static void updateKey(Key key) throws SQLException{
    String sql  = "update restourant_db.keys set login = ?, password = ?, accessRight = ? where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, key.getLogin());
    statement.setString(2, key.getPassword());
    statement.setString(3, key.getAccessRight());
    statement.setInt(4, key.getId());
    statement.executeUpdate();
  }
  
  private static void updateWaiter(User waiter) throws SQLException{
    updateKey(waiter.getKey());

   String sql  = "update restourant_db.waiters set name = ?, competencyRank = ? where id = ?";
   PreparedStatement statement = connection.prepareStatement(sql);
   statement.setString(1, waiter.getName());
   statement.setString(2, waiter.getCompetencyRank());
   statement.setInt(3, waiter.getId());
   statement.executeUpdate();
  }

  private static void updateAdmin(User admin) throws SQLException{

    updateKey(admin.getKey());

   String sql  = "update restourant_db.administrators set name = ?  where id = ?";
   PreparedStatement statement = connection.prepareStatement(sql);
   statement.setString(1, admin.getName());
   statement.setInt(2, admin.getId());
   statement.executeUpdate();
    
  }
  private static void updateOwner(User owner) throws SQLException{
    updateKey(owner.getKey());

   String sql  = "update restourant_db.owners set name = ?  where id = ?";
   PreparedStatement statement = connection.prepareStatement(sql);
   statement.setString(1, owner.getName());
   statement.setInt(2, owner.getId());
   statement.executeUpdate();
  }

  public static boolean hasWaiterOrder(int idWaiter) throws SQLException{
    String sql = "select idWaiter from restourant_db.orders where idWaiter = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, idWaiter);
    ResultSet result = statement.executeQuery();

    if(result.next()){
        return true;
    }

    return false;
  }

  public static boolean hasWaiterReport(int idWaiter) throws SQLException{
    String sql = "select idWaiter from restourant_db.reports where idWaiter = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, idWaiter);
    ResultSet result = statement.executeQuery();

    if(result.next()){
        return true;
    }

    return false;
  }

  public static List<Table> getAllTables() throws SQLException{
    String sql = "SELECT * FROM restourant_db.tables";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Table> tables = new ArrayList<>();
    while (result.next()) {
        int idTable = result.getInt("id");
        String number = result.getString("number");
        int capacity = result.getInt("capacity");

        tables.add(new Table(idTable, number, capacity));
    }
    return tables;
  }
  public static List<Table> getAllFreeTables() throws SQLException{
    String sql = "SELECT * FROM restourant_db.tables \n" + //
                "where id not in (select idTable from restourant_db.orders)";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Table> tables = new ArrayList<>();
    while (result.next()) {
        int idTable = result.getInt("id");
        String number = result.getString("number");
        int capacity = result.getInt("capacity");

        tables.add(new Table(idTable, number, capacity));
    }
    return tables;
  }

  public static List<String> getAllNotFreeTablesNumbers() throws SQLException{
    List<Table> tables = getAllNotFreeTables();
    List<String> tablesNumbers = new ArrayList<>();
    for (Table table : tables) {
        tablesNumbers.add(table.getNumber());
    }
    return tablesNumbers;
  }

  public static List<Table> getAllNotFreeTables() throws SQLException{
    String sql = "SELECT * FROM restourant_db.tables \n" + //
                "where id in (select idTable from restourant_db.orders)";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Table> tables = new ArrayList<>();
    while (result.next()) {
        int idTable = result.getInt("id");
        String number = result.getString("number");
        int capacity = result.getInt("capacity");

        tables.add(new Table(idTable, number, capacity));
    }
    return tables;
  }

  public static List<String> getTablesNumbersFromReportsMatchingDate(LocalDate date) throws SQLException{
    String sql = "SELECT distinct number FROM restourant_db.reports r\n" + //
                "join restourant_db.tables t on idTable = t.id\n" + //
                "where date(dateTime) = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    Date convertedDate = Date.valueOf(date);
    statement.setDate(1, convertedDate);
    ResultSet result = statement.executeQuery();

    List<String> tablesNumbers = new ArrayList<>();
    while (result.next()) {
        String tableNumber = result.getString("number");
        tablesNumbers.add(tableNumber);
    }

    return tablesNumbers;
  }

  public static double getMaxSumForTableAtDate(LocalDate date, String tableNumber) throws SQLException{
    String sql = "SELECT idReport, number, sum((quantity * price)) as tableGeneralPrice, date(dateTime) FROM restourant_db.reports r\n" + //
                "join restourant_db.tables t on idTable = t.id\n" + //
                "join restourant_db.dish_list_history dlh on dlh.idReport = r.id\n" + //
                "join restourant_db.dishes d on dlh.idDish = d.id\n" + //
                "where date(dateTime) = ? and number = ?\n" + //
                "group by idReport\n" + //
                "order by tableGeneralPrice desc\n" + //
                "limit 1";
    PreparedStatement statement = connection.prepareStatement(sql);
    Date convertedDate = Date.valueOf(date);
    statement.setDate(1, convertedDate);
    statement.setString(2, tableNumber);
    ResultSet result = statement.executeQuery();

    if(result.next()){
        return result.getDouble("tableGeneralPrice");
    }
    return 0;
  }

   public static void addTable(Table table) throws SQLException{
    String sql = "insert into restourant_db.tables (number, capacity) value (?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
    statement.setString(1, table.getNumber());
    statement.setInt(2, table.getCapacity());
    statement.executeUpdate();

    ResultSet result = statement.getGeneratedKeys();
    result.next();
    int idTable = result.getInt(1);

    table.setId(idTable);
   }

  public static List<Order> getAllOrders() throws SQLException{
    String sql = "select dl.idOrder, dl.idDish, d.category, d.name as dishName, d.description, dl.quantity, d.price, o.idTable, t.number, t.capacity, o.idWaiter, w.name as waiterName, o.dateTime from restourant_db.orders o join restourant_db.dish_list dl on dl.idOrder = o.id join restourant_db.dishes d on d.id = dl.idDish join restourant_db.waiters w on w.id = o.idWaiter join restourant_db.tables t on t.id = o.idTable order by idOrder desc;";

    PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    ResultSet result = statement.executeQuery();

    List<Order> orders = new ArrayList<>();


    while (result.next()) {

        int idTable = result.getInt("idTable");
        String tableNumber = result.getString("number");
        int capacity = result.getInt("capacity");

        int idWaiter = result.getInt("idWaiter");
        String waiterName = result.getString("waiterName");

        Timestamp time = result.getTimestamp("dateTime");
        LocalDateTime dateTime = time.toLocalDateTime();

        
        List<Dish> dishes = new ArrayList<>();
        int orderId;
        boolean iWasInTheLoop = false;
        do {
            orderId = result.getInt("idOrder");
            int idDish = result.getInt("idDish");
            String category = result.getString("category");
            String dishName = result.getString("dishName");
            String description = result.getString("description");
            int quantity = result.getInt("quantity");
            double price = result.getDouble("price");

            dishes.add(new Dish(idDish, category, dishName, description, price, quantity));

            iWasInTheLoop = true;
        } while (result.next() && orderId == result.getInt("idOrder"));

        orders.add(new Order(orderId, new Table(idTable, tableNumber, capacity), new Waiter(idWaiter, waiterName), dishes, dateTime));
        if(iWasInTheLoop){
            result.previous();
        }
        
    }

    return orders;
  }

  public static int addNewOrder (Order order) throws SQLException {
    String sql = "Insert into restourant_db.orders (idWaiter, idTable, dateTime) value (?,?,?)";
    PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
    statement.setInt(1, order.getWaiter().getId());
    statement.setInt(2, order.getTable().getId());

    Timestamp timestamp = Timestamp.valueOf(order.getDateTime());
    statement.setTimestamp(3, timestamp);
    statement.executeUpdate();

    ResultSet generatedKeys = statement.getGeneratedKeys();
    generatedKeys.next();
    int orderId = generatedKeys.getInt(1);
    
    List<Dish> orderedDishes = order.getDishes();
    for (Dish dish : orderedDishes) {
        addDishToOrder(orderId, dish.getId(), dish.getQuantity());
    }

    return orderId;
  }

  public static void addDishToOrder(int idOrder, int idDish, int quantity) throws SQLException{
    String sql = "Insert into restourant_db.dish_list (idOrder, idDish, quantity) value (?, ?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, idOrder);
    statement.setInt(2, idDish);
    statement.setInt(3, quantity);
    
    statement.executeUpdate();
  }

  public static void removeDishFromOrder(int orderId, int dishId) throws SQLException{
    String sql = "delete from restourant_db.dish_list where idOrder = ? and idDish = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, orderId);
    statement.setInt(2, dishId);

    statement.executeUpdate();
  }

  public static void removeOrderById(int orderId) throws SQLException{
    String sql = "delete from restourant_db.orders where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, orderId);
    statement.executeUpdate();
  }

  public static List<Report> getAllReports() throws SQLException{
        String sql = "select dlh.idReport, dlh.idDish, d.category, d.name as dishName, d.description, dlh.quantity, d.price, r.idTable, t.number, t.capacity, r.idWaiter, w.name as waiterName, r.dateTime from restourant_db.reports r\n" + //
                    "join restourant_db.dish_list_history dlh on dlh.idReport = r.id \n" + //
                    "join restourant_db.dishes d on d.id = dlh.idDish \n" + //
                    "join restourant_db.waiters w on w.id = r.idWaiter \n" + //
                    "join restourant_db.tables t on t.id = r.idTable \n" + //
                    "order by idReport desc;";
    
        PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet result = statement.executeQuery();
    
        List<Report> reports = new ArrayList<>();
        while (result.next()) {
    
            int idTable = result.getInt("idTable");
            String tableNumber = result.getString("number");
            int capacity = result.getInt("capacity");
    
            int idWaiter = result.getInt("idWaiter");
            String waiterName = result.getString("waiterName");
    
            Timestamp time = result.getTimestamp("dateTime");
            LocalDateTime dateTime = time.toLocalDateTime();
    
            
            List<Dish> dishes = new ArrayList<>();
            int reportId;
            boolean iWasInTheLoop = false;
            do {
                reportId = result.getInt("idReport");
                int idDish = result.getInt("idDish");
                String category = result.getString("category");
                String dishName = result.getString("dishName");
                String description = result.getString("description");
                int quantity = result.getInt("quantity");
                double price = result.getDouble("price");
    
                dishes.add(new Dish(idDish, category, dishName, description, price, quantity));
    
                iWasInTheLoop = true;
            } while (result.next() && reportId == result.getInt("idReport"));
    
            reports.add(new Report(reportId, new Table(idTable, tableNumber, capacity), new Waiter(idWaiter, waiterName), dishes, dateTime));
            if(iWasInTheLoop){
                result.previous();
            }
            
        }
    
        return reports;
      
  }

  public static void removeReportById(int reportId) throws SQLException{
    String sql = "delete from restourant_db.reports where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, reportId);
    statement.executeUpdate();
  }

  public static void removeTableById(int idTable) throws SQLException{
        String sql = "delete from restourant_db.tables where id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, idTable);
        statement.executeUpdate();
  }

public static void updateWaiterInOrder(int waiterId, int orderId) throws SQLException {
    String sql = "update restourant_db.orders set idWaiter = ? where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, waiterId);
    statement.setInt(2, orderId);
    statement.executeUpdate();
}
public static void updateTableInOrder(int tableId, int orderId) throws SQLException {
    String sql = "update restourant_db.orders set idTable = ? where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, tableId);
    statement.setInt(2, orderId);
    statement.executeUpdate();
}

public static void updateOrderDishQuantity(int idOrder, int idDish, int quantity) throws SQLException{
    String sql = "update restourant_db.dish_list set quantity = ? where idOrder = ? and idDish = ? ";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, quantity);
    statement.setInt(2, idOrder);
    statement.setInt(3, idDish);

    statement.executeUpdate();
}

public static boolean hasTableOrders(int idTable) throws SQLException{
    String sql = "select idTable from restourant_db.orders where idTable = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, idTable);
    ResultSet result = statement.executeQuery();

    if (result.next()) {
        return true;
    }

    return false;
}

public static boolean checkUnickTableNumber(String tableNumber) throws SQLException{
    String sql = "select number from restourant_db.tables where number = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, tableNumber);
    ResultSet result = statement.executeQuery();
    if(result.next()){
        return false;
    }

    return true;
}

public static void saveExecutedOrder(int orderId) throws SQLException, ClassNotFoundException{
    if(connection.isClosed()){
        connect();
    }
    String sql = "INSERT INTO restourant_db.reports (id, idWaiter, idTable, dateTime) " +
        "SELECT id, idWaiter, idTable, dateTime FROM restourant_db.orders WHERE id = ?";
    PreparedStatement insertHistory = connection.prepareStatement(sql);
    insertHistory.setInt(1, orderId); 
    insertHistory.executeUpdate();


// 2. Вставка даних в таблицю dish_list_history
   sql = "INSERT INTO restourant_db.dish_list_history (idReport, idDish, quantity) " +
   "SELECT idOrder, idDish, quantity FROM restourant_db.dish_list WHERE idOrder = ?";
    PreparedStatement insertDishListHistory = connection.prepareStatement(sql);
    insertDishListHistory.setInt(1, orderId);  
    insertDishListHistory.executeUpdate();

}




public static Order getOrderById(int orderId) throws SQLException{
    String sql = "select dl.idOrder, dl.idDish, d.category, d.name as dishName, d.description, dl.quantity, d.price, o.idTable, t.number, t.capacity, o.idWaiter, w.name as waiterName, o.dateTime from restourant_db.orders o join restourant_db.dish_list dl on dl.idOrder = o.id join restourant_db.dishes d on d.id = dl.idDish join restourant_db.waiters w on w.id = o.idWaiter join restourant_db.tables t on t.id = o.idTable where idOrder = ? order by idOrder;";
    PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
    statement.setInt(1, orderId);
    ResultSet result = statement.executeQuery();

        List<Dish> dishes = new ArrayList<>();
        Waiter waiter = new Waiter();
        Table table = new Table();
        LocalDateTime dateTime = LocalDateTime.now();

        while (result.next()) { 
            int idTable = result.getInt("idTable");
            String tableNumber = result.getString("number");
            int capacity = result.getInt("capacity");

            int idWaiter = result.getInt("idWaiter");
            String waiterName = result.getString("waiterName");

            Timestamp time = result.getTimestamp("dateTime");
            dateTime = time.toLocalDateTime();

            int idDish = result.getInt("idDish");
            String category = result.getString("category");
            String dishName = result.getString("dishName");
            String description = result.getString("description");
            int quantity = result.getInt("quantity");
            double price = result.getDouble("price");

            table = new Table(idTable, tableNumber, capacity);
            waiter = new Waiter(idWaiter, waiterName);
            dishes.add(new Dish(idDish, category, dishName, description, price, quantity));
        }

        Order order = new Order(orderId, table, waiter, dishes, dateTime);

        return order;
    }

   public static Order getOrderByTableId(int tableId) throws SQLException{
    String sql = "select id from restourant_db.orders where idTable = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, tableId);
    ResultSet result = statement.executeQuery();

    Order order = new Order(null, null, null, null);
    if(result.next()){
        int orderId = result.getInt("id");
        order = getOrderById(orderId);
    }

    return order;
   }



    public static List<Event> getAllEvents() throws SQLException{
        String sql = "select * from restourant_db.events";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        List<Event> events = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");

            Timestamp timeStamp = resultSet.getTimestamp("eventDateTime");
            LocalDateTime dateTime = timeStamp.toLocalDateTime();

            String eventType = resultSet.getString("event_type");
            events.add(new Event(id, dateTime, eventType));
        }
        return events;
    }

public static List<Order> getEventOrders(int idEvent) throws SQLException{
    String sql = "SELECT ol.idOrder " + 
                 "FROM restourant_db.events e " +
                 "Join restourant_db.order_list ol on ol.idEvent = e.id "+
                 "join restourant_db.orders o on ol.idOrder = o.id " +
                 "where idEvent = ? ";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, idEvent);
    ResultSet result = statement.executeQuery();

    
    List<Order> orders = new ArrayList<>();
    while (result.next()) {

        int orderId = result.getInt("idOrder");
        orders.add(getOrderById(orderId));
    }

    return orders;
}

public static void removeEventById(int eventId) throws SQLException{
    String sql = "delete from restourant_db.events where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, eventId);
    statement.executeUpdate();
   }

   public static void addEvent(Event event) throws SQLException{
    String sql = "insert into restourant_db.events (eventDateTime, event_type) value (?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
    Timestamp timestamp = Timestamp.valueOf( event.getDateTime());
    statement.setTimestamp(1, timestamp);
    statement.setString(2, event.getEventType());
    statement.executeUpdate();

    ResultSet result = statement.getGeneratedKeys();
    result.next();
    int eventId = result.getInt(1);
    event.setId(eventId);
   }

   public static void addEventOrder(int orderId, int eventId) throws SQLException{
    String sql = "insert into restourant_db.order_list (idEvent, idOrder) value (?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, eventId);
    statement.setInt(2, orderId);
    statement.executeUpdate();
   }

   public static void removeOrderFromEvent(int orderId) throws SQLException{
    String sql = "delete from restourant_db.order_list where idOrder = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, orderId);
    statement.executeUpdate();
   }

   public static List<Table> getMissedEventTables(int eventId) throws SQLException{
    String sql = "SELECT * FROM restourant_db.tables "+
    "WHERE id NOT IN " + 
        "(SELECT idTable FROM restourant_db.order_list ol " +
        "JOIN restourant_db.orders o ON o.id = ol.idOrder) " +
    "and id IN " +
        "(SELECT idTable FROM restourant_db.orders) " +
    "ORDER BY id ";

    PreparedStatement statement  = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Table> tables = new ArrayList<>();
    while (result.next()) {
        int id = result.getInt("id");
        String number = result.getString("number");

        tables.add(new Table(id, number));
    }
    return tables;
   }

  public static Table getTableByNumber(String tablenNumber) throws SQLException{
    String sql = "Select * from restourant_db.tables where number = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, tablenNumber);
    ResultSet result = statement.executeQuery();

    if(result.next()){
        int id = result.getInt("id");
        String number = result.getString("number");
        int capacity = result.getInt("capacity");

        return new Table(id, number, capacity);
    }

    
    return null;
   }

  public static void updateEventDateTime(LocalDateTime dateTime, int eventId) throws SQLException{
    String sql = "update restourant_db.events set eventDateTime = ? where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);

    Timestamp timestamp = Timestamp.valueOf(dateTime);
    statement.setTimestamp(1, timestamp);
    statement.setInt(2, eventId);
    statement.executeUpdate();
  }

  public static void updateEventType(String eventType, int eventId) throws SQLException{
    String sql = "update restourant_db.events set event_type = ? where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, eventType);
    statement.setInt(2, eventId);
    statement.executeUpdate();
  }

  private static Event getEventById(int idEvent) throws SQLException {
    String sql = "Select * from restourant_db.events where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, idEvent);
    ResultSet result = statement.executeQuery();
    if(result.next()){
        int id = result.getInt("id");
        String eventType = result.getString("event_type");
        Timestamp timestamp = result.getTimestamp("eventDateTime");
        LocalDateTime dateTime = timestamp.toLocalDateTime();

        return new Event(id, dateTime, eventType);
    }
    return null;
}

  public static LocalDate getLastUpdatedRecordDate() throws SQLException{
    String sql = "SELECT * FROM restourant_db.static_last_updated_date_value_of_work_schedule";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();
    
    if(result.next()){
        Timestamp timestamp = result.getTimestamp("static_last_updated_date_value");
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        LocalDate date = dateTime.toLocalDate();
        return date;
    }

    return null;
  }

  public static List<WorkSchedule> getWorkScheduleRecords() throws SQLException{
    String sql ="SELECT ws.id, ws.idWaiter, w.name, stateOne, stateTwo, stateThree, stateFour, stateFive, stateSix, stateSeven " +
                "FROM restourant_db.work_schedule ws  " +
                "join restourant_db.waiters w on ws.idWaiter = w.id";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<WorkSchedule> records = new ArrayList<>();
    LocalDate date = getLastUpdatedRecordDate();
    WorkSchedule.setLastUpdatedRecordDate(date);
    while (result.next()) {
        int id = result.getInt("id");
        String waiterName = result.getString("name");
        String states [] = {
            result.getString("stateOne"),
            result.getString("stateTwo"),
            result.getString("stateThree"),
            result.getString("stateFour"),
            result.getString("stateFive"),
            result.getString("stateSix"),
            result.getString("stateSeven"),
        };

        records.add(new WorkSchedule(id, waiterName, states));
    }
    return records;
  }

  public static void saveStates( String[] states, int recordId) throws SQLException{
   String stateTitles[] = {"stateOne", "stateTwo", "stateThree", "stateFour", "stateFive", "stateSix", "stateSeven"};
   for (int i = 0; i < states.length; i++) {
    String sql = "Update restourant_db.work_schedule set " + stateTitles[i] + " = ? where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, states[i]);
    statement.setInt(2, recordId);
    statement.executeUpdate();
   }

   updateStaticRecordDate();
  }

  public static void updateStaticRecordDate() throws SQLException{
   String sql = "update restourant_db.static_last_updated_date_value_of_work_schedule " +
                "set static_last_updated_date_value = ?";
    
    LocalDate localDate = LocalDate.now();
    Date date = Date.valueOf(localDate);

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setDate(1, date);
    statement.executeUpdate();
  }

  public static List<Waiter> getAllWaiterNotInWorkSchedule() throws SQLException{
    String sql = "SELECT id, name FROM restourant_db.waiters where id not in(SELECT idWaiter  FROM restourant_db.work_schedule)";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();

    List<Waiter> waiters = new ArrayList<>();
    while (result.next()) {
        int idWaiter = result.getInt("id");
        String waiterName = result.getString("name");
        waiters.add(new Waiter(idWaiter, waiterName));
    }

    return waiters;
  }

 public static void addNewScheduleRecord(WorkSchedule record) throws SQLException{
    String states[] =  record.getStates();
    String sql = "Insert into restourant_db.work_schedule " + 
                "(idWaiter, stateOne, stateTwo, stateThree, stateFour, stateFive, stateSix, stateSeven) " + 
                "value (?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
    statement.setInt(1, record.getWaiterId());
    for (int i = 0; i < states.length; i++) {
        statement.setString(i+2, states[i]);
    }
    statement.executeUpdate();

   ResultSet result = statement.getGeneratedKeys();
   if(result.next()){
    int recordId = result.getInt(1);
    record.setId(recordId);
   }
  
 }

 public static void updateScheduleRecord(WorkSchedule record) throws SQLException{
    String sql = "update restourant_db.work_schedule " +
                 "set idWaiter = ?, " +
                  "stateOne = ?, " + 
                  "stateTwo = ?, " + 
                  "stateThree = ?, " +
                  "stateFour = ?, " + 
                  "stateFive = ?, " +
                  "stateSix = ?, " + 
                  "stateSeven = ? " + 
                  "where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, record.getWaiterId());
    for (int i = 0; i < record.getStates().length; i++) {
        statement.setString(i+2, record.getStates()[i]);
    }
    statement.setInt(9, record.getId());

    statement.executeUpdate();
 }

 public static void removeWorkScheduleRecord(int recordId) throws SQLException{
    String sql = "delete from restourant_db.work_schedule where id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, recordId);
    statement.executeUpdate();
 }

    public static Event getEventIfHasOrder(int idOrder) throws SQLException{
     String sql = "select idEvent from restourant_db.order_list where idOrder = ?";
     PreparedStatement statement = connection.prepareStatement(sql);
     statement.setInt(1, idOrder);
     ResultSet result = statement.executeQuery();

     if(result.next()){
        int eventId = result.getInt("idEvent");
      return DB_Handler.getEventById(eventId);
    }

        return null;
    }

   public static List<String> getPopularDishesDaily() throws SQLException{
        String sql = "SELECT idDish, name\n" + //
                    "FROM (\n" + //
                    "    SELECT sum(quantity) as quantitySum, Date(dateTime) as date, idDish, name\n" + //
                    "    FROM restourant_db.orders o\n" + //
                    "    JOIN restourant_db.dish_list dl ON o.id = dl.idOrder\n" + //
                    "    JOIN restourant_db.dishes d ON d.id = dl.idDish\n" + //
                    "    GROUP BY idDish, name, Date(dateTime)\n" + //
                    "    HAVING quantitySum > 2\n" + //
                    ") AS tempTable\n" + //
                    "GROUP BY idDish, name\n" + //
                    "HAVING COUNT(DISTINCT date) = (\n" + //
                    "    SELECT COUNT(DISTINCT Date(dateTime))\n" + //
                    "    FROM restourant_db.orders\n" + //
                    ")";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();
        
        List<String> dishesNames = new ArrayList<>(); 
        while (result.next()) {
            String dishName = result.getString("name");
            dishesNames.add(dishName);
        }

        return dishesNames;
    }
        
    public static List<Integer> getDishesOrderedMoreThanTwiceAtEachTable() throws SQLException{
        String sql = "SELECT idDish FROM restourant_db.orders o\n" + //
                        "join restourant_db.dish_list dl on o.id = dl.idOrder\n" + //
                        "join restourant_db.dishes d on d.id = dl.idDish\n" + //
                        "where quantity > 2\n" + //
                        "group by  idDish\n" + //
                        "having count(distinct idTable) = (SELECT count(distinct idTable) FROM restourant_db.orders)\n";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();
        List<Integer> dishesIds = new ArrayList<>();
        while (result.next()) {
            dishesIds.add(result.getInt("idDish"));
        }

        return dishesIds;
    }

    public static Map <String, Integer> countDishesPerCategory() throws SQLException{
        String sql = "select  category, count(id) as quantity from restourant_db.dishes\n" + //
                      "group by category\n" + 
                      "order by category";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        Map <String, Integer> categoryDishQuantity = new LinkedHashMap<>();
        while (result.next()) {
            String category = result.getString("category");
            int quantity = result.getInt("quantity");
            categoryDishQuantity.put(category, quantity);
        }
        return categoryDishQuantity;
    }


    public static Map<Integer, Integer> getTopFiveDishes() throws SQLException{
        String sql = "SELECT id, SUM(quantity) AS degreeRate\n" + //
                        "FROM restourant_db.dishes d\n" + //
                        "JOIN restourant_db.dish_list dl ON d.id = dl.idDish\n" + //
                        "GROUP BY id\n" + //
                        "ORDER BY SUM(quantity) DESC\n" + //
                        "LIMIT 5;";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();

        Map<Integer, Integer> top5DishesAndTheirRateDegree = new LinkedHashMap<>();
        while (result.next()) {
            int idDish = result.getInt("id");
            int degreeRate = result.getInt("degreeRate");

            top5DishesAndTheirRateDegree.put(idDish, degreeRate);
        }

        return top5DishesAndTheirRateDegree;
    }

    public static double calculateRevenueForDate(LocalDate date) throws SQLException{
        String sql = "SELECT sum(quantity * price) as generalPrice FROM restourant_db.reports r\n" + //
                        "join restourant_db.dish_list_history dlh on dlh.idReport = r.id\n" + //
                        "join restourant_db.dishes d on dlh.idDish = d.id\n" + //
                        "where date(dateTime) = ?\n" + //
                        "group by date(dateTime)";
        PreparedStatement statement = connection.prepareStatement(sql);
        Date convertedDate = Date.valueOf(date);

        statement.setDate(1, convertedDate);
        ResultSet result = statement.executeQuery();

        if(result.next()){
            double amount = result.getDouble("generalPrice");
            return amount;
        }

        return 0.0;  
    }

   public static List<Waiter> getWaitersWorkingWekends() throws SQLException{
    String sql = "SELECT w.id, w.name, w.competencyRank, w.keyId, k.login, k.accessRight\n " + //
                "FROM restourant_db.waiters w\n " + //
                "JOIN restourant_db.work_schedule wsh ON wsh.idWaiter = w.id\n " + //
                "JOIN restourant_db.keys k ON k.id = w.keyId\n " + //
                "WHERE \n " + //
                "    CASE \n " + //
                "        WHEN DAYOFWEEK(CURRENT_DATE) = 6 THEN \n " + //
                "            wsh.stateFive = 'Робочий' or wsh.stateSix = 'Робочий'\n " + //
                "        \n" + //
                "        WHEN DAYOFWEEK(CURRENT_DATE) = 7 THEN \n " + //
                "            wsh.stateFour = 'Робочий' or wsh.stateFive = 'Робочий'\n " + //
                "        \n " + //
                "        WHEN DAYOFWEEK(CURRENT_DATE) = 1 THEN \n " + //
                "            wsh.stateThree = 'Робочий' or wsh.stateFour = 'Робочий'\n " + //
                "\n" + //
                "        WHEN DAYOFWEEK(CURRENT_DATE) = 2 THEN \n" + //
                "            wsh.stateThree = 'Робочий' or wsh.stateTwo = 'Робочий'\n " + //
                "        \n" + //
                "        WHEN DAYOFWEEK(CURRENT_DATE) = 3 THEN \n" + //
                "            wsh.stateTwo = 'Робочий' or wsh.stateOne = 'Робочий'\n " + //
                "\n" + //
                "        WHEN DAYOFWEEK(CURRENT_DATE) = 4 THEN \n" + //
                "            wsh.stateOne = 'Робочий' or wsh.stateSeven = 'Робочий'\n " + //
                "\n" + //
                "        WHEN DAYOFWEEK(CURRENT_DATE) = 5 THEN \n " + //
                "            wsh.stateSix = 'Робочий' or wsh.stateSeven = 'Робочий'\n " + //
                "    END;\n " + //
                "";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet result = statement.executeQuery();
    
    List<Waiter> waiters = new ArrayList<>();
    while (result.next()) {

        int keyId = result.getInt("keyId");
        String login = result.getString("login");
        String accessRight = result.getString("accessRight");

        int idWaiter = result.getInt("id");
        String name = result.getString("name");
        String competencyRank = result.getString("competencyRank");

        waiters.add(new Waiter(idWaiter, name, competencyRank, new Key(keyId, login, accessRight)));
    }
    
    return waiters;
    
    }

}