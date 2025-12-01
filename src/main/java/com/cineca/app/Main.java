package com.cineca.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int scelta = -1;

        //menù per la scelta delle azioni che utente puo fare
        while (scelta != 0) {
            System.out.println("\n--- MENU GESTIONE Database---");
            System.out.println("1. Test Connessione Database");
            System.out.println("2. Inserisci nuovo Studente");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");
            
            // Controllo input per evitare crash se l'utente inserisce lettere
            if (scanner.hasNextInt()) {
                scelta = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.next(); // consuma input non valido
                scelta = -1;
            }

            //chiamata di ogni funzione per ogni funzionalità scelta, default = errore perchè non è un numero nello switch
            switch (scelta) {
                case 1:
                    testConnessione();
                    break;
                case 2:
                    boolean riuscita = inserisciStudente(scanner);
                    if(riuscita){
                        System.out.println("Studente aggiunto :D ");
                    }else{
                        System.out.println("Errore durante l'aggiunta dello studente :C");
                    }
                    break;

                case 4:
                    boolean riuscitaElimina = eliminaStudente(scanner);
                    if(riuscitaElimina){
                        System.out.println("Studente eliminato con successo");
                    }else{
                        System.out.println("c'è stato un errore durante la eliminazione dello studente");
                    }
                    
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
        //inizio input dati utente per la creazione di un nuovo studente
        System.out.print("Inserisci il nome dello studente: ");
        String nome = scn.nextLine();

        System.out.print("Inserisci il cognome dello studente: ");
        String cognome = scn.nextLine();

        System.out.print("Inserisci il luogo di nascita dello studente: ");
        String LuogoDiNascita = scn.nextLine();

        //regex per dividere 
        String regex = "\\s+\\d+ ";
        System.out.println("Inserisci la via di residenza(formato: via numero): ");
        //togliamo spazi iniziali e finali
        String tmp = scn.nextLine().trim(); 
        String[] pezzi = tmp.split(regex);
        String via_residenza = pezzi[0];
        int numero_residenza =  Integer.parseInt(pezzi[1]);
        

        
        //stessa cosa di via di residenza ma con via di domicilio 
        System.out.println("Inserisci ora la via di domicilio(formato: via numero): ");
        tmp = scn.nextLine().trim(); 
        pezzi = tmp.split(regex);
        String via_domicilio = pezzi[0];
        int numero_domicilio =  Integer.parseInt(pezzi[1]);

        //istanziamo le varie variabili uguali a null
        Connection conn = null;
        PreparedStatement pstmt = null;

        //query con placeholder a ? per non fare escape con apici per sqli
        String query = "INSERT INTO studenti (nome, cognome, LuogoDiNascita) VALUES (?, ?, ?)";
    
        try {
            //definizione delle variabili
            conn = DatabaseManager.ottieniConnessione();
            pstmt = conn.prepareStatement(query);
            
            //dati nelle prepared statement sostituiti
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, LuogoDiNascita);
            
            return pstmt.executeUpdate() > 0; // Ritorna true se inserita 1+ riga
            
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            return false;
            
        } finally {
            //chiudiamo le connessioni
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }

    }
    // MODIFICA STUDENTE
    private static boolean modificastudente(Scanner scn) {
        System.out.println("Benvenuto nella modifica dello studente");
 
        String nuovoNome = "";
        String nuovoCognome = "";
        String nuovoLuogoDiNascita = "";
 
        int idStudente = 0;
        int sceltaMenu = -1;
 
        //Lettura dell'id
        System.out.print("Inserisci l'ID dello studente da modificare: ");
        if (!scn.hasNextInt()) {
            System.out.println("ID non valido. Ritorno al menu principale.");
            scn.nextLine();
            return false;
        }
        idStudente = scn.nextInt();
        scn.nextLine();
 
        //Menu
        while (sceltaMenu != 0) {
            System.out.println("\n--- COSA VUOI MODIFICARE? ---");
            System.out.println("1. Nome");
            System.out.println("2. Cognome");
            System.out.println("3. Luogo di Nascita");
            System.out.println("4. Modifica tutto il record");
            System.out.println("0. Annulla modifica");
            System.out.print("Scegli un campo da aggiornare: ");
 
            if (scn.hasNextInt()) {
                sceltaMenu = scn.nextInt();
                scn.nextLine();
            } else {
                scn.nextLine();
                System.out.println("Opzione non valida, riprova.");
                continue;
            }
 
            switch (sceltaMenu) {
                case 1:
                    System.out.print("Nuovo Nome: ");
                    nuovoNome = scn.nextLine();
                    return aggiornaSingoloCampo(idStudente, "nome", nuovoNome);
 
                case 2:
                    System.out.print("Nuovo Cognome: ");
                    nuovoCognome = scn.nextLine();
                    return aggiornaSingoloCampo(idStudente, "cognome", nuovoCognome);
 
                case 3:
                    System.out.print("Nuovo Luogo di Nascita: ");
                    nuovoLuogoDiNascita = scn.nextLine();
                    return aggiornaSingoloCampo(idStudente, "LuogoDiNascita", nuovoLuogoDiNascita);
 
                case 4:
                    // Modifica tutto il record
                    System.out.print("Nuovo Nome: ");
                    nuovoNome = scn.nextLine();
                    System.out.print("Nuovo Cognome: ");
                    nuovoCognome = scn.nextLine();
                    System.out.print("Nuovo Luogo di Nascita: ");
                    nuovoLuogoDiNascita = scn.nextLine();
 
                    return aggiornaTuttoIlRecord(idStudente, nuovoNome, nuovoCognome, nuovoLuogoDiNascita);
 
                case 0:
                    System.out.println("Modifica annullata.");
                    break; // Esce dallo switch, il ciclo while terminerà
 
                default:
                    System.out.println("Opzione non valida.");
                    break;
            }
        }
        return false; // Ritorna false se l'utente ha scelto 0 (Annulla modifica)
    }
 
    // AGGIORNA SINGOLO CAMPO
    private static boolean aggiornaSingoloCampo(int idStudente, String nomeColonna, String nuovoValore) {
        Connection conn = null;
        PreparedStatement pstmt = null;
 
        String query = "UPDATE studenti SET " + nomeColonna + " = ? WHERE id = ?";
 
        try {
            conn = DatabaseManager.ottieniConnessione();
 
            pstmt = conn.prepareStatement(query);
 
            pstmt.setString(1, nuovoValore);
            pstmt.setInt(2, idStudente);
            int righeModificate = pstmt.executeUpdate();
 
            return righeModificate > 0;
 
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            return false;
 
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
    }
 
    // AGGIORNA TUTTO IL RECORD
    private static boolean aggiornaTuttoIlRecord(int idStudente, String nome, String cognome, String LuogoDiNascita) {
        Connection conn = null;
        PreparedStatement pstmt = null;
 
        String query = "UPDATE studenti SET nome = ?, cognome = ?, LuogoDiNascita = ? WHERE id = ?";
 
        try {
 
            conn = DatabaseManager.ottieniConnessione();
 
            pstmt = conn.prepareStatement(query);
 
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, LuogoDiNascita);
 
            pstmt.setInt(4, idStudente);
            int righeModificate = pstmt.executeUpdate();
 
            return righeModificate > 0;
 
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            return false;
 
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
    }
    private static boolean eliminaStudente(Scanner scn){
        //campo di input
        System.out.println("Inserisci l'ID univoco dello studente da eliminare;");
        int scelta = scn.nextInt();
        scn.nextLine();

        //variabili a null
        Connection conn = null;
        PreparedStatement pstmt = null;
        String query = "DELETE FROM studenti WHERE id = ?";

        try{
            //prepared statement ed esecuzione query
            conn = DatabaseManager.ottieniConnessione();
            pstmt = conn.prepareStatement(query);

            pstmt.setInt(1, scelta);
            return pstmt.executeUpdate() > 0;

        }catch(SQLException e){
            System.out.println("Errore nel db: "  + e.getMessage());
            return false;
        }finally{
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
    }
}
