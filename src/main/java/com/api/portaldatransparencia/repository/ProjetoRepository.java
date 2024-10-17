package com.api.portaldatransparencia.repository;

import com.api.portaldatransparencia.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @Query("SELECT p FROM Projeto p WHERE " +
            "(:referencia IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :referencia, '%'))) AND " +
            "(:coordenador IS NULL OR LOWER(p.coordenador) LIKE LOWER(CONCAT('%', :coordenador, '%'))) AND " +
            "(:dataInicio IS NULL OR p.dataInicio >= :dataInicio) AND " +
            "(:dataTermino IS NULL OR p.dataTermino <= :dataTermino) AND " +
            "(:classificacao IS NULL OR LOWER(p.classificacao) = LOWER(:classificacao)) AND " +
            "(:situacao IS NULL OR LOWER(p.situacao) = LOWER(:situacao))")
    List<Projeto> buscarProjetos(
            @Param("referencia") String referencia,
            @Param("coordenador") String coordenador,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataTermino") LocalDate dataTermino,
            @Param("classificacao") String classificacao,
            @Param("situacao") String situacao
    );
}
