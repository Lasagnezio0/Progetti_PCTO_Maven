package com.cineca.app.frontend;

import javax.swing.SwingUtilities;

import com.cineca.app.backend.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        // Swing deve girare nel suo thread dedicato (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Tenta connessione iniziale (Opzionale, solo per check)
                DatabaseManager.ottieniConnessione().close(); 
                System.out.println("DB Connesso. Avvio GUI...");
                
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                
            } catch (Exception e) {
                System.err.println("Impossibile connettersi al DB: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}