package com.api.portaldatransparencia.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.api.portaldatransparencia.model.Projeto;
import com.api.portaldatransparencia.service.ProjetoService;

@RestController
@RequestMapping("/projetos")
public class ProjetoController {

    @Autowired
    private ProjetoService projetoService;

    @PostMapping
    public ResponseEntity<Projeto> criarProjetoSemArquivo(@RequestBody Projeto projeto) {
        // Processa o projeto sem arquivo
        System.out.println("Descrição recebida: " + projeto.getDescricao());

        Projeto projetoSalvo = projetoService.salvarProjeto(projeto);
        return ResponseEntity.ok(projetoSalvo);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Projeto> criarProjetoComArquivo(
            @RequestParam("projeto") String projetoJson,
            @RequestParam("arquivo") MultipartFile arquivo) {

        // Converter JSON para o objeto Projeto
        ObjectMapper objectMapper = new ObjectMapper();
        Projeto projeto;
        try {
            projeto = objectMapper.readValue(projetoJson, Projeto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        // Salvar arquivo e associar ao projeto
        String nomeArquivo = projetoService.salvarArquivo(arquivo);
        projeto.setNomeArquivo(nomeArquivo);
        Projeto projetoSalvo = projetoService.salvarProjeto(projeto);

        return ResponseEntity.ok(projetoSalvo);
    }


    @GetMapping
    public List<Projeto> listarProjetos() {
        return projetoService.listarProjetos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Projeto> buscarProjetoPorId(@PathVariable Long id) {
        Optional<Projeto> projeto = projetoService.buscarProjetoPorId(id);
        return projeto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Projeto> buscarProjetos(
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String coordenador,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataTermino,
            @RequestParam(required = false) String classificacao,
            @RequestParam(required = false) String situacao
    ) {
        return projetoService.buscarProjetos(referencia, coordenador, dataInicio, dataTermino, classificacao, situacao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProjeto(@PathVariable Long id) {
        if (projetoService.buscarProjetoPorId(id).isPresent()) {
            projetoService.deletarProjeto(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Projeto> atualizarProjeto(@PathVariable Long id, @RequestBody Projeto projeto) {
        if (projetoService.buscarProjetoPorId(id).isPresent()) {
            projeto.setId(id);
            return ResponseEntity.ok(projetoService.salvarProjeto(projeto));
        }
        return ResponseEntity.notFound().build();
    }
}
