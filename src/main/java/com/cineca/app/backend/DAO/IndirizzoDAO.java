package com.cineca.app.backend.DAO;

import java.sql.*;

public class IndirizzoDAO {

    // Inserisce un indirizzo e RITORNA L'ID generato
    // Nota: Riceve la Connection come parametro!
    public static int inserisciIndirizzo(Connection conn, String via, int civico) throws SQLException {
        String sql = "INSERT INTO indirizzi (indirizzo, numero_civico) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, via);
            pstmt.setInt(2, civico);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Ritorna l'ID appena creato
                }
            }
        }
        throw new SQLException("Fallimento creazione indirizzo, nessun ID ottenuto.");
    }

    // Aggiorna un indirizzo esistente
    public static void aggiornaIndirizzo(Connection conn, int id, String via, int civico) throws SQLException {
        String sql = "UPDATE indirizzi SET indirizzo=?, numero_civico=? WHERE id=?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, via);
            ps.setInt(2, civico);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }
}
