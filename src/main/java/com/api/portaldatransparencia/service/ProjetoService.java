package com.api.portaldatransparencia.service;

import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.api.portaldatransparencia.model.Arquivo;
import com.api.portaldatransparencia.model.TipoDocumento;
import com.api.portaldatransparencia.repository.ArquivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class ProjetoService {

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private ArquivoRepository arquivoRepository;

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

    @Value("${diretorio.upload}")
    private String diretorioUpload;

    public String salvarArquivo(MultipartFile file) throws IOException {
        String nomeArquivo = file.getOriginalFilename();
        Path caminho = Paths.get(diretorioUpload + nomeArquivo);
        Files.copy(file.getInputStream(), caminho, StandardCopyOption.REPLACE_EXISTING);
        return caminho.toString();
    }

    public void salvarProjetoComArquivos(Projeto projeto, List<MultipartFile> arquivos, TipoDocumento tipoDocumento) throws IOException {
        // Primeiro, salva o projeto para garantir que ele tenha um ID
        Projeto projetoSalvo = projetoRepository.save(projeto);
        System.out.println("Projeto salvo com ID: " + projetoSalvo.getId());

        // Agora, adiciona os arquivos ao projeto salvo
        for (MultipartFile arquivo : arquivos) {
            String urlArquivo = saveFileToStorage(arquivo);  // Salvar o arquivo no sistema de arquivos
            if (urlArquivo == null) {
                System.out.println("Falha ao salvar o arquivo " + arquivo.getOriginalFilename());
                continue;  // Pula para o próximo arquivo se falhar ao salvar
            }

            // Criar o objeto Arquivo e associar ao projeto
            Arquivo novoArquivo = new Arquivo();
            novoArquivo.setNome(arquivo.getOriginalFilename());
            novoArquivo.setUrl(urlArquivo);  // Define a URL do arquivo salvo
            novoArquivo.setTipoDocumento(tipoDocumento);
            novoArquivo.setProjeto(projetoSalvo);  // Associa o arquivo ao projeto salvo

            arquivoRepository.save(novoArquivo);  // Salvar o arquivo no repositório de Arquivo
            System.out.println("Arquivo salvo: " + novoArquivo.getNome());

            projetoSalvo.addArquivo(novoArquivo);  // Adicionar o arquivo ao projeto
        }

        // Salva o projeto novamente para garantir a associação dos arquivos
        projetoRepository.save(projetoSalvo);
        System.out.println("Arquivos associados e projeto salvo com arquivos.");
    }


    private String saveFileToStorage(MultipartFile file) {
        String folder = "src/main/resources/static/uploads/";
        Path path = Paths.get(folder + file.getOriginalFilename());

        try {
            // Criar os diretórios caso ainda não existam
            Files.createDirectories(path.getParent());

            // Salvar o arquivo no diretório
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            // Log do erro e retorno nulo se houver falha
            e.printStackTrace();
            return null;  // Retorna null em caso de erro
        }

        // Retornar o caminho completo do arquivo salvo
        return path.toString();
    }

}
