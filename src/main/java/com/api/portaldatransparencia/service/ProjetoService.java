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

    // Buscar Projeto por ID
    public Optional<Projeto> buscarProjetoPorId(Long id) {
        // Busca o projeto pelo ID
        Optional<Projeto> projetoOpt = projetoRepository.findById(id);

        // Se o projeto for encontrado, buscar os arquivos associados
        if (projetoOpt.isPresent()) {
            Projeto projeto = projetoOpt.get();

            // Busca os arquivos associados ao projeto pelo ID do projeto
            List<Arquivo> arquivos = arquivoRepository.findByProjetoId(projeto.getId());

            // Define os arquivos no projeto
            projeto.setArquivos(arquivos);

            // Retorna o projeto com os arquivos
            return Optional.of(projeto);
        }

        // Caso o projeto não seja encontrado, retorna Optional vazio
        return Optional.empty();
    }

    // Deletar Projeto
    public void deletarProjeto(Long id) {
        Optional<Projeto> projetoOpt = projetoRepository.findById(id);
        if (projetoOpt.isPresent()) {
            Projeto projeto = projetoOpt.get();
            // Deleta os arquivos do sistema de arquivos
            for (Arquivo arquivo : projeto.getArquivos()) {
                Path filePath = Paths.get(arquivo.getUrl());
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    e.printStackTrace(); // Log de erro se não conseguir deletar o arquivo
                }
                // Remover o arquivo do banco de dados
                arquivoRepository.delete(arquivo);
            }
            // Remover o projeto do banco de dados
            projetoRepository.delete(projeto);
        }
    }

    public void atualizarProjeto(Long id, Projeto projetoAtualizado,
            List<MultipartFile> planosDeTrabalho,
            List<MultipartFile> contratos,
            List<MultipartFile> termosAditivos,
            List<String> arquivosRemovidos) throws IOException {
        // Carregar o projeto existente do banco de dados
        Optional<Projeto> projetoExistenteOpt = projetoRepository.findById(id);
        if (projetoExistenteOpt.isEmpty()) {
            throw new RuntimeException("Projeto não encontrado.");
        }

        Projeto projetoExistente = projetoExistenteOpt.get();

        // Atualizar os campos do projeto existente
        projetoExistente.setTitulo(projetoAtualizado.getTitulo());
        projetoExistente.setDescricao(projetoAtualizado.getDescricao());
        projetoExistente.setCoordenador(projetoAtualizado.getCoordenador());
        projetoExistente.setDataInicio(projetoAtualizado.getDataInicio());
        projetoExistente.setDataTermino(projetoAtualizado.getDataTermino());
        projetoExistente.setValor(projetoAtualizado.getValor());

        // Remover arquivos
        if (arquivosRemovidos != null && !arquivosRemovidos.isEmpty()) {
            for (String nomeArquivo : arquivosRemovidos) {
                Arquivo arquivoParaRemover = projetoExistente.getArquivos()
                        .stream()
                        .filter(a -> a.getNome().equals(nomeArquivo))
                        .findFirst()
                        .orElse(null);
                if (arquivoParaRemover != null) {
                    projetoExistente.removeArquivo(arquivoParaRemover); // Remove da coleção
                    arquivoRepository.delete(arquivoParaRemover); // Exclui do banco de dados
                    // Remover o arquivo do sistema de arquivos (opcional)
                    Files.deleteIfExists(Paths.get(arquivoParaRemover.getUrl()));
                }
            }
        }

        // Manter a coleção de arquivos existente
        List<Arquivo> arquivosExistentes = projetoExistente.getArquivos();

        // Adicionar novos arquivos de Planos de Trabalho
        if (planosDeTrabalho != null) {
            for (MultipartFile arquivo : planosDeTrabalho) {
                String urlArquivo = saveFileToStorage(arquivo);
                Arquivo novoArquivo = new Arquivo();
                novoArquivo.setNome(arquivo.getOriginalFilename());
                novoArquivo.setUrl(urlArquivo);
                novoArquivo.setTamanho(arquivo.getSize());
                novoArquivo.setTipoDocumento(TipoDocumento.PLANO_DE_TRABALHO);
                novoArquivo.setProjeto(projetoExistente);
                arquivosExistentes.add(novoArquivo);
            }
        }

        // Adicionar novos arquivos de Contratos
        if (contratos != null) {
            for (MultipartFile arquivo : contratos) {
                String urlArquivo = saveFileToStorage(arquivo);
                Arquivo novoArquivo = new Arquivo();
                novoArquivo.setNome(arquivo.getOriginalFilename());
                novoArquivo.setUrl(urlArquivo);
                novoArquivo.setTamanho(arquivo.getSize());
                novoArquivo.setTipoDocumento(TipoDocumento.CONTRATO);
                novoArquivo.setProjeto(projetoExistente);
                arquivosExistentes.add(novoArquivo);
            }
        }

        // Adicionar novos arquivos de Termos Aditivos
        if (termosAditivos != null) {
            for (MultipartFile arquivo : termosAditivos) {
                String urlArquivo = saveFileToStorage(arquivo);
                Arquivo novoArquivo = new Arquivo();
                novoArquivo.setNome(arquivo.getOriginalFilename());
                novoArquivo.setUrl(urlArquivo);
                novoArquivo.setTamanho(arquivo.getSize());
                novoArquivo.setTipoDocumento(TipoDocumento.TERMO_ADITIVO);
                novoArquivo.setProjeto(projetoExistente);
                arquivosExistentes.add(novoArquivo);
            }
        }

        // Salvar o projeto atualizado com a coleção de arquivos
        projetoRepository.save(projetoExistente);
    }

    public void salvarProjetoComArquivos(Projeto projeto, List<MultipartFile> arquivos, TipoDocumento tipoDocumento)
            throws IOException {
        // Primeiro, salva o projeto para garantir que ele tenha um ID
        Projeto projetoSalvo = projetoRepository.save(projeto);
        System.out.println("Projeto salvo com ID: " + projetoSalvo.getId());

        // Agora, adiciona os arquivos ao projeto salvo
        for (MultipartFile arquivo : arquivos) {
            String urlArquivo = saveFileToStorage(arquivo); // Salvar o arquivo no sistema de arquivos
            if (urlArquivo == null) {
                System.out.println("Falha ao salvar o arquivo " + arquivo.getOriginalFilename());
                continue; // Pula para o próximo arquivo se falhar ao salvar
            }

            // Criar o objeto Arquivo e associar ao projeto
            Arquivo novoArquivo = new Arquivo();
            novoArquivo.setNome(arquivo.getOriginalFilename());
            novoArquivo.setUrl(urlArquivo); // Define a URL do arquivo salvo
            novoArquivo.setTamanho(arquivo.getSize());
            novoArquivo.setTipoDocumento(tipoDocumento);
            novoArquivo.setProjeto(projetoSalvo); // Associa o arquivo ao projeto salvo

            arquivoRepository.save(novoArquivo); // Salvar o arquivo no repositório de Arquivo
            System.out.println("Arquivo salvo: " + novoArquivo.getNome());

            projetoSalvo.addArquivo(novoArquivo); // Adicionar o arquivo ao projeto
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
            return null; // Retorna null em caso de erro
        }

        // Retornar o caminho completo do arquivo salvo
        return path.toString();
    }

    public List<Projeto> buscarProjetosPorCampos(String titulo, String coordenador, String contratante, String dataInicio, String dataTermino, String termo) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Projeto> cq = cb.createQuery(Projeto.class);
        Root<Projeto> projeto = cq.from(Projeto.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtrar pelo título do projeto
        if (titulo != null && !titulo.isEmpty()) {
            predicates.add(cb.like(cb.lower(projeto.get("titulo")), "%" + titulo.toLowerCase() + "%"));
        }

        // Filtrar pelo coordenador
        if (coordenador != null && !coordenador.isEmpty()) {
            predicates.add(cb.like(cb.lower(projeto.get("coordenador")), "%" + coordenador.toLowerCase() + "%"));
        }

        // Filtrar pelo contratante
        if (contratante != null && !contratante.isEmpty()) {
            predicates.add(cb.like(cb.lower(projeto.get("empresa")), "%" + contratante.toLowerCase() + "%"));
        }

        // Filtrar pela data de início
        if (dataInicio != null && !dataInicio.isEmpty()) {
            try {
                LocalDate dataInicioFormatada = LocalDate.parse(dataInicio); // Converter string para LocalDate
                predicates.add(cb.equal(projeto.get("dataInicio"), dataInicioFormatada));
            } catch (Exception e) {
                // Tratar o erro de formatação de data, se necessário
                e.printStackTrace();
            }
        }

        // Filtrar pela data de término
        if (dataTermino != null && !dataTermino.isEmpty()) {
            try {
                LocalDate dataTerminoFormatada = LocalDate.parse(dataTermino); // Converter string para LocalDate
                predicates.add(cb.equal(projeto.get("dataTermino"), dataTerminoFormatada));
            } catch (Exception e) {
                // Tratar o erro de formatação de data, se necessário
                e.printStackTrace();
            }
        }

        // Filtrar por termo geral, incluindo busca por ID, datas e outros campos
        if (termo != null && !termo.isEmpty()) {
            List<Predicate> termoPredicates = new ArrayList<>();

            // Tentar converter o termo em número para a busca por ID
            try {
                Long idTermo = Long.parseLong(termo);
                termoPredicates.add(cb.equal(projeto.get("id"), idTermo));
            } catch (NumberFormatException e) {
                // Se não for um número, apenas continuar com as buscas nos outros campos
            }

            // Adicionar a busca por outros campos (texto)
            termoPredicates.add(cb.or(
                    cb.like(cb.lower(projeto.get("descricao")), "%" + termo.toLowerCase() + "%"),
                    cb.like(cb.lower(projeto.get("classificacao")), "%" + termo.toLowerCase() + "%"),
                    cb.like(cb.lower(projeto.get("titulo")), "%" + termo.toLowerCase() + "%"),
                    cb.like(cb.lower(projeto.get("coordenador")), "%" + termo.toLowerCase() + "%"),
                    cb.like(cb.lower(projeto.get("empresa")), "%" + termo.toLowerCase() + "%"),
                    cb.like(cb.lower(projeto.get("situacao")), "%" + termo.toLowerCase() + "%")
            ));

            // Adicionar a busca por data de início e término, tentando converter o termo para uma data
            try {
                LocalDate dataTermo = LocalDate.parse(termo);
                termoPredicates.add(cb.or(
                        cb.equal(projeto.get("dataInicio"), dataTermo),
                        cb.equal(projeto.get("dataTermino"), dataTermo)
                ));
            } catch (Exception e) {
                // Se o termo não for uma data válida, continuar com a busca nos outros campos
            }

            // Adicionar os predicados ao where
            predicates.add(cb.or(termoPredicates.toArray(new Predicate[0])));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }


}
