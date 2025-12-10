package com.cineca.app.backend.modelli;

// jakarta.persistence.* -> Contiene le annotazioni standard per i database (@Entity, @Id, ecc.)
import jakarta.persistence.*;
import java.time.LocalDate;
//Serve per evitare problemi quando stampi un oggeto
import com.fasterxml.jackson.annotation.JsonIgnore;

//questa classe rappresenta la tabella esami nel database
@Entity
@Table(name = "esami")

public class Esame {

    //indica che questo campo è la chiave primaria della tabella
    @Id
    //non decidere tu l'id, lo fai fare al database in automatico con l autoincremento
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //altre colonne
    private String materia;
    private Integer voto;
    
    @Column(name = "data_esame")
    private LocalDate dataEsame;
    
    private boolean sostenuto;

    //molti esami possono appartenere a uno studente
    //fetch type serve per dire a JPA di non caricare subito lo studente quando carica l'esame, ma solo quando serve, piu veloce
    @ManyToOne(fetch = FetchType.LAZY)
    //indica la colonna che fa da foreign key nella tabella esami
    @JoinColumn(name = "studente_id")
    //evita il loop infinito quando stampi uno studente che ha esami che hanno studenti che hanno esami...
    @JsonIgnore 
    private Studente studente;

    //costruttore vuoto per Hibernate/JPA
    public Esame() {}

    // Getter e Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getMateria() { return materia; }
    public void setMateria(String materia) { this.materia = materia; }
    public Integer getVoto() { return voto; }
    public void setVoto(Integer voto) { this.voto = voto; }
    public LocalDate getDataEsame() { return dataEsame; }
    public void setDataEsame(LocalDate dataEsame) { this.dataEsame = dataEsame; }
    public boolean isSostenuto() { return sostenuto; }
    public void setSostenuto(boolean sostenuto) { this.sostenuto = sostenuto; }
    public Studente getStudente() { return studente; }
    public void setStudente(Studente studente) { this.studente = studente; }
}
