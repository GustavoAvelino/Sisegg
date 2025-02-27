package com.example.Sisegg.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ImportacaoDTO {

    private String nrProposta;
    private String nrApolice;
    private String nrEndosso;
    private String clienteNome;
    private String clienteCpf;
    private LocalDate dataVigenciaInicio;
    private LocalDate dataVigenciaFim;
    private LocalDate dataEmissao; 
    private BigDecimal premio;
    private LocalDate dataPrimeiraParcela;
    private BigDecimal valorPrimeiraParcela;
    private LocalDate dataSegundaParcela;
    private BigDecimal valorSegundaParcela;
    private String placa;
    private int numeroParcelas;

    public String getNrProposta() {
        return nrProposta;
    }
    public void setNrProposta(String nrProposta) {
        this.nrProposta = nrProposta;
    }

    public String getNrApolice() {
        return nrApolice;
    }
    public void setNrApolice(String nrApolice) {
        this.nrApolice = nrApolice;
    }

    public String getNrEndosso() {
        return nrEndosso;
    }
    public void setNrEndosso(String nrEndosso) {
        this.nrEndosso = nrEndosso;
    }

    public String getClienteNome() {
        return clienteNome;
    }
    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteCpf() {
        return clienteCpf;
    }
    public void setClienteCpf(String clienteCpf) {
        this.clienteCpf = clienteCpf;
    }

    public LocalDate getDataVigenciaInicio() {
        return dataVigenciaInicio;
    }
    public void setDataVigenciaInicio(LocalDate dataVigenciaInicio) {
        this.dataVigenciaInicio = dataVigenciaInicio;
    }

    public LocalDate getDataVigenciaFim() {
        return dataVigenciaFim;
    }
    public void setDataVigenciaFim(LocalDate dataVigenciaFim) {
        this.dataVigenciaFim = dataVigenciaFim;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }
    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public BigDecimal getPremio() {
        return premio;
    }
    public void setPremio(BigDecimal premio) {
        this.premio = premio;
    }

    public LocalDate getDataPrimeiraParcela() {
        return dataPrimeiraParcela;
    }
    public void setDataPrimeiraParcela(LocalDate dataPrimeiraParcela) {
        this.dataPrimeiraParcela = dataPrimeiraParcela;
    }

    public BigDecimal getValorPrimeiraParcela() {
        return valorPrimeiraParcela;
    }
    public void setValorPrimeiraParcela(BigDecimal valorPrimeiraParcela) {
        this.valorPrimeiraParcela = valorPrimeiraParcela;
    }

    public LocalDate getDataSegundaParcela() {
        return dataSegundaParcela;
    }
    public void setDataSegundaParcela(LocalDate dataSegundaParcela) {
        this.dataSegundaParcela = dataSegundaParcela;
    }

    public BigDecimal getValorSegundaParcela() {
        return valorSegundaParcela;
    }
    public void setValorSegundaParcela(BigDecimal valorSegundaParcela) {
        this.valorSegundaParcela = valorSegundaParcela;
    }

    public String getPlaca() {
        return placa;
    }
    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public int getNumeroParcelas() {
        return numeroParcelas;
    }
    public void setNumeroParcelas(int numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }
}
