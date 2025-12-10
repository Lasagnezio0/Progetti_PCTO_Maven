package com.cineca.app.backend.repository;

import com.cineca.app.backend.modelli.Esame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
// Spring ne creerà un'istanza automaticamente e la inietterà dove serve (con @Autowired).
public interface EsameRepository extends JpaRepository<Esame, Integer> {

    // Ereditando da JpaRepository, si ottengono metodi come:
    // - .save(Esame e)       -> INSERT o UPDATE
    // - .findAll()           -> SELECT * FROM esami
    // - .findById(Integer i) -> SELECT * FROM esami WHERE id = ?
    // - .deleteById(Integer i)-> DELETE FROM esami WHERE id = ?
    // Esame = Entità gestita
    // Integer = Tipo della chiave primaria (ID)

    //query derivata, fa la query sql comunque dietro le quinte da sola con l'id dello studente che risulterebe quindi in "SELECT * FROM esami WHERE studente_id = ?"
    List<Esame> findByStudenteId(Integer studenteId);
}