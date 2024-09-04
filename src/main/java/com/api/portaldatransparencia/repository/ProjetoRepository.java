package com.api.portaldatransparencia.repository;

import com.api.portaldatransparencia.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
}
