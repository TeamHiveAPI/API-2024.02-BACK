package com.api.portaldatransparencia.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    // Diretórios para upload e download, configurados no application.properties
    @Value("${diretorio.upload}")
    private String uploadDir;

    @Value("${diretorio.download}")
    private String downloadDir;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> criarProjeto(
            @RequestParam("projeto") String projetoJson,
            @RequestParam(value = "planosDeTrabalho", required = false) MultipartFile[] planosDeTrabalho,
            @RequestParam(value = "contratos", required = false) MultipartFile[] contratos,
            @RequestParam(value = "termosAditivos", required = false) MultipartFile[] termosAditivos) throws IOException {

        // Configurar o ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // Converter JSON para objeto Projeto
        Projeto projeto;
        try {
            projeto = objectMapper.readValue(projetoJson, Projeto.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao converter JSON: " + e.getMessage());
        }

        // Salvar múltiplos arquivos
        if (planosDeTrabalho != null && planosDeTrabalho.length > 0) {
            projetoService.salvarProjetoComArquivos(projeto, Arrays.asList(planosDeTrabalho), TipoDocumento.PLANO_DE_TRABALHO);
        }

        if (contratos != null && contratos.length > 0) {
            projetoService.salvarProjetoComArquivos(projeto, Arrays.asList(contratos), TipoDocumento.CONTRATO);
        }

        if (termosAditivos != null && termosAditivos.length > 0) {
            projetoService.salvarProjetoComArquivos(projeto, Arrays.asList(termosAditivos), TipoDocumento.TERMO_ADITIVO);
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
    public List<Projeto> searchProjetos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String coordenador,
            @RequestParam(required = false) String contratante,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataTermino,
            @RequestParam(required = false) String termo
    ) {
        return projetoService.buscarProjetosPorCampos(titulo, coordenador, contratante, dataInicio, dataTermino, termo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProjeto(@PathVariable Long id) {
        if (projetoService.buscarProjetoPorId(id).isPresent()) {
            projetoService.deletarProjeto(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> atualizarProjeto(
            @PathVariable Long id,
            @RequestParam("projeto") String projetoJson,
            @RequestParam(value = "planosDeTrabalho", required = false) MultipartFile[] planosDeTrabalho,
            @RequestParam(value = "contratos", required = false) MultipartFile[] contratos,
            @RequestParam(value = "termosAditivos", required = false) MultipartFile[] termosAditivos,
            @RequestParam(value = "arquivosRemovidos", required = false) String arquivosRemovidosJson) throws IOException {

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

        // Atualizar projeto e arquivos associados
        projetoService.atualizarProjeto(id, projetoAtualizado,
                Arrays.asList(planosDeTrabalho), Arrays.asList(contratos), Arrays.asList(termosAditivos),
                Arrays.asList(objectMapper.readValue(arquivosRemovidosJson, String[].class)));

        return ResponseEntity.ok("Projeto atualizado com sucesso!");
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadArquivo(@PathVariable String filename) {
        try {
            File file = new File(uploadDir, filename);

            // Verifica se o arquivo existe
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toURI());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
