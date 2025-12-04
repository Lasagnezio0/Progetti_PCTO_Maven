package com.cineca.app.backend.DAO; 

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EsameDAO {

    // 1. GET ESAMI (Riceve la connessione, lancia eccezione)
    public static List<Object[]> getEsamiByStudenteId(Connection conn, int idStudente) throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        String query = "SELECT id, materia, voto, data_esame, sostenuto FROM esami WHERE studente_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, idStudente);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String materia = rs.getString("materia");
                    Date data = rs.getDate("data_esame");
                    boolean sostenuto = rs.getBoolean("sostenuto");
                    
                    Integer voto = rs.getInt("voto");
                    if (rs.wasNull()) voto = null;

                    lista.add(new Object[]{ id, materia, voto, data, sostenuto });
                }
            }
        }
        return lista;
    }

    // 2. AGGIUNGI ESAME (Riceve connection, ritorna void, lancia eccezione)
    public static void aggiungiEsame(Connection conn, int idStudente, String materia, Integer voto, String dataString, boolean sostenuto) throws SQLException {
        String query = "INSERT INTO esami (studente_id, materia, voto, data_esame, sostenuto) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, idStudente);
            pst.setString(2, materia);

            if (voto == null) {
                pst.setNull(3, Types.INTEGER);
            } else {
                pst.setInt(3, voto);
            }

            pst.setDate(4, Date.valueOf(dataString));
            pst.setBoolean(5, sostenuto);

            pst.executeUpdate();
        }
    }

    // 3. AGGIORNA ESAME
    public static void aggiornaEsame(Connection conn, int idEsame, String materia, String dataString, Integer voto, boolean sostenuto) throws SQLException {
        String query = "UPDATE esami SET materia = ?, data_esame = ?, voto = ?, sostenuto = ? WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, materia);
            pst.setDate(2, Date.valueOf(dataString));

            if (voto == null) {
                pst.setNull(3, Types.INTEGER);
            } else {
                pst.setInt(3, voto);
            }

            pst.setBoolean(4, sostenuto);
            pst.setInt(5, idEsame);

            pst.executeUpdate();
        }
    }

    // 4. ELIMINA ESAMI DI UNO STUDENTE (Nuovo! Serve per eliminare lo studente a cascata)
    public static void eliminaEsamiDiStudente(Connection conn, int idStudente) throws SQLException {
        String query = "DELETE FROM esami WHERE studente_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, idStudente);
            pst.executeUpdate();
        }
    }
}