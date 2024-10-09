package com.api.portaldatransparencia.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> criarProjeto(
            @RequestParam("projeto") String projetoJson,
            @RequestParam(value = "planosDeTrabalho", required = false) MultipartFile planosDeTrabalho,
            @RequestParam(value = "contratos", required = false) MultipartFile contratos,
            @RequestParam(value = "termosAditivos", required = false) MultipartFile termosAditivos) throws IOException {

        // Configurando o ObjectMapper para suportar LocalDate
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Para garantir o formato ISO de datas
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE); // Desabilitar ajustes automáticos de timezone

        // Converter JSON para o objeto Projeto
        Projeto projeto;
        try {
            projeto = objectMapper.readValue(projetoJson, Projeto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao converter JSON para objeto Projeto: " + e.getMessage());
        }

        // Salvar os arquivos e associá-los ao projeto, se presentes
        if (planosDeTrabalho != null && !planosDeTrabalho.isEmpty()) {
            String nomeArquivo = projetoService.salvarArquivo(planosDeTrabalho);
            projeto.setNomeArquivoPlanosDeTrabalho(nomeArquivo);
        }

        if (contratos != null && !contratos.isEmpty()) {
            String nomeArquivo = projetoService.salvarArquivo(contratos);
            projeto.setNomeArquivoContratos(nomeArquivo);
        }

        if (termosAditivos != null && !termosAditivos.isEmpty()) {
            String nomeArquivo = projetoService.salvarArquivo(termosAditivos);
            projeto.setNomeArquivoTermosAditivos(nomeArquivo);
        }

        // Salvar o projeto
        Projeto projetoSalvo = projetoService.salvarProjeto(projeto);
        return ResponseEntity.ok("Projeto criado com sucesso!");
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
