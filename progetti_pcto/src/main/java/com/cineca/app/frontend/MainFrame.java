package com.cineca.app.frontend;

import com.cineca.app.backend.modelli.Esame;
import com.cineca.app.backend.modelli.Studente;
import com.cineca.app.backend.service.StudenteService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException; 
import java.util.List;
import java.util.Vector;

@Component
public class MainFrame extends JFrame {

    //Il service serve per usare i Repository per recuperare/salvare i dati, manipolando nel frattempo i Modelli per applicare le regole della tua applicazione
    private final StudenteService service;

    // Variabili GUI
    private JTable tabellaStudenti, tblEsami;
    private DefaultTableModel modelloTabella, modelEsami;
    private JButton btnTestConn, btnInserisci, btnModifica, btnElimina, btnAggiorna, btnModificaEsame, btnAggiungiEsame;

    public MainFrame(StudenteService service) {
        this.service = service;

        setTitle("Gestione Studenti - Spring Boot & Swing");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //al centro dello schermo con null
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        String[] colonne = {
            "ID",                   // 0
            "Nome",                 // 1
            "Cognome",              // 2
            "Luogo Nascita",        // 3
            "Residenza",            // 4 
            "Domicilio",            // 5 
            "ID_Res",               // 6 (Nascosto)
            "Via_Res",              // 7 (Nascosto)
            "Civ_Res",              // 8 (Nascosto)
            "ID_Dom",               // 9 (Nascosto)
            "Via_Dom",              // 10 (Nascosto)
            "Civ_Dom"               // 11 (Nascosto)
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
        nascondiColonna(tabellaStudenti, 11);
        nascondiColonna(tabellaStudenti, 10);
        nascondiColonna(tabellaStudenti, 9);
        nascondiColonna(tabellaStudenti, 8);
        nascondiColonna(tabellaStudenti, 7);
        nascondiColonna(tabellaStudenti, 6);
        nascondiColonna(tabellaStudenti, 0); 
        
        add(new JScrollPane(tabellaStudenti), BorderLayout.CENTER);
        
        JPanel panelEsamiContainer = new JPanel(new BorderLayout());
        panelEsamiContainer.add(new JLabel("Lista Esami dello Studente Selezionato"), BorderLayout.NORTH);
        panelEsamiContainer.add(new JScrollPane(tblEsami), BorderLayout.CENTER);
        panelEsamiContainer.setPreferredSize(new Dimension(400, 0)); 
        add(panelEsamiContainer, BorderLayout.EAST);

        // setuop dei bottoni
        JPanel pannelloBottoni = new JPanel();
        btnTestConn = new JButton("Ricarica Dati");
        btnInserisci = new JButton("Nuovo Studente");
        btnModifica = new JButton("Modifica Studente");
        btnElimina = new JButton("Elimina Studente");
        btnAggiorna = new JButton("Aggiorna View");
        
        btnAggiungiEsame = new JButton("Nuovo Esame");
        btnModificaEsame = new JButton("Modifica Esame");

        pannelloBottoni.add(btnTestConn);
        pannelloBottoni.add(btnInserisci);
        pannelloBottoni.add(btnModifica);
        pannelloBottoni.add(btnElimina);
        pannelloBottoni.add(new JSeparator(SwingConstants.VERTICAL));
        pannelloBottoni.add(btnAggiungiEsame);
        pannelloBottoni.add(btnModificaEsame);

        add(pannelloBottoni, BorderLayout.SOUTH);

        // creazione listener sui pulsanti
        tabellaStudenti.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                caricaEsamiStudente();
            }
        });

        btnTestConn.addActionListener(e -> stampaStudentiConIndirizzi());
        btnAggiorna.addActionListener(e -> stampaStudentiConIndirizzi());
        
        btnInserisci.addActionListener(e -> inserisciStudenteGUI());
        btnModifica.addActionListener(e -> modificaStudenteGUI());
        btnElimina.addActionListener(e -> eliminaStudenteGUI());
        
        btnAggiungiEsame.addActionListener(e -> aggiungiEsameAction());
        btnModificaEsame.addActionListener(e -> modificaEsameGUI());
    }

    private void nascondiColonna(JTable table, int index) {
        table.removeColumn(table.getColumnModel().getColumn(index));
    }

    public void stampaStudentiConIndirizzi() {
        modelloTabella.setRowCount(0);
        List<Studente> lista = service.ottieniTuttiStudenti();

        for (Studente s : lista) {
            Vector<Object> riga = new Vector<>();
            riga.add(s.getId());
            riga.add(s.getNome());
            riga.add(s.getCognome());
            riga.add(s.getLuogoDiNascita());

            String resString = "", domString = "";
            int idRes=0, civRes=0, idDom=0, civDom=0;
            String viaRes="", viaDom="";

            if(s.getResidenza() != null) {
                resString = s.getResidenza().getIndirizzo() + ", " + s.getResidenza().getNumeroCivico();
                idRes = s.getResidenza().getId();
                viaRes = s.getResidenza().getIndirizzo();
                civRes = s.getResidenza().getNumeroCivico();
            }
            if(s.getDomicilio() != null) {
                domString = s.getDomicilio().getIndirizzo() + ", " + s.getDomicilio().getNumeroCivico();
                idDom = s.getDomicilio().getId();
                viaDom = s.getDomicilio().getIndirizzo();
                civDom = s.getDomicilio().getNumeroCivico();
            }

            riga.add(resString);
            riga.add(domString);
            riga.add(idRes);
            riga.add(viaRes);
            riga.add(civRes);
            riga.add(idDom);
            riga.add(viaDom);
            riga.add(civDom);

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
                
                Studente s = service.inserisciStudenteCompleto(
                    txtNome.getText(), txtCognome.getText(), txtLuogo.getText(), 
                    txtViaRes.getText(), numRes, txtViaDom.getText(), numDom
                );
                
                if (s != null) {
                    JOptionPane.showMessageDialog(this, "Inserito con ID: " + s.getId());
                    stampaStudentiConIndirizzi();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        }
    }

    private void modificaStudenteGUI() {
        int riga = tabellaStudenti.getSelectedRow();
        if (riga == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona uno studente.");
            return;
        }

        int modelRow = tabellaStudenti.convertRowIndexToModel(riga);
        
        int id = Integer.parseInt(modelloTabella.getValueAt(modelRow, 0).toString());
        String nome = (String) modelloTabella.getValueAt(modelRow, 1);
        String cognome = (String) modelloTabella.getValueAt(modelRow, 2);
        String luogo = (String) modelloTabella.getValueAt(modelRow, 3);

        JTextField txtNome = new JTextField(nome);
        JTextField txtCognome = new JTextField(cognome);
        JTextField txtLuogo = new JTextField(luogo);

        Object[] message = {
            "Nome:", txtNome,
            "Cognome:", txtCognome,
            "Luogo Nascita:", txtLuogo
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Modifica Studente", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                service.aggiornaStudente(id, txtNome.getText(), txtCognome.getText(), txtLuogo.getText());
                stampaStudentiConIndirizzi();
                JOptionPane.showMessageDialog(this, "Studente aggiornato.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Errore aggiornamento: " + e.getMessage());
            }
        }
    }

    private void eliminaStudenteGUI() {
        int riga = tabellaStudenti.getSelectedRow();
        if (riga == -1) return;
        
        int modelRow = tabellaStudenti.convertRowIndexToModel(riga);
        int id = Integer.parseInt(modelloTabella.getValueAt(modelRow, 0).toString());

        if (JOptionPane.showConfirmDialog(this, "Eliminare?", "Conferma", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                service.eliminaStudente(id);
                stampaStudentiConIndirizzi();
                modelEsami.setRowCount(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Errore eliminazione: " + e.getMessage());
            }
        }
    }

    private void caricaEsamiStudente() {
        int rigaSelezionata = tabellaStudenti.getSelectedRow();
        if (rigaSelezionata == -1) { modelEsami.setRowCount(0); return; }

        int modelRow = tabellaStudenti.convertRowIndexToModel(rigaSelezionata);
        int idStudente = Integer.parseInt(modelloTabella.getValueAt(modelRow, 0).toString());

        modelEsami.setRowCount(0);

        List<Esame> listaEsami = service.getEsamiStudente(idStudente);

        for (Esame e : listaEsami) {
            String votoStr = (e.getVoto() == null) ? "-" : String.valueOf(e.getVoto());
            String sostenutoStr = e.isSostenuto() ? "Sì" : "No";

            modelEsami.addRow(new Object[]{
                e.getId(), 
                e.getMateria(), 
                votoStr, 
                e.getDataEsame(), 
                sostenutoStr
            });
        }
    }

    private void aggiungiEsameAction() {
        int rigaStudente = tabellaStudenti.getSelectedRow();
        if (rigaStudente == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona uno studente!");
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

        Object[] form = { "Materia:", txtMateria, "Data (YYYY-MM-DD):", txtData, "Stato:", chkSostenuto, "Voto:", spnVoto };

        if (JOptionPane.showConfirmDialog(this, form, "Nuovo Esame", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Integer voto = chkSostenuto.isSelected() ? (Integer) spnVoto.getValue() : null;
            
            try {
                // CORREZIONE QUI: Converto String in LocalDate
                LocalDate dataEsame = LocalDate.parse(txtData.getText());

                Esame nuovoEsame = new Esame();
                nuovoEsame.setMateria(txtMateria.getText());
                nuovoEsame.setDataEsame(dataEsame); // Ora passo l'oggetto LocalDate corretto
                nuovoEsame.setSostenuto(chkSostenuto.isSelected());
                nuovoEsame.setVoto(voto);

                service.aggiungiEsame(idStudente, nuovoEsame);
                caricaEsamiStudente();
            } catch (DateTimeParseException dtpe) {
                JOptionPane.showMessageDialog(this, "Formato data non valido! Usa YYYY-MM-DD (es. 2025-06-15)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        }
    }

    private void modificaEsameGUI() {
        int riga = tblEsami.getSelectedRow();
        if (riga == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona un esame dalla lista per modificarlo.");
            return;
        }

        int modelRow = tblEsami.convertRowIndexToModel(riga);
        
        int idEsame = Integer.parseInt(modelEsami.getValueAt(modelRow, 0).toString());
        String vecchiaMateria = (String) modelEsami.getValueAt(modelRow, 1);
        String vecchioVotoStr = modelEsami.getValueAt(modelRow, 2).toString();
        // La data nella tabella è una LocalDate (o la sua rappresentazione toString), la gestiamo come stringa per riempirla
        String vecchiaData = modelEsami.getValueAt(modelRow, 3).toString();
        boolean isSostenuto = "Sì".equals(modelEsami.getValueAt(modelRow, 4));

        JTextField txtMateria = new JTextField(vecchiaMateria);
        JTextField txtData = new JTextField(vecchiaData);
        JCheckBox chkSostenuto = new JCheckBox("Esame sostenuto?", isSostenuto);
        
        int votoIniziale = 18;
        try { votoIniziale = Integer.parseInt(vecchioVotoStr); } catch (NumberFormatException ignored) {}
        
        JSpinner spnVoto = new JSpinner(new SpinnerNumberModel(votoIniziale, 18, 31, 1)); 
        spnVoto.setEnabled(isSostenuto);
        chkSostenuto.addActionListener(e -> spnVoto.setEnabled(chkSostenuto.isSelected()));

        Object[] form = {
            "Materia:", txtMateria,
            "Data (YYYY-MM-DD):", txtData,
            "Stato:", chkSostenuto,
            "Voto:", spnVoto
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Modifica Esame", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // CORREZIONE QUI: Converto String in LocalDate
                LocalDate dataEsame = LocalDate.parse(txtData.getText());

                Esame esameAggiornato = new Esame();
                esameAggiornato.setId(idEsame);
                esameAggiornato.setMateria(txtMateria.getText());
                esameAggiornato.setDataEsame(dataEsame); // Passaggio corretto
                esameAggiornato.setSostenuto(chkSostenuto.isSelected());
                
                if (chkSostenuto.isSelected()) {
                    esameAggiornato.setVoto((Integer) spnVoto.getValue());
                } else {
                    esameAggiornato.setVoto(null);
                }

                service.aggiornaEsame(esameAggiornato);
                caricaEsamiStudente();
                JOptionPane.showMessageDialog(this, "Esame aggiornato!");

            } catch (DateTimeParseException dtpe) {
                JOptionPane.showMessageDialog(this, "Formato data non valido! Usa YYYY-MM-DD (es. 2025-06-15)");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore durante la modifica: " + ex.getMessage());
            }
        }
    }    
}