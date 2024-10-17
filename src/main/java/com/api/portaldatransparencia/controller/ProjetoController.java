package com.api.portaldatransparencia.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.api.portaldatransparencia.model.Arquivo;
import com.api.portaldatransparencia.model.TipoDocumento;
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

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<String> criarProjeto(
            @RequestParam("projeto") String projetoJson,
            @RequestParam(value = "planosDeTrabalho", required = false) MultipartFile[] planosDeTrabalho,
            @RequestParam(value = "contratos", required = false) MultipartFile[] contratos,
            @RequestParam(value = "termosAditivos", required = false) MultipartFile[] termosAditivos)
            throws IOException {

        // Configurar o ObjectMapper como antes
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // Log para verificar o conteúdo dos arquivos
        System.out.println("Planos de Trabalho: " + (planosDeTrabalho != null ? planosDeTrabalho.length : "null"));
        System.out.println("Contratos: " + (contratos != null ? contratos.length : "null"));
        System.out.println("Termos Aditivos: " + (termosAditivos != null ? termosAditivos.length : "null"));

        // Converter JSON para objeto Projeto
        Projeto projeto;
        try {
            projeto = objectMapper.readValue(projetoJson, Projeto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao converter JSON: " + e.getMessage());
        }

        System.out.println("Projeto recebido: " + projeto.getTitulo());

        // Salvar múltiplos arquivos
        if (planosDeTrabalho != null && planosDeTrabalho.length > 0) {
            System.out.println("Salvando planos de trabalho...");
            projetoService.salvarProjetoComArquivos(projeto, Arrays.asList(planosDeTrabalho),
                    TipoDocumento.PLANO_DE_TRABALHO);
        }

        if (contratos != null && contratos.length > 0) {
            System.out.println("Salvando contratos...");
            projetoService.salvarProjetoComArquivos(projeto, Arrays.asList(contratos), TipoDocumento.CONTRATO);
        }

        if (termosAditivos != null && termosAditivos.length > 0) {
            System.out.println("Salvando termos aditivos...");
            projetoService.salvarProjetoComArquivos(projeto, Arrays.asList(termosAditivos),
                    TipoDocumento.TERMO_ADITIVO);
        }

        // Salvar o projeto
        System.out.println("Salvando projeto final...");
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
    public List<Projeto> buscarProjetos(@RequestParam(required = false) String termo) {
        return projetoService.buscarProjetosPorTermo(termo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProjeto(@PathVariable Long id) {
        if (projetoService.buscarProjetoPorId(id).isPresent()) {
            projetoService.deletarProjeto(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<String> atualizarProjeto(
            @PathVariable Long id,
            @RequestParam("projeto") String projetoJson,
            @RequestParam(value = "planosDeTrabalho", required = false) MultipartFile[] planosDeTrabalho,
            @RequestParam(value = "contratos", required = false) MultipartFile[] contratos,
            @RequestParam(value = "termosAditivos", required = false) MultipartFile[] termosAditivos,
            @RequestParam(value = "arquivosRemovidos", required = false) String arquivosRemovidosJson)
            throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // Converter JSON para objeto Projeto
        Projeto projetoAtualizado;
        try {
            projetoAtualizado = objectMapper.readValue(projetoJson, Projeto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao converter JSON: " + e.getMessage());
        }

        // Converter JSON de arquivos removidos para uma lista de Strings
        List<String> arquivosRemovidos = new ArrayList<>();
        try {
            if (arquivosRemovidosJson != null && !arquivosRemovidosJson.isEmpty()) {
                arquivosRemovidos = Arrays.asList(objectMapper.readValue(arquivosRemovidosJson, String[].class));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao converter arquivos removidos: " + e.getMessage());
        }

        // Se algum dos arrays for nulo, inicializar como uma lista vazia
        List<MultipartFile> planosDeTrabalhoList = planosDeTrabalho != null ? Arrays.asList(planosDeTrabalho)
                : new ArrayList<>();
        List<MultipartFile> contratosList = contratos != null ? Arrays.asList(contratos) : new ArrayList<>();
        List<MultipartFile> termosAditivosList = termosAditivos != null ? Arrays.asList(termosAditivos)
                : new ArrayList<>();

        // Atualizar projeto e arquivos associados
        try {
            projetoService.atualizarProjeto(id, projetoAtualizado, planosDeTrabalhoList, contratosList,
                    termosAditivosList, arquivosRemovidos);
            return ResponseEntity.ok("Projeto atualizado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar projeto: " + e.getMessage());
        }
    }

}
