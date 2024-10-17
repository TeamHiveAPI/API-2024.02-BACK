package com.api.portaldatransparencia.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Projeto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column (nullable = false)
	private String titulo;

	@Column (nullable = false)
	private String coordenador;

	@Column (nullable = false)
	private LocalDate dataInicio;

	@Column (nullable = false, length = 2400)
	private String descricao;

	@Column (nullable = true)
	private LocalDate dataTermino;

	@Column (nullable = false)
	private String classificacao;

	@Column (nullable = false)
	private String situacao;

	@Column (nullable = false)
	private String empresa;

	@Column (nullable = false)
	private double valor;

	@OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Arquivo> arquivos = new ArrayList<>();

	public void addArquivo(Arquivo arquivo) {
		arquivos.add(arquivo);
		arquivo.setProjeto(this);
	}

	public void removeArquivo(Arquivo arquivo) {
		arquivos.remove(arquivo);
		arquivo.setProjeto(null);
	}

	// Getters e Setters
	public Projeto() {}

	public Projeto(String titulo, String coordenador, String descricao, LocalDate dataInicio, LocalDate dataTermino,
	               String classificacao, String situacao, String empresa, double valor) {
		this.titulo = titulo;
		this.coordenador = coordenador;
		this.descricao = descricao;
		this.dataInicio = dataInicio;
		this.dataTermino = dataTermino;
		this.classificacao = classificacao;
		this.situacao = situacao;
		this.empresa = empresa;
		this.valor = valor;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getCoordenador() {
		return coordenador;
	}

	public void setCoordenador(String coordenador) {
		this.coordenador = coordenador;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDate getDataTermino() {
		return dataTermino;
	}

	public void setDataTermino(LocalDate dataTermino) {
		this.dataTermino = dataTermino;
	}

	public String getClassificacao() {
		return classificacao;
	}

	public void setClassificacao(String classificacao) {
		this.classificacao = classificacao;
	}

	public String getSituacao() {
		return situacao;
	}

	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public List<Arquivo> getArquivos() {
		return arquivos;
	}

	public void setArquivos(List<Arquivo> novosArquivos) {
		// Remover os arquivos que não estão mais presentes
		arquivos.removeIf(a -> !novosArquivos.contains(a));

		// Adicionar novos arquivos
		for (Arquivo novoArquivo : novosArquivos) {
			if (!arquivos.contains(novoArquivo)) {
				addArquivo(novoArquivo);
			}
		}
	}
}
