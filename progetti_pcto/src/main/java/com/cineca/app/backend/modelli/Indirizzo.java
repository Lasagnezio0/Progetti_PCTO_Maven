package com.cineca.app.backend.modelli;

import jakarta.persistence.*;

@Entity
@Table(name = "indirizzi")
public class Indirizzo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String indirizzo; 
    
    @Column(name = "numero_civico")
    private Integer numeroCivico;

    // Costruttore vuoto (obbligatorio per JPA)
    public Indirizzo() {}

    // Costruttore utile per noi
    public Indirizzo(String indirizzo, Integer numeroCivico) {
        this.indirizzo = indirizzo;
        this.numeroCivico = numeroCivico;
    }

    // Getter e Setter (Fondamentali!)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    public Integer getNumeroCivico() { return numeroCivico; }
    public void setNumeroCivico(Integer numeroCivico) { this.numeroCivico = numeroCivico; }
}
