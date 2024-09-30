package com.api.portaldatransparencia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import jakarta.persistence.Column;

@Entity
public class Projeto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column (nullable = false)
	private String referencia;

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

	@Column (nullable = true)
	private String nomeArquivo;

	// Getters e Setters
	public Projeto() {}

	public Projeto(String referencia, String coordenador, String descricao, LocalDate dataInicio, LocalDate dataTermino,
	               String classificacao, String situacao, String empresa, double valor) {
		this.referencia = referencia;
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

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
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

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

}
