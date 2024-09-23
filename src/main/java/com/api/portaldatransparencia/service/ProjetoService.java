package com.api.portaldatransparencia.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.portaldatransparencia.model.Projeto;
import com.api.portaldatransparencia.repository.ProjetoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

@Service
public class ProjetoService {

    @Autowired
    private ProjetoRepository projetoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Projeto salvarProjeto(Projeto projeto) {
        return projetoRepository.save(projeto);
    }

    // Listar Projetos
    public List<Projeto> listarProjetos() {
        return projetoRepository.findAll();
    }

    // Buscar Projetos com filtros dinâmicos
    public List<Projeto> buscarProjetos(String referencia, String coordenador, LocalDate dataInicio, LocalDate dataTermino, String classificacao, String situacao) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Projeto> cq = cb.createQuery(Projeto.class);
        Root<Projeto> projeto = cq.from(Projeto.class);

        List<Predicate> predicates = new ArrayList<>();

        if (referencia != null && !referencia.isEmpty()) {
            predicates.add(cb.like(cb.lower(projeto.get("referencia")), "%" + referencia.toLowerCase() + "%"));
        }

        if (coordenador != null && !coordenador.isEmpty()) {
            predicates.add(cb.like(cb.lower(projeto.get("coordenador")), "%" + coordenador.toLowerCase() + "%"));
        }

        if (dataInicio != null) {
            predicates.add(cb.greaterThanOrEqualTo(projeto.get("dataInicio"), dataInicio));
        }

        if (dataTermino != null) {
            predicates.add(cb.lessThanOrEqualTo(projeto.get("dataTermino"), dataTermino));
        }

        if (classificacao != null && !classificacao.isEmpty()) {
            predicates.add(cb.equal(projeto.get("classificacao"), classificacao));
        }

        if (situacao != null && !situacao.isEmpty()) {
            predicates.add(cb.equal(projeto.get("situacao"), situacao));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(cq).getResultList();
    }

    // Buscar Projeto por ID
    public Optional<Projeto> buscarProjetoPorId(Long id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Projeto> cq = cb.createQuery(Projeto.class);
        Root<Projeto> projeto = cq.from(Projeto.class);

        Predicate idPredicate = cb.equal(projeto.get("id"), id);
        cq.where(idPredicate);

        Projeto resultado = entityManager.createQuery(cq).getSingleResult();
        return Optional.ofNullable(resultado);
    }

    // Deletar Projeto
    public void deletarProjeto(Long id) {
        projetoRepository.deleteById(id);
    }

    private final String pastaUpload = "uploads/";

    public String salvarArquivo(MultipartFile arquivo) {
        try {
            // Criar diretório se não existir
            Path pasta = Paths.get(pastaUpload);
            if (!Files.exists(pasta)) {
                Files.createDirectories(pasta);
            }

            // Salvar o arquivo
            String nomeArquivo = arquivo.getOriginalFilename();
            Path caminhoArquivo = pasta.resolve(nomeArquivo);
            Files.copy(arquivo.getInputStream(), caminhoArquivo);

            return nomeArquivo;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar o arquivo", e);
        }
    }

    // Salvar projeto (com o arquivo associado)
    public Projeto salvarProjetoComArquivo(Projeto projeto) {
        // Implementação do método para salvar o projeto
        return projetoRepository.save(projeto);
    }

}
