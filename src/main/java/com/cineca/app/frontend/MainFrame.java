//1. mettere anche gli esami, quando scelgo uno studente e lo seleziono, sotto appaiono gli esami 👌👍
//strato service necessario per fre da interfaccia con il mondo esterno 👌👍


//3 DAO differenti per ogni tabella, non una sola per tute e 3, ed il service che le usa tutte e 3 e decide di fare commit e rollback, non piu le classi DAO👌👍
//magari un dao chiama un altro DAO anche se serve 👌👍

//fare una classe ADAO   
    //costruttore parametro connection
    //tutti e 3 i dao devono implementare interfaccia comune

    //factory dei DAO --> Puoi centralizzare la creazione di DAO 
    //nei service esponiamo i DAO, che faranno la connessione tramite la factory, ed il service fa dao o rollback
    //miglioreremo il progetto poi verso springboot, modificando la parte backend, facendola diventare un servizio RESTful
    //al posto di swing poi siutilizza php oppure angular o react per fare il frontend
    //progettarla come una API, service(meglio farlo in springboot), fare da DOPO


package com.cineca.app.frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.cineca.app.backend.service.GestioneStudentiService;

import java.awt.*;
import java.util.Vector;
import java.util.List; 

public class MainFrame extends JFrame {

    // VARIABILE FONDAMENTALE: Lo strato Service
    private GestioneStudentiService service;

    // Variabili GUI
    private JTable tabellaStudenti, tblEsami;
    private DefaultTableModel modelloTabella, modelEsami;
    private JButton btnTestConn, btnInserisci, btnModifica, btnElimina, btnAggiorna, btnModificaEsame, btnAggiungiEsame;

    public MainFrame() {
        // 1. INIZIALIZZAZIONE SERVICE
        service = new GestioneStudentiService();

        // Impostazioni base JFrame
        setTitle("Gestione Studenti - Architettura Service Layer");
        setSize(1100, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SETUP MODELLI TABELLE ---
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

        String[] colEsami = { "ID", "Materia", "Voto", "Data", "Sostenuto" };
        
        modelloTabella = new DefaultTableModel(colonne, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        modelEsami = new DefaultTableModel(colEsami, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
       
        tabellaStudenti = new JTable(modelloTabella);
        tblEsami = new JTable(modelEsami);
        
        // Nascondiamo le colonne tecniche
        nascondiColonna(tabellaStudenti, 11); // Civ Dom
        nascondiColonna(tabellaStudenti, 10); // Via Dom
        nascondiColonna(tabellaStudenti, 9);  // ID Dom
        nascondiColonna(tabellaStudenti, 8);  // Civ Res
        nascondiColonna(tabellaStudenti, 7);  // Via Res
        nascondiColonna(tabellaStudenti, 6);  // ID Res
        nascondiColonna(tabellaStudenti, 0);  // ID Studente
        
        // Layout Tabelle
        add(new JScrollPane(tabellaStudenti), BorderLayout.CENTER);
        
        JPanel panelEsamiContainer = new JPanel(new BorderLayout());
        panelEsamiContainer.add(new JLabel("Lista Esami dello Studente Selezionato"), BorderLayout.NORTH);
        panelEsamiContainer.add(new JScrollPane(tblEsami), BorderLayout.CENTER);
        panelEsamiContainer.setPreferredSize(new Dimension(400, 0)); 
        add(panelEsamiContainer, BorderLayout.EAST);

        // --- SETUP BOTTONI ---
        JPanel pannelloBottoni = new JPanel();
        btnTestConn = new JButton("Test DB");
        btnInserisci = new JButton("Nuovo Studente");
        btnModifica = new JButton("Modifica Studente");
        btnElimina = new JButton("Elimina Studente");
        btnAggiorna = new JButton("Aggiorna Liste");
        
        btnAggiungiEsame = new JButton("Nuovo Esame");
        btnModificaEsame = new JButton("Modifica Esame");

        pannelloBottoni.add(btnTestConn);
        pannelloBottoni.add(btnInserisci);
        pannelloBottoni.add(btnModifica);
        pannelloBottoni.add(btnElimina);
        pannelloBottoni.add(btnAggiorna);
        pannelloBottoni.add(new JSeparator(SwingConstants.VERTICAL));
        pannelloBottoni.add(btnAggiungiEsame);
        pannelloBottoni.add(btnModificaEsame);

        add(pannelloBottoni, BorderLayout.SOUTH);

        // --- LISTENER ---
        
        // Listener Tabella: quando seleziono studente, chiedo al service gli esami
        tabellaStudenti.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                caricaEsamiStudente();
            }
        });

        // Listener Bottoni -> Chiamano metodi privati che usano il SERVICE
        btnTestConn.addActionListener(e -> testConnessione());
        btnInserisci.addActionListener(e -> inserisciStudenteGUI());
        btnModifica.addActionListener(e -> modificaStudenteGUI());
        btnElimina.addActionListener(e -> eliminaStudenteGUI());
        btnAggiorna.addActionListener(e -> stampaStudentiConIndirizzi());
        btnAggiungiEsame.addActionListener(e -> aggiungiEsameAction());
        btnModificaEsame.addActionListener(e -> modificaEsameGUI());

        // Caricamento iniziale
        stampaStudentiConIndirizzi();
    }

