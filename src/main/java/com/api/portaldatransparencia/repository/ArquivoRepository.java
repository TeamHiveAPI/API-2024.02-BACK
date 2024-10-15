package com.api.portaldatransparencia.repository;

import com.api.portaldatransparencia.model.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {
	List<Arquivo> findByProjetoId(Long projetoId);
}

