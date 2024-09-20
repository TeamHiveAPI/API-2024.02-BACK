package com.api.portaldatransparencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.portaldatransparencia.model.DatabaseInitializer;

@Repository
public interface DatabaseInitializerRepository extends JpaRepository<DatabaseInitializer, Long> {
}