    // Helper per nascondere colonne in modo pulito
    private void nascondiColonna(JTable table, int index) {
        table.removeColumn(table.getColumnModel().getColumn(index));
    }

    // =================================================================================
    // METODI LOGICI (TUTTI RIFATTI PER USARE IL SERVICE INVECE DEI DAO)
    // =================================================================================

    private void testConnessione() {
        // USIAMO IL SERVICE
        boolean ok = service.checkConnessione();
        if(ok){
            JOptionPane.showMessageDialog(this, "Connessione OK!");
        } else {
            JOptionPane.showMessageDialog(this, "Errore di connessione al DB.");
        }
    }

    public void stampaStudentiConIndirizzi() {
        modelloTabella.setRowCount(0);
        // USIAMO IL SERVICE: Non sappiamo come prende i dati, ci fidiamo
        Vector<Vector<Object>> datiStudenti = service.ottieniListaStudenti();

        for (Vector<Object> riga : datiStudenti) {
            modelloTabella.addRow(riga);
        }
    }

    private void inserisciStudenteGUI() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField txtNome = new JTextField(); JTextField txtCognome = new JTextField();
        JTextField txtLuogo = new JTextField(); JTextField txtViaRes = new JTextField();
        JTextField txtNumRes = new JTextField(); JTextField txtViaDom = new JTextField();
        JTextField txtNumDom = new JTextField();

        panel.add(new JLabel("Nome:")); panel.add(txtNome);
        panel.add(new JLabel("Cognome:")); panel.add(txtCognome);
        panel.add(new JLabel("Luogo Nascita:")); panel.add(txtLuogo);
        panel.add(new JLabel("--- RESIDENZA ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Via:")); panel.add(txtViaRes);
        panel.add(new JLabel("Civico:")); panel.add(txtNumRes);
        panel.add(new JLabel("--- DOMICILIO ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Via:")); panel.add(txtViaDom);
        panel.add(new JLabel("Civico:")); panel.add(txtNumDom);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo Studente", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int numRes = Integer.parseInt(txtNumRes.getText());
                int numDom = Integer.parseInt(txtNumDom.getText());
                
