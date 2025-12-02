package com.cineca.app.frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.cineca.app.backend.DatabaseManager;
import com.cineca.app.backend.StudentDAO; // Import aggiunto per DAO

import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class MainFrame extends JFrame {

    //variabili di JFRAME
    private JTable tabellaStudenti;
    private DefaultTableModel modelloTabella;
    private JButton btnTestConn, btnInserisci, btnModifica, btnElimina, btnAggiorna;

    

    public MainFrame() {
        // Impostazioni base JFrame
        setTitle("Gestione Studenti Database");
        setSize(1000, 600); // Un po' più larga per vedere bene
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Aggiungiamo colonne "nascoste" per avere i dati pronti per la modifica
        String[] colonne = {
            "ID",                   // 0: ID Studente (Nascosto)
            "Nome",                 // 1
            "Cognome",              // 2
            "Luogo Nascita",        // 3
            "Residenza",            // 4: Visualizzata unita
            "Domicilio",            // 5: Visualizzata unita
            "ID_Res",               // 6: Nascosto
            "Via_Res",              // 7: Nascosto
            "Civ_Res",              // 8: Nascosto
            "ID_Dom",               // 9: Nascosto
            "Via_Dom",              // 10: Nascosto
            "Civ_Dom"               // 11: Nascosto
        };
        
        //classe anonima, qui creiamo la tabella data la classe padre e poi override il metodo isCellEditable
        //per evitare che l'utente possa modificare i campi direttamente dall GUI
        modelloTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        //tabella creata a riga 16 definita
        tabellaStudenti = new JTable(modelloTabella);
        
        // Nascondiamo le colonne tecniche (ID e i dati grezzi degli indirizzi)
        // L'utente vede solo fino alla colonna 5, ovvero Domicilio
        tabellaStudenti.removeColumn(tabellaStudenti.getColumnModel().getColumn(11)); // Civ Dom
        tabellaStudenti.removeColumn(tabellaStudenti.getColumnModel().getColumn(10)); // Via Dom
        tabellaStudenti.removeColumn(tabellaStudenti.getColumnModel().getColumn(9));  // ID Dom
        tabellaStudenti.removeColumn(tabellaStudenti.getColumnModel().getColumn(8));  // Civ Res
        tabellaStudenti.removeColumn(tabellaStudenti.getColumnModel().getColumn(7));  // Via Res
        tabellaStudenti.removeColumn(tabellaStudenti.getColumnModel().getColumn(6));  // ID Res
        tabellaStudenti.removeColumn(tabellaStudenti.getColumnModel().getColumn(0));  // ID Studente
        
        //fa apparire la scrollbar se i dati superano lo spazio della tabella
        add(new JScrollPane(tabellaStudenti), BorderLayout.CENTER);


        JPanel pannelloBottoni = new JPanel();

        //definiamo i pulsanti istanziati a riga 17
        btnTestConn = new JButton("Test Connessione");
        btnInserisci = new JButton("Inserisci Studente");
        btnModifica = new JButton("Modifica Completa");
        btnElimina = new JButton("Elimina Studente");
        btnAggiorna = new JButton("Aggiorna Tabella");

        //li aggiungiamo direttametnte al pannello
        pannelloBottoni.add(btnTestConn);
        pannelloBottoni.add(btnInserisci);
        pannelloBottoni.add(btnModifica);
        pannelloBottoni.add(btnElimina);
        pannelloBottoni.add(btnAggiorna);

        //aggiungiamo il pannnello dei bottomi al frame
        add(pannelloBottoni, BorderLayout.SOUTH);

        //Aggiungiamo ad ogni pulsante un event listener come in javascript
        btnTestConn.addActionListener(e -> testConnessione());
        btnInserisci.addActionListener(e -> inserisciStudenteGUI());
        btnModifica.addActionListener(e -> modificaStudenteGUI());
        btnElimina.addActionListener(e -> eliminaStudenteGUI());
        btnAggiorna.addActionListener(e -> stampaStudentiConIndirizzi());

        //stampa la tabella in ogni caso
        stampaStudentiConIndirizzi();
    }

    private void testConnessione() {
        // La logica di test connessione rimane qui
        try (Connection conn = DatabaseManager.ottieniConnessione()) {
            JOptionPane.showMessageDialog(this, "Connessione OK!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errore: " + e.getMessage());
        }
    }

    private void inserisciStudenteGUI() {
        // Form di inserimento
        JPanel panel = new JPanel(new GridLayout(0, 2));

        //campo di input per ogni field
        JTextField txtNome = new JTextField();
        JTextField txtCognome = new JTextField();
        JTextField txtLuogo = new JTextField();
        JTextField txtViaRes = new JTextField();
        JTextField txtNumRes = new JTextField();
        JTextField txtViaDom = new JTextField();
        JTextField txtNumDom = new JTextField();

        //aggiunge prima il label, poi il campo di input al pannello
        panel.add(new JLabel("Nome:")); panel.add(txtNome);
        panel.add(new JLabel("Cognome:")); panel.add(txtCognome);
        panel.add(new JLabel("Luogo Nascita:")); panel.add(txtLuogo);
        panel.add(new JLabel("--- RESIDENZA ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Via:")); panel.add(txtViaRes);
        panel.add(new JLabel("Civico:")); panel.add(txtNumRes);
        panel.add(new JLabel("--- DOMICILIO ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Via:")); panel.add(txtViaDom);
        panel.add(new JLabel("Civico:")); panel.add(txtNumDom);

        //guardiamo il risultato del popup, showConfirmDialog ritorna OK o CANCEL, dal pannello, ultimo parametro serve per
        //mettere i pulsanti OK e CANCEL
        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo Studente", JOptionPane.OK_CANCEL_OPTION);

        //guardiamo se dal popup l'utente ha premuto OK
        if (result == JOptionPane.OK_OPTION) {
            try {
                //conversione da stringa campo di input a intero
                int numRes = Integer.parseInt(txtNumRes.getText());
                int numDom = Integer.parseInt(txtNumDom.getText());
                
                // CAMBIAMENTO: Chiama il DAO per eseguire l'inserimento e la transazione
                boolean ok = StudentDAO.inserisciStudente(txtNome.getText(), txtCognome.getText(), txtLuogo.getText(), 
                                                                    txtViaRes.getText(), numRes, txtViaDom.getText(), numDom);
                
                if (ok) {
                    //stampa sto messagguo solo se inserimento andato a buon fine
                    JOptionPane.showMessageDialog(this, "Inserito con successo!");
                    stampaStudentiConIndirizzi();
                } else {
                    JOptionPane.showMessageDialog(this, "Errore durante l'inserimento nel DB (Transazione Fallita).");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "I civici devono essere numeri!");
            }
        }
    }

    // NUOVA FUNZIONE MODIFICA COMPLETA
    private void modificaStudenteGUI() {

        //prende la riga selezionata dall'utente
        int rigaSelezionata = tabellaStudenti.getSelectedRow();

        //se non ne seleziona nessuna, mostra sto messaggio
        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona uno studente!");
            return;
        }

        //la table è formata da parte utente e parte modello, convertiamo quindi l'intero della parte utente nell
        //intero assoluto del modello dove ci sono i dati
        int modelRow = tabellaStudenti.convertRowIndexToModel(rigaSelezionata);

        // Recuperiamo TUTTI i dati (anche quelli nascosti)
        // La logica di recupero dati dalla GUI rimane qui
        //recupero id univoco
        int idStudente = (int) modelloTabella.getValueAt(modelRow, 0);
        
        // Dati Anagrafici
        String nome = (String) modelloTabella.getValueAt(modelRow, 1);
        String cognome = (String) modelloTabella.getValueAt(modelRow, 2);
        String luogo = (String) modelloTabella.getValueAt(modelRow, 3);
        
        // Dati Residenza (Nascosti)
        int idRes = (int) modelloTabella.getValueAt(modelRow, 6);
        String viaRes = (String) modelloTabella.getValueAt(modelRow, 7);
        int civRes = (int) modelloTabella.getValueAt(modelRow, 8);
        
        // Dati Domicilio (Nascosti)
        int idDom = (int) modelloTabella.getValueAt(modelRow, 9);
        String viaDom = (String) modelloTabella.getValueAt(modelRow, 10);
        int civDom = (int) modelloTabella.getValueAt(modelRow, 11);

        // Creiamo il Form pre-compilato
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField txtNome = new JTextField(nome);
        JTextField txtCognome = new JTextField(cognome);
        JTextField txtLuogo = new JTextField(luogo);
        
        JTextField txtViaRes = new JTextField(viaRes);
        JTextField txtCivRes = new JTextField(String.valueOf(civRes));
        
        JTextField txtViaDom = new JTextField(viaDom);
        JTextField txtCivDom = new JTextField(String.valueOf(civDom));

        panel.add(new JLabel("Nome:")); panel.add(txtNome);
        panel.add(new JLabel("Cognome:")); panel.add(txtCognome);
        panel.add(new JLabel("Luogo Nascita:")); panel.add(txtLuogo);
        panel.add(new JLabel("--- RESIDENZA ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Via:")); panel.add(txtViaRes);
        panel.add(new JLabel("Civico:")); panel.add(txtCivRes);
        panel.add(new JLabel("--- DOMICILIO ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Via:")); panel.add(txtViaDom);
        panel.add(new JLabel("Civico:")); panel.add(txtCivDom);

        //crea il popup e salva la scelta dell'utente
        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica Studente e Indirizzi", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int newCivRes = Integer.parseInt(txtCivRes.getText());
                int newCivDom = Integer.parseInt(txtCivDom.getText());

                // CAMBIAMENTO: Chiama il DAO per eseguire la modifica transazionale (3 UPDATE)
                boolean ok = StudentDAO.eseguiModificaCompleta(idStudente, idRes, idDom, 
                                            txtNome.getText(), txtCognome.getText(), txtLuogo.getText(),
                                            txtViaRes.getText(), newCivRes,
                                            txtViaDom.getText(), newCivDom);
                if (ok) {
                    //se la query va a buon fine
                    JOptionPane.showMessageDialog(this, "Modifica effettuata!");
                    stampaStudentiConIndirizzi();
                } else {
                    JOptionPane.showMessageDialog(this, "Errore nella modifica DB (Transazione Fallita).");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Errore: I civici devono essere numeri.");
            }
        }
    }

    private void eliminaStudenteGUI() {
        int riga = tabellaStudenti.getSelectedRow();
        if (riga == -1) return;
        
        int modelRow = tabellaStudenti.convertRowIndexToModel(riga);
        int id = (int) modelloTabella.getValueAt(modelRow, 0);

        //guarda se l'utente nella pagina creata con pulsannti YES_NO_OPTION ha premuto YES
        if (JOptionPane.showConfirmDialog(this, "Eliminare?", "Conferma", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            
            // CAMBIAMENTO: Chiama il DAO per eseguire la DELETE
            boolean ok = StudentDAO.eliminaStudente(id);
            
            if (ok) {
                stampaStudentiConIndirizzi();
                JOptionPane.showMessageDialog(this, "Eliminato.");
            } else {
                JOptionPane.showMessageDialog(this, "Errore durante l'eliminazione.");
            }
        }
    }


    public void stampaStudentiConIndirizzi() {
        modelloTabella.setRowCount(0);
        // Recuperiamo tutti i dati necessari per la tabella e per la modifica futura
        
        // CAMBIAMENTO: Chiama il DAO per ottenere tutti i dati già pre-elaborati
        Vector<Vector<Object>> datiStudenti = StudentDAO.ottieniTuttiStudentiCompleti();

        for (Vector<Object> riga : datiStudenti) {
            modelloTabella.addRow(riga);
        }
    }
    
    // da questa classe (MainFrame) perché la loro logica (JDBC/SQL/Transazioni) 
    // è stata spostata interamente in StudentDAO per una corretta architettura.
}