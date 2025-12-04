package com.cineca.app.backend.DAO;

import java.sql.*;
import java.util.Vector;

public class StudentDAO {

    // Inserisce lo studente collegando i due ID degli indirizzi
    public static void inserisciStudente(Connection conn, String nome, String cognome, String luogo, int idResidenza, int idDomicilio) throws SQLException {
        String sql = "INSERT INTO studenti (nome, cognome, luogo_di_nascita, residenza_id, domicilio_id) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, luogo);
            pstmt.setInt(4, idResidenza);
            pstmt.setInt(5, idDomicilio);
            pstmt.executeUpdate();
        }
    }

    // Aggiorna solo i dati anagrafici dello studente
    public static void aggiornaDatiStudente(Connection conn, int idStudente, String nome, String cognome, String luogo) throws SQLException {
        String sql = "UPDATE studenti SET nome=?, cognome=?, luogo_di_nascita=? WHERE id=?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, luogo);
            ps.setInt(4, idStudente);
            ps.executeUpdate();
        }
    }

    // Elimina studente
    public static boolean eliminaStudente(Connection conn, int idStudente) throws SQLException {
        String sql = "DELETE FROM studenti WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStudente);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Query di lettura (Join). Anche qui passiamo la connessione per coerenza.
    public static Vector<Vector<Object>> ottieniTuttiStudentiCompleti(Connection conn) throws SQLException {
        Vector<Vector<Object>> data = new Vector<>();
        String query = "SELECT s.id, s.nome, s.cognome, s.luogo_di_nascita, " +
                       "R.id as rid, R.indirizzo as rvia, R.numero_civico as rciv, " +
                       "D.id as did, D.indirizzo as dvia, D.numero_civico as dciv " +
                       "FROM studenti s " +
                       "JOIN indirizzi R ON s.residenza_id = R.id " +
                       "JOIN indirizzi D ON s.domicilio_id = D.id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Vector<Object> riga = new Vector<>();
                riga.add(rs.getInt("id"));             // 0
                riga.add(rs.getString("nome"));        // 1
                riga.add(rs.getString("cognome"));     // 2
                riga.add(rs.getString("luogo_di_nascita")); // 3
                riga.add(rs.getString("rvia") + " " + rs.getInt("rciv")); // 4 (Residenza Unita)
                riga.add(rs.getString("dvia") + " " + rs.getInt("dciv")); // 5 (Domicilio Unito)
                riga.add(rs.getInt("rid"));            // 6
                riga.add(rs.getString("rvia"));        // 7
                riga.add(rs.getInt("rciv"));           // 8
                riga.add(rs.getInt("did"));            // 9
                riga.add(rs.getString("dvia"));        // 10
                riga.add(rs.getInt("dciv"));           // 11
                data.add(riga);
            }
        }
        return data;
    }
}