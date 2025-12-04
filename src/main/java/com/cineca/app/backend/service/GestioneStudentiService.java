package com.cineca.app.backend.service;

import com.cineca.app.backend.config.DatabaseManager; 

import com.cineca.app.backend.DAO.StudentDAO;
import com.cineca.app.backend.DAO.IndirizzoDAO;
import com.cineca.app.backend.DAO.EsameDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

public class GestioneStudentiService {

    // --- CONNESSIONE ---
    public boolean checkConnessione() {
        try (Connection conn = DatabaseManager.ottieniConnessione()) {
            return true;
        } catch (SQLException e) {
            System.err.println("Service DB Error: " + e.getMessage());
            return false;
        }
    }

    // --- STUDENTI ---
    
    // Lista completa (Lettura)
    public Vector<Vector<Object>> ottieniListaStudenti() {
        try (Connection conn = DatabaseManager.ottieniConnessione()) {
            return StudentDAO.ottieniTuttiStudentiCompleti(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Vector<>();
        }
    }

    // Inserimento Studente + Indirizzi (Transazione)
    public boolean inserisciStudenteCompleto(String nome, String cognome, String luogo, String viaRes, int civRes, String viaDom, int civDom) {
        Connection conn = null;
        try {
            conn = DatabaseManager.ottieniConnessione();
            conn.setAutoCommit(false); // START TRANSACTION

            int idRes = IndirizzoDAO.inserisciIndirizzo(conn, viaRes, civRes);
            int idDom = IndirizzoDAO.inserisciIndirizzo(conn, viaDom, civDom);
            StudentDAO.inserisciStudente(conn, nome, cognome, luogo, idRes, idDom);

            conn.commit(); // COMMIT
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    // Modifica Studente + Indirizzi (Transazione)
    public boolean aggiornaStudenteCompleto(int idStudente, int idRes, int idDom, String nome, String cognome, String luogo, String viaRes, int civRes, String viaDom, int civDom) {
        Connection conn = null;
        try {
            conn = DatabaseManager.ottieniConnessione();
            conn.setAutoCommit(false); // START TRANSACTION

            StudentDAO.aggiornaDatiStudente(conn, idStudente, nome, cognome, luogo);
            IndirizzoDAO.aggiornaIndirizzo(conn, idRes, viaRes, civRes);
            IndirizzoDAO.aggiornaIndirizzo(conn, idDom, viaDom, civDom);

            conn.commit(); // COMMIT
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    // Elimina Studente (A cascata: Esami -> Studente)
    public boolean eliminaStudente(int idStudente) {
        Connection conn = null;
        try {
            conn = DatabaseManager.ottieniConnessione();
            conn.setAutoCommit(false); // START TRANSACTION

            // 1. Elimina prima gli esami (altrimenti la Foreign Key blocca tutto)
            EsameDAO.eliminaEsamiDiStudente(conn, idStudente);
            
            // 2. Elimina lo studente
            boolean deleted = StudentDAO.eliminaStudente(conn, idStudente);

            if (deleted) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    // --- ESAMI (Nuovi metodi che usano EsameDAO aggiornato) ---

    // Get Esami
    public List<Object[]> getEsamiStudente(int idStudente) {
        try (Connection conn = DatabaseManager.ottieniConnessione()) {
            // Passiamo la connessione al DAO
            return EsameDAO.getEsamiByStudenteId(conn, idStudente);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Aggiungi Esame
    public boolean aggiungiEsame(int idStudente, String materia, Integer voto, String data, boolean sostenuto) {
        Connection conn = null;
        try {
            conn = DatabaseManager.ottieniConnessione();
            // Anche per una singola insert usiamo il pattern Service per coerenza
            conn.setAutoCommit(false); 

            EsameDAO.aggiungiEsame(conn, idStudente, materia, voto, data, sostenuto);

            conn.commit();
            return true;
        } catch (Exception e) { // Catch Exception generica per prendere anche errori data
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    // Aggiorna Esame
    public boolean aggiornaEsame(int idEsame, String materia, String data, Integer voto, boolean sostenuto) {
        Connection conn = null;
        try {
            conn = DatabaseManager.ottieniConnessione();
            conn.setAutoCommit(false);

            EsameDAO.aggiornaEsame(conn, idEsame, materia, data, voto, sostenuto);

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
}