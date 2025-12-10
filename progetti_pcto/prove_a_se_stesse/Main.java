package com.cineca.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int scelta = -1;

        // menù per la scelta delle azioni che utente puo fare
        while (scelta != 0) {
            System.out.println("\n--- MENU GESTIONE Database---");
            System.out.println("1. Test Connessione Database");
            System.out.println("2. Inserisci nuovo Studente");
            System.out.println("3. Modifica uno Studente");
            System.out.println("4. Elimina uno Studente");
            System.out.println("5. Stampa tutti gli Studente");
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

            // chiamata di ogni funzione per ogni funzionalità scelta, default = errore perchè non è un numero nello switch
            switch (scelta) {
                case 1:
                    testConnessione();
                    break;
                case 2:
                    boolean riuscita = inserisciStudente(scanner);
                    if (riuscita) {
                        System.out.println("Studente aggiunto :D ");
                    } else {
                        System.out.println("Errore durante l'aggiunta dello studente :C");
                    }
                    break;
                case 3:
                    boolean riuscitaModifica = modificastudente(scanner);
                    if (riuscitaModifica) {
                        System.out.println("Studente modificato con successo");
                    } else {
                        System.out.println("c'è stato un errore durante la modifica dello studente");
                    }
                    break;
                case 4:
                    boolean riuscitaElimina = eliminaStudente(scanner);
                    if (riuscitaElimina) {
                        System.out.println("Studente eliminato con successo");
                    } else {
                        System.out.println("c'è stato un errore durante la eliminazione dello studente");
                    }
                    
                    break;
                case 5:
                    stampaStudentiConIndirizzi();
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

    // INSERIMENTO STUDENTE
    private static boolean inserisciStudente(Scanner scn) {
        // inizio input dati utente per la creazione di un nuovo studente
        System.out.print("Inserisci il nome dello studente: ");
        String nome = scn.nextLine();

        System.out.print("Inserisci il cognome dello studente: ");
        String cognome = scn.nextLine();

        System.out.print("Inserisci il luogo di nascita dello studente: ");
        String LuogoDiNascita = scn.nextLine();

        /*
         * regex per dividere
         * String regex = "\\s+\\d+ ";
         * System.out.println("Inserisci la via di residenza(formato: via numero): ");
         * togliamo spazi iniziali e finali
         * String tmp = scn.nextLine().trim();
         * String[] pezzi = tmp.split(regex);
         * String via_residenza = pezzi[0];
         * int numero_residenza = Integer.parseInt(pezzi[1]);
         */

        System.out.print("Inserisci la via di residenza(solo via): ");
        String via_residenza = scn.nextLine().trim();
        System.out.print("Inserisci il numero civico di residenza: ");

        int numero_residenza = 0;
        if (scn.hasNextInt()) {
            numero_residenza = scn.nextInt();
            scn.nextLine();
        } else {
            scn.nextLine();
            System.out.println("Opzione non valida, riprova.");
            return false;
        }

        // stessa cosa di via di residenza ma con via di domicilio
        System.out.print("Inserisci ora la via di domicilio(solo via): ");
        String via_domicilio = scn.nextLine().trim();
        System.out.print("Inserisci il numero civico di domicilio: ");

        int numero_domicilio = 0;
        if (scn.hasNextInt()) {
            numero_domicilio = scn.nextInt();
            scn.nextLine();
        } else {
            scn.nextLine();
            System.out.println("Opzione non valida, riprova.");
            return false;
        }

        // istanziamo le varie variabili uguali a null
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtResidenza = null;
        PreparedStatement pstmtDomicilio = null;
        ResultSet rs = null;
        int residenzaId = -1;
        int domicilioId = -1;

        // query con placeholder a ? per non fare escape con apici per sqli
        String queryIndirizzo = "INSERT INTO indirizzi (indirizzo, numero_civico) VALUES (?, ?)";
        String queryStudente = "INSERT INTO studenti (nome, cognome, luogo_di_nascita, residenza_id, domicilio_id) VALUES (?, ?, ?,? ,? )";

        try {
            // Prima query: quella di residenza
            conn = DatabaseManager.ottieniConnessione();
            conn.setAutoCommit(false); // Disattiva l'AutoCommit, serve per non fare fallire più query dipendenti,
            /*
             * Se il recupero id del domicilio fallisse, ma il Passo 1
             * avesse già salvato l'indirizzo, avresti un database in uno stato inconsistent
             * Se uno qualsiasi dei passi fallisce (catturato dal catch), viene chiamato conn.rollback(),
             * che cancella automaticamente anche l'indirizzo di residenza e l'indirizzo di domicilio inseriti
             * in precedenza. L'operazione è annullata completamente.
             */

            // Inserimento residenza e recupero id

            // prepara la variabile a ricevere anche le chiavi generate
            pstmtResidenza = conn.prepareStatement(queryIndirizzo, Statement.RETURN_GENERATED_KEYS);
            // solito prepared statement
            pstmtResidenza.setString(1, via_residenza);
            pstmtResidenza.setInt(2, numero_residenza);
            pstmtResidenza.executeUpdate();

            // Recupera l'id
            rs = pstmtResidenza.getGeneratedKeys();
            if (rs.next()) {
                // prendiamo il valore della prima colonna, ovvero l'id
                residenzaId = rs.getInt(1);
            }
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                } // Chiudi subito l'ultimo ResultSet

            // inserimento domicilio e recupero id

            // stessa cosa come con la residenza ma con invece, domicilio
            pstmtDomicilio = conn.prepareStatement(queryIndirizzo, Statement.RETURN_GENERATED_KEYS);
            pstmtDomicilio.setString(1, via_domicilio);
            pstmtDomicilio.setInt(2, numero_domicilio);
            pstmtDomicilio.executeUpdate();

            // Recupera l'id, dal primo campo
            rs = pstmtDomicilio.getGeneratedKeys();
            if (rs.next()) {
                domicilioId = rs.getInt(1);
            }

            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                } // Chiudi subito l'ultimo ResultSet

            // Controlla che entrambi gli ID siano stati recuperati, altrimenti esce
            if (residenzaId == -1 || domicilioId == -1) {
                throw new SQLException("Errore: Impossibile recuperare gli ID di residenza/domicilio.");
            }

            // inserimento studente
            pstmt = conn.prepareStatement(queryStudente);

            // Dati studente
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, LuogoDiNascita);

            // Chiavi Esterne prese gentilmente dalle query di prima
            pstmt.setInt(4, residenzaId);
            pstmt.setInt(5, domicilioId);

            //per il check dopo, se almeno una riga è stata aggiornata
            int righeAggiornate = pstmt.executeUpdate();

            // Se tutto è andato bene, conferma le modifiche
            conn.commit();
            return righeAggiornate > 0;

        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback(); // Nel caso qualcosa vada storto, annulla tutto
                    System.err.println("Transazione annullat.");
                } catch (SQLException ee) {
                    System.err.println("Rollback fallito: " + ee.getMessage());
                }
            }
            return false;

        } finally {
            // chiusura di tutte le risorse in modo esplicito
            if (rs != null) try {rs.close(); } catch (SQLException e) {}
            if (pstmtResidenza != null)try {pstmtResidenza.close();} catch (SQLException e) {}
            if (pstmtDomicilio != null)try {pstmtDomicilio.close();} catch (SQLException e) {}
            if (pstmt != null)try {pstmt.close();} catch (SQLException e) {}
            if (conn != null)try {conn.close();} catch (SQLException e) {}
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

        // Lettura dell'id
        System.out.print("Inserisci l'ID dello studente da modificare: ");
        if (!scn.hasNextInt()) {
            System.out.println("ID non valido. Ritorno al menu principale.");
            scn.nextLine();
            return false;
        }
        idStudente = scn.nextInt();
        scn.nextLine();

        // Menu
        while (sceltaMenu != 0) {
            System.out.println("\n--- COSA VUOI MODIFICARE? ---");
            System.out.println("1. Nome");
            System.out.println("2. Cognome");
            System.out.println("3. Luogo di Nascita");
            System.out.println("4. Modifica tutto il record");
            System.out.println("5. Modifica Indirizzo");
            System.out.println("6. Modifica Domicilio");
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
                    return aggiornaSingoloCampo(idStudente, "luogo_di_nascita", nuovoLuogoDiNascita);

                case 4:
                    // Modifica tutto il record
                    System.out.print("Nuovo Nome: ");
                    nuovoNome = scn.nextLine();
                    System.out.print("Nuovo Cognome: ");
                    nuovoCognome = scn.nextLine();
                    System.out.print("Nuovo Luogo di Nascita: ");
                    nuovoLuogoDiNascita = scn.nextLine();

                    return aggiornaTuttoIlRecord(idStudente, nuovoNome, nuovoCognome, nuovoLuogoDiNascita);
                case 5:
                    // Modifica indirizzo
                    return modificaIndirizzo(scn, idStudente,"residenza_id" );
                case 6:
                    return modificaIndirizzo(scn, idStudente,"domicilio_id" );
 
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
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                }
        }
    }
    
    // MODIFICA INDIRIZZO
    private static boolean modificaIndirizzo(Scanner scn, int idStudente, String tipoIndirizzo) {
        System.out.print("Inserisci la nuova via (solo via): ");
        String indirizzo = scn.nextLine().trim();
        int numero = 0;
        System.out.print("Inserisci il numero civico: ");
        if(scn.hasNextInt()){
            numero = scn.nextInt();
            scn.nextLine();
        }else{
            System.out.println("Numero civico non valido. Ritorno al menu principale.");
            scn.nextLine();
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        String query = "UPDATE indirizzi SET via = ?, numero = ? WHERE id = (SELECT " + tipoIndirizzo + " FROM studenti WHERE id = ?)";

        try {
            conn = DatabaseManager.ottieniConnessione();
            pstmt = conn.prepareStatement(query);

            pstmt.setString(1, indirizzo);
            pstmt.setInt(2, numero);
            pstmt.setInt(3, idStudente);

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
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                }
        }
    }
    
    // ELIMINA STUDENTE
    private static boolean eliminaStudente(Scanner scn) {
        // campo di input
        System.out.print("Inserisci l'ID univoco dello studente da eliminare: ");
        int scelta = scn.nextInt();
        scn.nextLine();

        // variabili a null
        Connection conn = null;
        PreparedStatement pstmt = null;
        String query = "DELETE FROM studenti WHERE id = ?";

        try {
            // prepared statement ed esecuzione query
            conn = DatabaseManager.ottieniConnessione();
            pstmt = conn.prepareStatement(query);

            pstmt.setInt(1, scelta);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Errore nel db: " + e.getMessage());
            return false;
        } finally {
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                }
        }
    }

    public static void stampaStudentiConIndirizzi() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        String query = "SELECT s.nome, s.cognome, s.luogo_di_nascita,R.indirizzo, R.numero_civico, " + 
                       "D.indirizzo, D.numero_civico FROM studenti s " + 
                       "JOIN indirizzi R ON s.residenza_id = R.id " + 
                       "JOIN indirizzi D ON s.domicilio_id = D.id";

        try {
            conn = DatabaseManager.ottieniConnessione();
            // non utilizziamo prepared statement perche tanto non ci sono input possibilmente malevoli
            stmt = conn.createStatement();
            // prendiamo i risultati della query in rs
            rs = stmt.executeQuery(query);

            System.out.println("------------------------------------ Lista Studenti con Indirizzi ------------------------------------");
            // stavo andando a capo con \t ma non veniva allineato bene, quindi uso printf
            String FORMAT_STRING = "%-15s %-15s %-20s %-25s %-25s\n";
            System.out.printf(FORMAT_STRING, 
                              "Nome", "Cognome", "Luogo Nascita", "Residenza", "Domicilio");
            System.out.println("------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                // estraggo i vari campi dal result set
                String nome = rs.getString(1);
                String cognome = rs.getString(2);
                String luogo_di_nascita = rs.getString(3);
                String viaResidenza = rs.getString(4);
                int civicoResidenza = rs.getInt(5);
                String viaDomicilio = rs.getString(6);
                int civicoDomicilio = rs.getInt(7);

                String residenzaCompleta = viaResidenza + " " + civicoResidenza;
                String domicilioCompleto = viaDomicilio + " " + civicoDomicilio;

                System.out.printf(FORMAT_STRING, nome, cognome, luogo_di_nascita, residenzaCompleta, domicilioCompleto);
            }
            System.out.println("------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Errore durante la stampa degli studenti: " + e.getMessage());
        } finally {
            // Chiudi le risorse (ResultSet, Statement, Connection)
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                }
        }
    }
}