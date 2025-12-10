package com.cineca.app.backend.repository;

import com.cineca.app.backend.modelli.Studente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudenteRepository extends JpaRepository<Studente, Integer> {
}
