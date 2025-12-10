package com.cineca.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // Metodo statico che restituisce la connessione
    public static Connection ottieniConnessione() throws SQLException {
        // Usa le variabili prese dalla classe Config 

        // creare classe con le variabili di configurazione
        /*public class Config {
            *public static final String URL = "url_database";
            *public static final String USER = "user";
            *public static final String PASSWORD = "password!"; 
        }
         */

        return DriverManager.getConnection(Config.URL, Config.USER, Config.PASSWORD);
    }
}
