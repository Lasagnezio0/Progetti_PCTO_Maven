package com.cineca.app.backend;

import java.sql.*;
import java.util.Vector;

public class StudentDAO {

    // Helper per inserire indirizzo e tornare ID (spostato da MainFrame)
    private static int inserisciIndirizzo(Connection conn, String via, int civico) throws SQLException {
        String sql = "INSERT INTO indirizzi (indirizzo, numero_civico) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, via);
            pstmt.setInt(2, civico);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Fallimento creazione indirizzo");
    }

    // Metodo spostato da MainFrame: Esegue un test di connessione al database.
    public static boolean testConnessioneDB() {
        try (Connection conn = DatabaseManager.ottieniConnessione()) {
            return true;
        } catch (SQLException e) {
            // Stampiamo l'errore qui, ma la gestione del messaggio JOptionPane avviene nella GUI.
            System.err.println("Errore di connessione al DB: " + e.getMessage());
            return false;
        }
    }

    //Recupera tutti gli studenti (incluso ID e dati indirizzi separati per la modifica)
    public static Vector<Vector<Object>> ottieniTuttiStudentiCompleti() {
        Vector<Vector<Object>> data = new Vector<>();
        // Query che prende tutti i campi, inclusi gli ID e i campi separati per Residenza (R) e Domicilio (D)
        String query = "SELECT s.id, s.nome, s.cognome, s.luogo_di_nascita, " +
                       "R.id as rid, R.indirizzo as rvia, R.numero_civico as rciv, " +
                       "D.id as did, D.indirizzo as dvia, D.numero_civico as dciv " +
                       "FROM studenti s " +
                       "JOIN indirizzi R ON s.residenza_id = R.id " +
                       "JOIN indirizzi D ON s.domicilio_id = D.id";

        try (Connection conn = DatabaseManager.ottieniConnessione();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Vector<Object> riga = new Vector<>();
                // Questi 12 campi sono necessari per l'utilizzo nel JTable del MainFrame (incluso i nascosti)
                riga.add(rs.getInt("id"));             // 0: ID Studente (Nascosto)
                riga.add(rs.getString("nome"));        // 1: Nome
                riga.add(rs.getString("cognome"));     // 2: Cognome
                riga.add(rs.getString("luogo_di_nascita")); // 3: Luogo Nascita
                riga.add(rs.getString("rvia") + " " + rs.getInt("rciv")); // 4: Residenza Unita
                riga.add(rs.getString("dvia") + " " + rs.getInt("dciv")); // 5: Domicilio Unito
                riga.add(rs.getInt("rid"));            // 6: ID Res (Nascosto)
                riga.add(rs.getString("rvia"));        // 7: Via Res (Nascosto)
                riga.add(rs.getInt("rciv"));           // 8: Civ Res (Nascosto)
                riga.add(rs.getInt("did"));            // 9: ID Dom (Nascosto)
                riga.add(rs.getString("dvia"));        // 10: Via Dom (Nascosto)
                riga.add(rs.getInt("dciv"));           // 11: Civ Dom (Nascosto)
                
                data.add(riga);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Inserisce uno studente (Transazione complessa)
    public static boolean inserisciStudente(String nome, String cognome, String luogo, 
                                            String viaRes, int civRes, 
                                            String viaDom, int civDom) {
        Connection conn = null;
        try {
            conn = DatabaseManager.ottieniConnessione();
            conn.setAutoCommit(false); // Inizio Transazione

            int idRes = inserisciIndirizzo(conn, viaRes, civRes);
            int idDom = inserisciIndirizzo(conn, viaDom, civDom);

            String queryStudente = "INSERT INTO studenti (nome, cognome, luogo_di_nascita, residenza_id, domicilio_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(queryStudente)) {
                pstmt.setString(1, nome);
                pstmt.setString(2, cognome);
                pstmt.setString(3, luogo);
                pstmt.setInt(4, idRes);
                pstmt.setInt(5, idDom);
                pstmt.executeUpdate();
            }

            conn.commit(); // Conferma
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    // NUOVO: Modifica studente (Transazione complessa per 3 UPDATE)
    public static boolean eseguiModificaCompleta(int idStud, int idRes, int idDom, String nome, String cognome, String luogo, String viaRes, int civRes, String viaDom, int civDom) {
        Connection conn = null;
        //dato che i campi di inpiut in modifica hanno sempre qualcosa dentro, prende tuti i valori dei campi
        //e ci li piazza nell'update della tabella studenti e indirizzi
        try {
            conn = DatabaseManager.ottieniConnessione();
            conn.setAutoCommit(false);

            // 1. Aggiorna Studente
            try (PreparedStatement ps = conn.prepareStatement("UPDATE studenti SET nome=?, cognome=?, luogo_di_nascita=? WHERE id=?")) {
                ps.setString(1, nome); ps.setString(2, cognome); ps.setString(3, luogo); ps.setInt(4, idStud);
                ps.executeUpdate();
            }

            // 2. Aggiorna Residenza
            try (PreparedStatement ps = conn.prepareStatement("UPDATE indirizzi SET indirizzo=?, numero_civico=? WHERE id=?")) {
                ps.setString(1, viaRes); ps.setInt(2, civRes); ps.setInt(3, idRes);
                ps.executeUpdate();
            }

            // 3. Aggiorna Domicilio
            try (PreparedStatement ps = conn.prepareStatement("UPDATE indirizzi SET indirizzo=?, numero_civico=? WHERE id=?")) {
                ps.setString(1, viaDom); ps.setInt(2, civDom); ps.setInt(3, idDom);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            System.err.println("Errore modifica: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }


    // Elimina studente
    public static boolean eliminaStudente(int idStudente) {
        String query = "DELETE FROM studenti WHERE id = ?";
        try (Connection conn = DatabaseManager.ottieniConnessione();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idStudente);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}