package com.cineca.app.backend.modelli;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "studenti")
public class Studente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    private String cognome;
    
    @Column(name = "luogo_di_nascita")
    private String luogoDiNascita;

    // Relazione con Indirizzo (Residenza), un solo indirizzo per studente
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "residenza_id")
    private Indirizzo residenza;

    // Relazione con Indirizzo (Domicilio)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "domicilio_id")
    private Indirizzo domicilio;

    // Relazione con Esami, lo studente può avere molti esami
    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Esame> esami = new ArrayList<>();

    public Studente() {}

    // Metodo di utilità per aggiungere esami facilmente
    public void addEsame(Esame esame) {
        esami.add(esame);
        esame.setStudente(this);
    }

    // Getter e Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getLuogoDiNascita() { return luogoDiNascita; }
    public void setLuogoDiNascita(String luogoDiNascita) { this.luogoDiNascita = luogoDiNascita; }
    public Indirizzo getResidenza() { return residenza; }
    public void setResidenza(Indirizzo residenza) { this.residenza = residenza; }
    public Indirizzo getDomicilio() { return domicilio; }
    public void setDomicilio(Indirizzo domicilio) { this.domicilio = domicilio; }
    public List<Esame> getEsami() { return esami; }
    public void setEsami(List<Esame> esami) { this.esami = esami; }
}