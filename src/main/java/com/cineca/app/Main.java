package com.cineca.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int scelta = -1;

        while (scelta != 0) {
            System.out.println("\n--- MENU GESTIONE Database---");
            System.out.println("1. Test Connessione Database");
            System.out.println("2. Inserisci nuovo Studente");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");
            
            // Controllo input per evitare crash se l'utente inserisce lettere
            if (scanner.hasNextInt()) {
                scelta = scanner.nextInt();
            } else {
                scanner.next(); // consuma input non valido
                scelta = -1;
            }

            switch (scelta) {
                case 1:
                    testConnessione();
                    break;
                case 2:
                    inserisciStudente(scanner);
                    break;
                case 0:
                    System.out.println("Uscita in corso...");
                    break;
                default:
                    System.out.println("Opzione non valida, riprova.");
            }
        }
        scanner.close();
    }

    private static void testConnessione() {
        System.out.println("Tentativo di connessione...");
        // Qui usiamo la classe DatabaseManager creata prima
        try (Connection conn = DatabaseManager.ottieniConnessione()) {
            System.out.println("Connessione riuscita!");
        } catch (SQLException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
        }
    }

    private static boolean inserisciStudente(Scanner scn) {
        System.out.print("Inserisci il nome dello studente: ");
        String nome = scn.nextLine();
        System.out.print("Inserisci il cognome dello studente: ");
        String cognome = scn.nextLine();
        System.out.print("Inserisci il luogo di nascita dello studente: ");
        String LuogoDiNascita = scn.nextLine();

        Connection conn = null;
        PreparedStatement pstmt = null;
        String query = "INSERT INTO studenti (nome, cognome, LuogoDiNascita) VALUES (?, ?, ?)";
    
        try {
            conn = DatabaseManager.ottieniConnessione();
            pstmt = conn.prepareStatement(query);
            
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, LuogoDiNascita);
            
            return pstmt.executeUpdate() > 0; // Ritorna true se inserita 1+ riga
            
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            return false;
            
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }

    }
}