                // USIAMO IL SERVICE: Gestisce lui la transazione complessa
                boolean ok = service.inserisciStudenteCompleto(
                    txtNome.getText(), txtCognome.getText(), txtLuogo.getText(), 
                    txtViaRes.getText(), numRes, txtViaDom.getText(), numDom
                );
                
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Inserito con successo!");
                    stampaStudentiConIndirizzi();
                } else {
                    JOptionPane.showMessageDialog(this, "Errore durante l'inserimento (Transazione Rollback).");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "I civici devono essere numeri!");
            }
        }
    }

    private void modificaStudenteGUI() {
        int rigaSelezionata = tabellaStudenti.getSelectedRow();
        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona uno studente!");
            return;
        }

        // Convertire indice View -> Indice Model
        int modelRow = tabellaStudenti.convertRowIndexToModel(rigaSelezionata);

        // Recupero dati attuali dal modello
        int idStudente = Integer.parseInt(modelloTabella.getValueAt(modelRow, 0).toString());
        String nome = (String) modelloTabella.getValueAt(modelRow, 1);
        String cognome = (String) modelloTabella.getValueAt(modelRow, 2);
        String luogo = (String) modelloTabella.getValueAt(modelRow, 3);
        int idRes = (int) modelloTabella.getValueAt(modelRow, 6);
        String viaRes = (String) modelloTabella.getValueAt(modelRow, 7);
        int civRes = (int) modelloTabella.getValueAt(modelRow, 8);
        int idDom = (int) modelloTabella.getValueAt(modelRow, 9);
        String viaDom = (String) modelloTabella.getValueAt(modelRow, 10);
        int civDom = (int) modelloTabella.getValueAt(modelRow, 11);

        // Form
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField txtNome = new JTextField(nome); JTextField txtCognome = new JTextField(cognome);
        JTextField txtLuogo = new JTextField(luogo); JTextField txtViaRes = new JTextField(viaRes);
        JTextField txtCivRes = new JTextField(String.valueOf(civRes)); JTextField txtViaDom = new JTextField(viaDom);
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

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica Studente", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int newCivRes = Integer.parseInt(txtCivRes.getText());
                int newCivDom = Integer.parseInt(txtCivDom.getText());

                // USIAMO IL SERVICE: Aggiorna 3 tabelle in una volta sola
                boolean ok = service.aggiornaStudenteCompleto(
                    idStudente, idRes, idDom, 
                    txtNome.getText(), txtCognome.getText(), txtLuogo.getText(),
                    txtViaRes.getText(), newCivRes, txtViaDom.getText(), newCivDom
                );

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Modifica avvenuta con successo!");
                    stampaStudentiConIndirizzi();
                } else {
                    JOptionPane.showMessageDialog(this, "Errore durante la modifica.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Errore: I civici devono essere numeri.");
            }
        }
    }

    private void eliminaStudenteGUI() {
        int riga = tabellaStudenti.getSelectedRow();
        if (riga == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona uno studente da eliminare.");
            return;
        }
        
        int modelRow = tabellaStudenti.convertRowIndexToModel(riga);
        int id = Integer.parseInt(modelloTabella.getValueAt(modelRow, 0).toString());

        if (JOptionPane.showConfirmDialog(this, "Eliminare definitivamente?", "Conferma", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            
            // USIAMO IL SERVICE: Elimina prima esami, poi studente (gestione FK)
            boolean ok = service.eliminaStudente(id);
            
            if (ok) {
                stampaStudentiConIndirizzi();
                modelEsami.setRowCount(0); // Pulisce tabella esami
                JOptionPane.showMessageDialog(this, "Eliminato.");
            } else {
                JOptionPane.showMessageDialog(this, "Errore durante l'eliminazione.");
            }
        }
    }

    private void caricaEsamiStudente() {
        int rigaSelezionata = tabellaStudenti.getSelectedRow();
        if (rigaSelezionata == -1) { modelEsami.setRowCount(0); return; }

        int modelRow = tabellaStudenti.convertRowIndexToModel(rigaSelezionata);
        int idStudente = Integer.parseInt(modelloTabella.getValueAt(modelRow, 0).toString());

        modelEsami.setRowCount(0);

        // USIAMO IL SERVICE: Ottiene lista esami
        List<Object[]> listaEsami = service.getEsamiStudente(idStudente);

        for (Object[] esame : listaEsami) {
            // esame[] = {id, materia, voto, data, sostenuto}
            boolean isSostenuto = (boolean) esame[4];
            Object testoVoto = (esame[2] == null) ? "-" : esame[2];

            modelEsami.addRow(new Object[]{
                esame[0], esame[1], testoVoto, esame[3], (isSostenuto ? "Sì" : "No")
            });
        }
    }

    private void aggiungiEsameAction() {
        int rigaStudente = tabellaStudenti.getSelectedRow();
        if (rigaStudente == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona uno studente prima!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabellaStudenti.convertRowIndexToModel(rigaStudente);
        int idStudente = Integer.parseInt(modelloTabella.getValueAt(modelRow, 0).toString());

        JTextField txtMateria = new JTextField();
        JTextField txtData = new JTextField("2025-06-15");
        JCheckBox chkSostenuto = new JCheckBox("Già sostenuto?");
        JSpinner spnVoto = new JSpinner(new SpinnerNumberModel(18, 0, 30, 1));
        spnVoto.setEnabled(false);
        chkSostenuto.addActionListener(e -> spnVoto.setEnabled(chkSostenuto.isSelected()));

        Object[] form = { "Materia:", txtMateria, "Data:", txtData, "Stato:", chkSostenuto, "Voto:", spnVoto };

        if (JOptionPane.showConfirmDialog(this, form, "Nuovo Esame", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Integer voto = chkSostenuto.isSelected() ? (Integer) spnVoto.getValue() : null;
            
            // USIAMO IL SERVICE
            boolean successo = service.aggiungiEsame(idStudente, txtMateria.getText(), voto, txtData.getText(), chkSostenuto.isSelected());

            if (successo) {
                JOptionPane.showMessageDialog(this, "Esame aggiunto!");
                caricaEsamiStudente();
            } else {
                JOptionPane.showMessageDialog(this, "Errore inserimento (Data valida?).");
            }
        }
    }

    private void modificaEsameGUI() {
        int rigaEsame = tblEsami.getSelectedRow();
        if (rigaEsame == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona un esame!");
            return;
        }

        if (tblEsami.getValueAt(rigaEsame, 4).toString().equalsIgnoreCase("Sì")) {
            JOptionPane.showMessageDialog(this, "Esame già verbalizzato, non modificabile!", "Stop", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Recupero dati attuali
        int idEsame = Integer.parseInt(tblEsami.getValueAt(rigaEsame, 0).toString());
        String mat = tblEsami.getValueAt(rigaEsame, 1).toString();
        String dat = tblEsami.getValueAt(rigaEsame, 3).toString();

        JTextField txtMateria = new JTextField(mat);
        JTextField txtData = new JTextField(dat);
        JCheckBox chk = new JCheckBox("Verbalizza ora?");
        JSpinner spnVoto = new JSpinner(new SpinnerNumberModel(18, 0, 30, 1));
        spnVoto.setEnabled(false);
        chk.addActionListener(e -> spnVoto.setEnabled(chk.isSelected()));

        Object[] form = { "Materia:", txtMateria, "Data:", txtData, "Stato:", chk, "Voto:", spnVoto };

        if (JOptionPane.showConfirmDialog(this, form, "Modifica Esame", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Integer voto = chk.isSelected() ? (Integer) spnVoto.getValue() : null;

            // USIAMO IL SERVICE
            boolean successo = service.aggiornaEsame(idEsame, txtMateria.getText(), txtData.getText(), voto, chk.isSelected());

            if (successo) {
                JOptionPane.showMessageDialog(this, "Aggiornato!");
                caricaEsamiStudente();
            } else {
                JOptionPane.showMessageDialog(this, "Errore aggiornamento.");
            }
        }
    }    
}