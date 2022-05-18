package ru.geekbrains.jt.chat.server.core;

import java.sql.*;

public class SqlClient {
    private static Connection connection;
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat-server/db/clients-db.sqlite");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    synchronized static String getNick(String login, String password) {
        String query = String.format(
                "select nickname from users where login='%s' and password='%s'",
                login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next())
                return set.getString("nickname");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

   synchronized static boolean checkLogin(String login){
       String query = String.format(
               "select login from users where login='%s'",login);
       try (ResultSet set = statement.executeQuery(query)) {
           return (set.next());
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
   }
   synchronized static boolean addLogin(String nickname, String login, String password){
        /* добавление пары  login,password в базу данных
        String query = String.format("INSERT INTO users (login,password,nickname) VALUES ('%s','%s','%s')",login,password,nickname);
        */
        String query = String.format("INSERT INTO users (login,password,nickname) VALUES ('%s','%s','%s')",login,password,nickname);
        try {
            statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
          //  throw new RuntimeException(e);
            return false;
        }

   }

   static void userRegistration(String login, String password, String nickname){
        if (!checkLogin(login)){
            addLogin(nickname,login,password);
       }

   }

}
