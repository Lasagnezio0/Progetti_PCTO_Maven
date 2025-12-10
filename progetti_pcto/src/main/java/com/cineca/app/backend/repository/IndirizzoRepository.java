package com.cineca.app.backend.repository;

import com.cineca.app.backend.modelli.Indirizzo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndirizzoRepository extends JpaRepository<Indirizzo, Integer> {
}

