package com.cineca.app.backend.service;

//importazioni varie per cercare dove trovare i repository e i modelli
import com.cineca.app.backend.modelli.*;
import com.cineca.app.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//definiamo la classe come servizio di spring, se non metto questa annotazione gli @autorwired non funzionano
@Service
public class StudenteService {

    //Autowired serve per la richiesta do strumenti, chiede "l'accesso al database tramite questa interfaccia"
    @Autowired
    private StudenteRepository studenteRepository;
    
    @Autowired
    private EsameRepository esameRepository;

    // Metodo che chiede do dargli tutto, e da indietro direttamente la lista di studenti
    public List<Studente> ottieniTuttiStudenti() {
        return studenteRepository.findAll();
    }

    //Transactional serve per dire a spring che questa operazione deve essere fatta come una transazione, se qualcosa per ASSURD va storto fa il rollback
    // AAAAAAAAAAAAAAATOMICO ( o come cavolo si scrive )
    @Transactional
    public Studente inserisciStudenteCompleto(String nome, String cognome, String luogo, 
                                              String viaRes, int civRes, 
                                              String viaDom, int civDom) {
        // Creo gli indirizzi con i dati grezzi
        Indirizzo residenza = new Indirizzo(viaRes, civRes);
        Indirizzo domicilio = new Indirizzo(viaDom, civDom);

        // Creo lo studente con tutti i suoi dati
        Studente s = new Studente();
        s.setNome(nome);
        s.setCognome(cognome);
        s.setLuogoDiNascita(luogo);
        s.setResidenza(residenza);
        s.setDomicilio(domicilio);

        // Nonostante salva solo lo studente, grazie a CascadeType.ALL nelle entities studente, Spring salverà anche i due indirizzi nel db
        return studenteRepository.save(s);
    }

    // Grazie al db configurato per bene con le foreign key, se cancelli uno studente, cancella anche i suoi esami ed i suoi indirizzi
    @Transactional
    public void eliminaStudente(int id) {
        studenteRepository.deleteById(id);
    }

    @Transactional
    public Esame aggiungiEsame(int idStudente, Esame esame) {
        // Trovo lo studente dato l'id
        Studente s = studenteRepository.findById(idStudente)
                        //orElseThrow serve per gestire il caso in cui lo studente non esista
                        .orElseThrow(() -> new RuntimeException("Studente non trovato"));
        
        // Collego l'esame allo studente tramite la foreign key
        esame.setStudente(s);
        
        // Salvo l'esame nel db ez
        return esameRepository.save(esame);
    }

    public List<Esame> getEsamiStudente(int idStudente) {
        //usa un metodo speciale definito nel repository per prendere tutti gli esami di uno studente
        return esameRepository.findByStudenteId(idStudente);
    }

    @Transactional
    public void aggiornaEsame(Esame datiEsame) {
        // Recupero l'esame esistente dal DB per non perdere il collegamento allo Studente perche la grafica non lo ha, ha solo i dati modificati 
        //cercandolo nel db tramite l'id siamo sicuri di avere l'oggetto vero
        Esame esameEsistente = esameRepository.findById(datiEsame.getId())
                .orElseThrow(() -> new RuntimeException("Esame non trovato con ID: " + datiEsame.getId()));

        // Aggiorno i campi dell'esame esistente con i nuovi dati
        esameEsistente.setMateria(datiEsame.getMateria());
        esameEsistente.setDataEsame(datiEsame.getDataEsame());
        esameEsistente.setSostenuto(datiEsame.isSostenuto());
        esameEsistente.setVoto(datiEsame.getVoto());

        // Salvotramite un comando UPDATE non piu INSERT 
        esameRepository.save(esameEsistente);
    }

    // uguale come per AggiornaEsame ma con lo studente
    @Transactional
    public void aggiornaStudente(int id, String nome, String cognome, String luogo) {
        Studente s = studenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));
        s.setNome(nome);
        s.setCognome(cognome);
        s.setLuogoDiNascita(luogo);
        studenteRepository.save(s);
    }
}